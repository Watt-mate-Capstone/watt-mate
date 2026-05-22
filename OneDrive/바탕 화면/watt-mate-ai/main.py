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

app = FastAPI(title="WattMate AI API Server for AWS Deployment")

# ✅ 1. CORS 설정 (React 프론트엔드 직접 호출 방어 및 Spring RestTemplate 허용)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ✅ 2. AI 모델 및 스케일러 파일 로드
# AWS EC2 배포 경로 혹은 로컬 상대 경로에 맞게 자동 지정
MODEL_PATH = 'watt_mate_model.keras'
SCALER_PATH = 'watt_mate_scaler.pkl'

try:
    if os.path.exists(MODEL_PATH) and os.path.exists(SCALER_PATH):
        # 외부 가중치 컴파일 경고 우회를 위해 compile=False 지정
        MODEL = tf.keras.models.load_model(MODEL_PATH, compile=False)
        SCALER = joblib.load(SCALER_PATH)
        print("✅ [AI 엔진] 베이스 파인튜닝 모델 및 스케일러 로드 성공")
    else:
        MODEL, SCALER = None, None
        print("⚠️ [AI 엔진] 경고: 모델 혹은 스케일러 파일을 찾을 수 없습니다. 경로를 확인하세요.")
except Exception as e:
    MODEL, SCALER = None, None
    print(f"🚨 [AI 엔진] 로딩 중 치명적 에러 발생: {e}")


# ✅ 3. 데이터 정제 전처리 함수 (Spring 및 React 시차 무력화)
def preprocess(df):
    hourly_cols = [f"{i:02d}:00" for i in range(1, 25)]
    
    # 가로(Wide) 구조의 24시간 데이터를 세로(Long) 구조 시계열로 Melt 변환
    df_melted = df.melt(id_vars=['날짜'], value_vars=hourly_cols, var_name='시간', value_name='kWh')
    
    # 24:00을 자바로 넘기면 파싱 에러가 발생하므로, 정수형 변환 및 정렬을 위한 헬퍼 필드 생성
    df_melted['hour_int'] = df_melted['시간'].str.split(':').str[0].astype(int)
    
    # 날짜와 시간 순으로 칼같이 정렬 (시계열 연속성 확보)
    df_melted = df_melted.sort_values(by=['날짜', 'hour_int']).reset_index(drop=True)
    df_melted['kWh'] = pd.to_numeric(df_melted['kWh'], errors='coerce').fillna(0.0)
    return df_melted


# ✅ 4. 예측 서빙 라우터
@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    if MODEL is None or SCALER is None:
        raise HTTPException(status_code=500, detail="AI 예측 엔진이 서버에 로드되지 않았습니다.")

    try:
        # 파일 바이너리 스트림 읽기
        contents = await file.read()
        
        # 한국어 윈도우 인코딩(CP949) 및 리눅스/맥 표준(UTF-8) 예외방어 예치
        try:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='cp949')
        except Exception:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='utf-8-sig')
            
        # 데이터 시계열 타임라인 가공
        df_proc = preprocess(df_raw)

        # -------------------------------------------------------------
        # 5. [실측 데이터 가공] 과거 전체 데이터를 백엔드 데이터형식으로 매핑
        # -------------------------------------------------------------
        hourly_history = []
        for _, row in df_proc.iterrows():
            # Spring의 DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm") 양식과 100% 매칭
            time_str = f"{row['시간']}"
            hourly_history.append({
                "timestamp": f"{row['날짜']} {time_str}",
                "usage": float(row['kWh'])
            })

        # -------------------------------------------------------------
        # 6. [핵심 수정: 예측 붕괴 방지] 168시간 LSTM 미래 재귀 예측 연산
        # -------------------------------------------------------------
        # 스케일러 정규화
        scaled_data = SCALER.transform(df_proc[['kWh']])
        
        # 모델 빌드 당시 설정한 일주일 윈도우(168)를 정확히 바인딩
        LOOK_BACK = 168
        
        if len(scaled_data) < LOOK_BACK:
            raise ValueError(f"입력 데이터 파일의 총 시간 분량이 윈도우 사이즈({LOOK_BACK}시간)보다 적습니다.")
            
        current_batch = scaled_data[-LOOK_BACK:].reshape(1, LOOK_BACK, 1)
        predictions_scaled = []

        # 스프링부트 서비스 요구사항인 미래 24시간 타임스텝 예측 진행
        prediction_hours = 24 

        for _ in range(prediction_hours):
            # 다음 1시간 예측
            next_pred = MODEL.predict(current_batch, verbose=0)
            
            # 차원 방어 후 스케일 값 추출
            val = next_pred[0, 0] if next_pred.ndim > 1 else next_pred[0]
            predictions_scaled.append(val)
            
            # [윈도우 슬라이딩]: 예측값을 꼬리에 붙이고 첫번째 시간 삭제하여 168 차원 유지
            next_val_reshaped = np.array(val).reshape(1, 1, 1)
            current_batch = np.append(current_batch[:, 1:, :], next_val_reshaped, axis=1)

        # MinMaxScaler 역변환을 통한 kWh 스케일 복원
        predictions_rescaled = SCALER.inverse_transform(np.array(predictions_scaled).reshape(-1, 1)).flatten().tolist()
        
        # 하드웨어 오차로 인한 기계적 음수 튀는 현상 원천 차단
        next_preds = [max(0.001, float(v)) for v in predictions_rescaled]

        # -------------------------------------------------------------
        # 7. Spring Boot 백엔드 DTO(PowerAnalysisResponse) 맞춤형 반환
        # -------------------------------------------------------------
        return {
            "hourlyHistory": hourly_history, 
            "next24hPred": next_preds, 
            "status": "Success"
        }
        
    except Exception as e:
        import traceback
        traceback.print_exc() # AWS 가동 중 디버깅을 위한 에러 트레이스 출력
        raise HTTPException(status_code=500, detail=f"AI 분석 파이프라인 내부 에러: {str(e)}")

# 헬스체크 엔드포인트
@app.get("/")
def health_check():
    return {"status": "healthy", "service": "WattMate AI Model Server"}

if __name__ == "__main__":
    import uvicorn
    # AWS EC2의 퍼블릭 개방을 위해 호스트 주소를 0.0.0.0으로 바인딩
    uvicorn.run(app, host="0.0.0.0", port=8000)