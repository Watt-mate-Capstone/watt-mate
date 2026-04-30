import React from 'react';

export default function PredictionChart() {
  return (
    <section className="card chart-section">
      <h3>📊 AI 전력 소비 예측 분석</h3>
      <div className="chart-placeholder">
        [ LSTM 예측 그래프 영역: 실제 사용량 vs AI 예측량 ]
      </div>
      <div className="ai-tip">
        <strong>💡 와트메이트 AI 분석:</strong>
        어제보다 낮 시간 사용량이 15% 줄었습니다! 이대로 유지하면 이번 달 예상 요금을 방어할 수 있어요.
      </div>
    </section>
  );
}