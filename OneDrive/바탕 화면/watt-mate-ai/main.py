from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import pandas as pd
import numpy as np
import io
import tensorflow as tf
import joblib

app = FastAPI()

# ✅ CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- [1] AI 모델 및 스케일러 로드 (유지) ---
try:
    MODEL = tf.keras.models.load_model('watt_mate_best_model.keras', compile=False)
    SCALER = joblib.load('watt_mate_scaler.pkl')
    print("✅ AI 모델 및 스케일러 로드 성공")
except Exception as e:
    MODEL, SCALER = None, None
    print(f"⚠️ 로드 실패: {e}")

# --- [2] 데이터 전처리 및 재구조화 (유지) ---
def preprocess(df):
    hourly_cols = [f"{i:02d}:00" for i in range(1, 25)]
    # Wide -> Long 변환
    df_melted = df.melt(id_vars=['날짜'], value_vars=hourly_cols, var_name='시간', value_name='kWh')
    df_melted['hour_int'] = df_melted['시간'].str.split(':').str[0].astype(int)
    # 정렬
    df_melted = df_melted.sort_values(by=['날짜', 'hour_int']).reset_index(drop=True)
    df_melted['kWh'] = pd.to_numeric(df_melted['kWh'], errors='coerce').fillna(0)
    return df_melted

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    if not MODEL or not SCALER:
        raise HTTPException(status_code=500, detail="AI 모델이 로드되지 않았습니다.")

    try:
        contents = await file.read()
        # --- [3] 인코딩 대응 (유지) ---
        try:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='cp949')
        except:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='utf-8')
            
        df_proc = preprocess(df_raw) # 전체 4개월 데이터

        # --- [4] 전체 시간별 실측 데이터 추출 (신규: DB 저장용) ---
        hourly_history = []
        for _, row in df_proc.iterrows():
            hourly_history.append({
                "timestamp": f"{row['날짜']} {row['시간']}",
                "usage": float(row['kWh'])
            })

        # --- [5] 재귀적 예측 로직 (유지) ---
        scaled_data = SCALER.transform(df_proc[['kWh']])
        current_batch = scaled_data[-24:].reshape(1, 24, 1)
        predictions_scaled = []

        for _ in range(24):
            next_pred = MODEL.predict(current_batch, verbose=0)
            val = next_pred[0, 0] if next_pred.ndim > 1 else next_pred[0]
            predictions_scaled.append(val)
            # 윈도우 업데이트
            next_val_reshaped = np.array(val).reshape(1, 1, 1)
            current_batch = np.append(current_batch[:, 1:, :], next_val_reshaped, axis=1)

        # 원래 단위 복구
        predictions_rescaled = SCALER.inverse_transform(np.array(predictions_scaled).reshape(-1, 1)).flatten().tolist()
        next_24h_pred = [max(0, float(v)) for v in predictions_rescaled]

        return {
            "hourlyHistory": hourly_history,  # 4개월 전체 실측 데이터
            "next24hPred": next_24h_pred,     # 미래 24시간 예측 데이터
            "status": "Success"
        }
        
    except Exception as e:
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)