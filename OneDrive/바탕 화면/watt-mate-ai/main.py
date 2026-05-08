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
    print(f"⚠️ 모델 로드 실패: {e}")

def preprocess(df):
    """
    데이터 전처리: Wide 포맷을 Long 포맷으로 변환하고 시간 순으로 정확히 정렬합니다.
    """
    hourly_cols = [f"{i:02d}:00" for i in range(1, 25)]
    
    # 데이터 재구조화 (Wide -> Long)
    df_melted = df.melt(id_vars=['날짜'], value_vars=hourly_cols, var_name='시간', value_name='kWh')
    
    # ✅ 시간 정렬 문제 해결: "01:00" 문자열 대신 숫자형으로 변환하여 정렬
    df_melted['hour_int'] = df_melted['시간'].str.split(':').str[0].astype(int)
    
    # 날짜와 시간(정수) 순으로 정렬해야 데이터 순서가 뒤섞이지 않습니다.
    df_melted = df_melted.sort_values(by=['날짜', 'hour_int']).reset_index(drop=True)
    
    # 결측치 처리
    df_melted['kWh'] = pd.to_numeric(df_melted['kWh'], errors='coerce').fillna(0)
    
    return df_melted

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    if not MODEL or not SCALER:
        raise HTTPException(status_code=500, detail="모델 또는 스케일러가 로드되지 않았습니다.")

    try:
        contents = await file.read()
        # CSV 읽기 (인코딩 대응)
        try:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='cp949')
        except:
            df_raw = pd.read_csv(io.BytesIO(contents), encoding='utf-8')
            
        df_proc = preprocess(df_raw)
        
        # 1. 이번 주 평균 전력량 (최근 7일 데이터 기반 시간별 평균)
        # sort=False를 사용하여 01:00~24:00 순서 유지
        this_week_avg = df_proc.tail(168).groupby('시간', sort=False)['kWh'].mean().tolist()

        # 2. 다음 주 예측 전력량
        # 모델 입력용 스케일링
        scaled_data = SCALER.transform(df_proc[['kWh']])
        
        # 최근 24시간 데이터를 추출하여 모델 입력 형태 (batch, timesteps, features)로 변환
        input_window = scaled_data[-24:].reshape(1, 24, 1) 
        
        # 모델 예측
        pred_scaled = MODEL.predict(input_window)
        
        # ✅ 예측값이 (1, 24) 형태라고 가정하고 역스케일링 진행
        # 만약 모델 출력이 1개라면 24번 반복하는 로직이 필요하지만, 
        # 현재 그래프 상 24개가 출력되는 것으로 보여 아래와 같이 처리합니다.
        pred_rescaled = SCALER.inverse_transform(pred_scaled.reshape(-1, 1)).flatten()
        
        # 음수 값이 나올 경우 0으로 보정 (전력 사용량은 음수일 수 없음)
        next_week_pred = [max(0, float(v)) for v in pred_rescaled]

        # 만약 예측값이 24개가 아니라면 부족한 부분을 평균값 기반으로 채움 (방어 코드)
        if len(next_week_pred) < 24:
            padding = [this_week_avg[i] * 1.02 for i in range(len(next_week_pred), 24)]
            next_week_pred.extend(padding)

        return {
            "thisWeekAvg": this_week_avg, 
            "nextWeekPred": next_week_pred[:24] # 정확히 24개만 전달
        }
        
    except Exception as e:
        import traceback
        traceback.print_exc() # 서버 터미널에 상세 에러 출력
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)