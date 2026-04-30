import React from 'react';

export default function InsightCard() {
  return (
    <div className="card">
      <h3>💡 오늘의 절전 인사이트</h3>
      <ul className="tip-list">
        <li>
          <span className="tip-title">대기전력만 잡아도 한 달 커피값이?</span>
          <span className="tip-desc">자주 안 쓰는 가전제품 플러그 뽑기, 생각보다 효과가 큽니다. AI가 분석한 대기전력 절감 리포트를 확인해보세요.</span>
        </li>
        <li>
          <span className="tip-title">봄철 환기할 때 공기청정기는 잠시 OFF</span>
          <span className="tip-desc">창문을 열고 환기할 때 공기청정기가 풀가동되면 전력 소모가 극심해집니다. 효율적인 실내 공기 관리법을 소개합니다.</span>
        </li>
      </ul>
    </div>
  );
}