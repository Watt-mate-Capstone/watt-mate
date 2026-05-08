from fastapi import FastAPI, UploadFile, File, HTTPException
import pandas as pd
import numpy as np
import io
import tensorflow as tf
import joblib

app = FastAPI()

# 모델 및 스케일러 로드 (파일 경로 확인 필수)
try:
    MODEL = tf.keras.models.load_model('model/wattmate_lstm_model.h5')
    SCALER = joblib.load('model/scaler.pkl')
    print("✅ AI 모델 로드 성공")
except Exception as e:
    MODEL, SCALER = None, None
    print(f"⚠️ 모델 로드 실패 (테스트 모드): {e}")

def preprocess(df):
    hourly_cols = [f"{i:02d}:00" for i in range(1, 25)]
    df_melted = df.melt(id_vars=['날짜'], value_vars=hourly_cols, var_name='시간', value_name='kWh')
    df_melted['kWh'] = pd.to_numeric(df_melted['kWh'], errors='coerce').fillna(0)
    return df_melted.sort_values(by=['날짜', '시간']).reset_index(drop=True)

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    try:
        contents = await file.read()
        df_raw = pd.read_csv(io.BytesIO(contents), encoding='cp949')
        df_proc = preprocess(df_raw)
        
        # 이번주 평균 (01:00~24:00)
        this_week_avg = df_proc.tail(168).groupby('시간')['kWh'].mean().sort_index().tolist()

        if MODEL and SCALER:
            scaled = SCALER.transform(df_proc[['kWh']])
            input_win = scaled[-24:].reshape(1, 24, 1) # 최근 24시간 입력
            pred_scaled = MODEL.predict(input_win)
            next_week_pred = SCALER.inverse_transform(pred_scaled.reshape(-1, 1)).flatten().tolist()
        else:
            next_week_pred = [v * 0.95 for v in this_week_avg] # 더미 데이터

        return {"thisWeekAvg": this_week_avg, "nextWeekPred": next_week_pred}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))