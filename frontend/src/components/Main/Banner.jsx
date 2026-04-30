import React from 'react';

export default function Banner() {
  return (
    <div className="banner">
      <div>
        <h1>지구를 구하는 작은 습관 🌍</h1>
        <p>이번 달 와트메이트 사용자들이 절약한 총 전력량은 <strong>~ Wh</strong> 입니다.</p>
      </div>
      <div style={{ fontSize: '60px', opacity: 0.8 }}>⚡</div>
    </div>
  );
}