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

# 모델 및 스케일러 로드
try:
    # compile=False는 커스텀 손실 함수나 메트릭이 있을 경우 로드 에러를 방지합니다.
    MODEL = tf.keras.models.load_model('watt_mate_best_model.keras', compile=False)
    SCALER = joblib.load('watt_mate_scaler.pkl')
    print("✅ AI 모델 및 스케일러 로드 성공")
except Exception as e:
    MODEL, SCALER = None, None
    print(f"⚠️ 로드 실패: {e}")

def preprocess(df):
    """
    데이터 전처리: Wide 포맷을 Long 포맷으로 변환하고 시간 순으로 정확히 정렬합니다.
    """
    hourly_cols = [f"{i:02d}:00" for i in range(1, 25)]
    
    # 데이터 재구조화 (Wide -> Long)
    df_melted = df.melt(id_vars=['날짜'], value_vars=hourly_cols, var_name='시간', value_name='kWh')
    
    # 시간 정렬 (01:00 -> 1)
    df_melted['hour_int'] = df_melted['시간'].str.split(':').str[0].astype(int)
    
    # 날짜와 시간(정수) 순으로 정렬해야 데이터 순서가 뒤섞이지 않습니다.
    df_melted = df_melted.sort_values(by=['날짜', 'hour_int']).reset_index(drop=True)
    
    # 결측치 처리
    df_melted['kWh'] = pd.to_numeric(df_melted['kWh'], errors='coerce').fillna(0)
    
    return df_melted

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    if not MODEL or not SCALER:
        raise HTTPException(status_code=500, detail="모델 로드 필요")

    try:
        contents = await file.read()
        # CSV 읽기 (인코딩 대응)
        try:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='cp949')
        except:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='utf-8')
            
        df_proc = preprocess(df_raw)
        
        # --- 1. 최근 한 달(30일) 데이터 분석 ---
        # 30일 * 24시간 = 최근 720개 행 추출
        last_month_df = df_proc.tail(720) 
        
        # 최근 한 달간의 "시간별 평균" 사용량 (01:00 ~ 24:00 패턴)
        month_avg_by_hour = last_month_df.groupby('시간', sort=False)['kWh'].mean().tolist()

        # --- 2. 재귀적(Recursive) 예측 로직 적용 ---
        # 모델이 1시간만 예측하는 구조일 경우, 예측값을 다시 입력에 넣어 24번 반복합니다.
        scaled_data = SCALER.transform(df_proc[['kWh']])
        
        # 최근 24시간 데이터를 시작 입력값으로 설정
        current_batch = scaled_data[-24:].reshape(1, 24, 1)
        predictions_scaled = []

        for _ in range(24):
            # 다음 1시간 예측
            next_pred = MODEL.predict(current_batch, verbose=0) # 결과: [[value]]
            
            # 예측값 저장 (배열 형태에 따라 인덱스 조정 필요할 수 있음)
            # 보통 (1, 1) 혹은 (1, 24) 중 첫 번째 값을 가져옴
            val = next_pred[0, 0] if next_pred.ndim > 1 else next_pred[0]
            predictions_scaled.append(val)
            
            # 윈도우 업데이트: 가장 오래된 값을 버리고 방금 예측한 값을 끝에 추가
            next_val_reshaped = np.array(val).reshape(1, 1, 1)
            current_batch = np.append(current_batch[:, 1:, :], next_val_reshaped, axis=1)

        # 원래 단위로 복구 (inverse_transform)
        predictions_scaled = np.array(predictions_scaled).reshape(-1, 1)
        next_period_pred_rescaled = SCALER.inverse_transform(predictions_scaled).flatten().tolist()
        
        # 음수 제거 및 타입 변환
        next_period_pred = [max(0, float(v)) for v in next_period_pred_rescaled]

        return {
            "lastMonthAvg": month_avg_by_hour,   # 최근 한 달 시간별 평균 패턴
            "nextMonthPred": next_period_pred,  # 재귀적으로 생성된 24시간 예측 패턴
            "status": "Monthly Analysis Success"
        }
        
    except Exception as e:
        import traceback
        traceback.print_exc() # 서버 터미널에 상세 에러 출력
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)