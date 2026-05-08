from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import pandas as pd
import numpy as np
import io
import tensorflow as tf
import joblib

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

try:
    MODEL = tf.keras.models.load_model('watt_mate_best_model.keras', compile=False)
    SCALER = joblib.load('watt_mate_scaler.pkl')
    print("✅ AI 모델 및 스케일러 로드 성공")
except Exception as e:
    MODEL, SCALER = None, None
    print(f"⚠️ 로드 실패: {e}")

def preprocess(df):
    hourly_cols = [f"{i:02d}:00" for i in range(1, 25)]
    df_melted = df.melt(id_vars=['날짜'], value_vars=hourly_cols, var_name='시간', value_name='kWh')
    
    # 시간 정렬 (01:00 -> 1)
    df_melted['hour_int'] = df_melted['시간'].str.split(':').str[0].astype(int)
    df_melted = df_melted.sort_values(by=['날짜', 'hour_int']).reset_index(drop=True)
    df_melted['kWh'] = pd.to_numeric(df_melted['kWh'], errors='coerce').fillna(0)
    
    return df_melted

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    if not MODEL or not SCALER:
        raise HTTPException(status_code=500, detail="모델 로드 필요")

    try:
        contents = await file.read()
        try:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='cp949')
        except:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='utf-8')
            
        df_proc = preprocess(df_raw)
        
        # --- 1. 최근 한 달(30일) 데이터 분석 ---
        # 30일 * 24시간 = 최근 720개 행 추출
        last_month_df = df_proc.tail(720) 
        
        # 최근 한 달간의 "시간별 평균" 사용량 (01:00 ~ 24:00 패턴)
        # 과거와 너무 똑같은 문제를 방지하기 위해 '주간'이 아닌 '월간' 패턴으로 희석
        month_avg_by_hour = last_month_df.groupby('시간', sort=False)['kWh'].mean().tolist()

        # --- 2. 다음 달 평균 시간별 전력량 예측 ---
        # LSTM 입력: 최근 24시간의 트렌드를 보고 다음 패턴을 예측
        scaled_data = SCALER.transform(df_proc[['kWh']])
        input_window = scaled_data[-24:].reshape(1, 24, 1) 
        
        pred_scaled = MODEL.predict(input_window)
        pred_rescaled = SCALER.inverse_transform(pred_scaled.reshape(-1, 1)).flatten()
        
        # 예측값 보정 (음수 제거 및 타입 변환)
        # '다음 달' 예측이므로 단순 익일 예측보다 약간의 가중치(예: 계절성 2~5%)를 고려해볼 수 있음
        next_period_pred = [max(0, float(v)) for v in pred_rescaled]

        return {
            "lastMonthAvg": month_avg_by_hour,   # 최근 한 달 시간별 평균 패턴
            "nextMonthPred": next_period_pred[:24], # 모델이 예측한 다음 주기 패턴
            "status": "Monthly Analysis Success"
        }
        
    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)