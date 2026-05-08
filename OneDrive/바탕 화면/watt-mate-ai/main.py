from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import pandas as pd
import numpy as np
import io
import tensorflow as tf
import joblib

app = FastAPI()

# ✅ 프론트엔드 직접 통신을 위한 CORS 허용 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 모든 도메인 허용 (테스트 완료 후 리액트 주소만 넣는 것 권장)
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 모델 및 스케일러 로드
try:
    MODEL = tf.keras.models.load_model('watt_mate_best_model.keras')
    SCALER = joblib.load('watt_mate_scaler.pkl')
    print("✅ AI 모델 로드 성공")
except Exception as e:
    MODEL, SCALER = None, None
    print(f"⚠️ 모델 로드 실패: {e}")

def preprocess(df):
    # 01:00 ~ 24:00 컬럼 리스트 생성
    hourly_cols = [f"{i:02d}:00" for i in range(1, 25)]
    
    # 데이터 재구조화 (Wide -> Long)
    df_melted = df.melt(id_vars=['날짜'], value_vars=hourly_cols, var_name='시간', value_name='kWh')
    df_melted['kWh'] = pd.to_numeric(df_melted['kWh'], errors='coerce').fillna(0)
    
    # 날짜와 시간 순으로 정렬
    return df_melted.sort_values(by=['날짜', '시간']).reset_index(drop=True)

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        # CSV 읽기 (한글 인코딩 대응)
        try:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='cp949')
        except:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='utf-8')
            
        df_proc = preprocess(df_raw)
        
        # 1. 이번 주 평균 전력량 (최근 7일 데이터 기반 시간별 평균)
        # 시간별로 그룹화하여 01:00~24:00 순서 유지
        this_week_avg = df_proc.tail(168).groupby('시간', sort=False)['kWh'].mean().tolist()

        # 2. 다음 주 예측 전력량
        if MODEL and SCALER:
            # 스케일링 적용
            scaled = SCALER.transform(df_proc[['kWh']])
            # 최근 24시간 데이터를 입력값으로 사용 (모델 형태에 맞춤)
            input_win = scaled[-24:].reshape(1, 24, 1) 
            
            pred_scaled = MODEL.predict(input_win)
            # 원래 단위로 복구
            next_week_pred = SCALER.inverse_transform(pred_scaled.reshape(-1, 1)).flatten().tolist()
        else:
            # 모델이 없을 경우 대비한 기본 로직
            next_week_pred = [v * 1.05 for v in this_week_avg] 

        return {
            "thisWeekAvg": this_week_avg, 
            "nextWeekPred": next_week_pred
        }
        
    except Exception as e:
        print(f"Error during prediction: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)