import os
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'      # 텐서플로우 내부 시스템 로그 숨김
os.environ['TF_ENABLE_ONEDNN_OPTS'] = '0'     # 부동 소수점 오차 방지를 위한 안내 로그 숨김

import io
import joblib
import numpy as np
import pandas as pd
import tensorflow as tf
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List

app = FastAPI(title="WattMate AI API Server - May Full Month Prediction")

# ✅ 1. CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ✅ 2. AI 모델 및 스케일러 파일 로드
MODEL_PATH = 'watt_mate_model.keras'
SCALER_PATH = 'watt_mate_scaler.pkl'

try:
    if os.path.exists(MODEL_PATH) and os.path.exists(SCALER_PATH):
        MODEL = tf.keras.models.load_model(MODEL_PATH, compile=False)
        SCALER = joblib.load(SCALER_PATH)
        print("✅ [AI 엔진] 베이스 파인튜닝 모델 및 스케일러 로드 성공")
    else:
        MODEL, SCALER = None, None
        print("⚠️ [AI 엔진] 경고: 모델 혹은 스케일러 파일을 찾을 수 없습니다.")
except Exception as e:
    MODEL, SCALER = None, None
    print(f"🚨 [AI 엔진] 로딩 중 치명적 에러 발생: {e}")


# ✅ 3. 데이터 정제 전처리 함수
def preprocess(df):
    hourly_cols = [f"{i:02d}:00" for i in range(1, 25)]
    
    # 가로 구조의 데이터를 세로 구조 시계열로 Melt 변환
    df_melted = df.melt(id_vars=['날짜'], value_vars=hourly_cols, var_name='시간', value_name='kWh')
    df_melted['hour_int'] = df_melted['시간'].str.split(':').str[0].astype(int)
    
    # 날짜와 시간 순으로 칼같이 정렬
    df_melted = df_melted.sort_values(by=['날짜', 'hour_int']).reset_index(drop=True)
    df_melted['kWh'] = pd.to_numeric(df_melted['kWh'], errors='coerce').fillna(0.0)
    return df_melted


# ✅ 4. 예측 서빙 라우터 (5월 한 달 전체 확장)
@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    if MODEL is None or SCALER is None:
        raise HTTPException(status_code=500, detail="AI 예측 엔진이 서버에 로드되지 않았습니다.")

    try:
        contents = await file.read()
        
        try:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='cp949')
        except Exception:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='utf-8-sig')
            
        df_proc = preprocess(df_raw)

        # -------------------------------------------------------------
        # 5. [실측 데이터 가공] 과거 전체 데이터 매핑 (~4월 30일)
        # -------------------------------------------------------------
        hourly_history = []
        for _, row in df_proc.iterrows():
            time_str = f"{row['시간']}"
            hourly_history.append({
                "timestamp": f"{row['날짜']} {time_str}",
                "usage": float(row['kWh'])
            })

        # -------------------------------------------------------------
        # 6. [핵심 수정] 5월 한 달 전체(31일 * 24시간 = 744시간) 재귀 예측
        # -------------------------------------------------------------
        scaled_data = SCALER.transform(df_proc[['kWh']])
        
        LOOK_BACK = 168  # 일주일 패턴 윈도우 크기 고정
        
        if len(scaled_data) < LOOK_BACK:
            raise ValueError(f"입력 데이터 파일의 총 시간 분량이 윈도우 사이즈({LOOK_BACK}시간)보다 적습니다.")
            
        current_batch = scaled_data[-LOOK_BACK:].reshape(1, LOOK_BACK, 1)
        predictions_scaled = []

        # 🌟 예측 기간을 5월 한 달 전체인 744시간으로 확장
        prediction_hours = 31 * 24 

        print(f"🚀 [AI 연산] 5월 한 달 전체({prediction_hours}시간) 실시간 재귀 예측 루프 가동 시작...")
        for _ in range(prediction_hours):
            next_pred = MODEL.predict(current_batch, verbose=0)
            val = next_pred[0, 0] if next_pred.ndim > 1 else next_pred[0]
            predictions_scaled.append(val)
            
            # 윈도우 슬라이딩
            next_val_reshaped = np.array(val).reshape(1, 1, 1)
            current_batch = np.append(current_batch[:, 1:, :], next_val_reshaped, axis=1)

        # MinMaxScaler 역변환
        predictions_rescaled = SCALER.inverse_transform(np.array(predictions_scaled).reshape(-1, 1)).flatten().tolist()
        
        # 🌟 하한 보정선 설치: 재귀 오차로 인해 후반부 예측값이 0으로 무너지는 것 방지 (기저전력 0.15 상주)
        next_preds = [max(0.15, float(v)) for v in predictions_rescaled]

        # -------------------------------------------------------------
        # 7. Spring Boot 백엔드 DTO 매칭 반환 (키 이름 next24hPred 유지)
        # -------------------------------------------------------------
        return {
            "hourlyHistory": hourly_history, 
            "next24hPred": next_preds, 
            "status": "Success"
        }
        
    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"AI 분석 파이프라인 내부 에러: {str(e)}")


@app.get("/")
def health_check():
    return {"status": "healthy", "service": "WattMate AI Full-Month Prediction Server"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)