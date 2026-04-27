import React from 'react';

export default function SignUpGuideCard({ onNavigate }) {
  return (
    <div className="signup-card">
      <div className="signup-card-badge">회원가입 안내</div>

      <div className="signup-card-title">
        와트메이트와 함께 에너지 절약을 시작해보세요
      </div>

      <div className="signup-card-description">
        회원가입 후 우리 집 전력 사용량을 확인하고, AI 분석과 절감 리워드를 받아볼 수 있어요.
      </div>

      <div className="signup-info-box">
        <div className="signup-info-title">회원가입 후 입력하는 정보</div>
        <div className="signup-info-text">
          · 이름 / 이메일 / 휴대폰 번호
          <br />
          · 거주 지역 정보
          <br />
          · 가구 유형 및 가구원 수
        </div>
      </div>

      <button
        className="signup-submit-btn"
        onClick={() => onNavigate('signupform')}
      >
        회원가입
      </button>

      <div className="signup-login-text">
        이미 계정이 있나요?
        <button
          type="button"
          onClick={() => onNavigate('login')}
          className="signup-login-btn"
        >
          로그인
        </button>
      </div>
    </div>
  );
}
