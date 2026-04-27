import React from 'react';

const infoItems = [
  { icon: '⚡', text: 'AI 기반 전력 소비 분석' },
  { icon: '🎁', text: '절감 성과에 따른 리워드 지급' },
  { icon: '🏆', text: '지역별 랭킹 및 명예의 전당' },
];

export default function SignUpFormIntro() {
  return (
    <div className="signupform-left">
      <div className="signupform-badge">WattMate Sign Up</div>

      <h1 className="signupform-title">
        우리 집 에너지 절약의
        <br />
        첫 시작
      </h1>

      <p className="signupform-description">
        지역과 가구 정보를 바탕으로
        <br />
        맞춤형 절감 분석과 리워드를 제공합니다.
      </p>

      <div className="signupform-info-box">
        {infoItems.map((item) => (
          <div className="signupform-info-item" key={item.text}>
            <span className="signupform-info-icon">{item.icon}</span>
            <span>{item.text}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
