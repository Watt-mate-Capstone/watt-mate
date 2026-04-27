import React from 'react';
import FeatureCard from './FeatureCard';

const features = [
  { label: '실시간 분석', title: '전력 사용량 확인' },
  { label: '맞춤 가이드', title: 'AI 절약 팁 제공' },
  { label: '리워드 시스템', title: '절감 포인트 적립' },
];

export default function SignUpIntro() {
  return (
    <div className="signup-left">
      <div className="signup-brand">
        <div className="signup-logo">
          <div className="signup-logo-diamond"></div>
          <div className="signup-logo-bolt"></div>
        </div>

        <div>
          <div className="signup-brand-subtitle">에너지가 가치가 되는 순간</div>
          <div className="signup-brand-title">와트메이트</div>
        </div>
      </div>

      <div className="signup-main-title">
        우리 동네에서 시작하는
        <br />
        에너지 절약 습관
      </div>

      <div className="signup-description">
        AI 기반 에너지 절감 리워드 서비스로
        <br />
        전력 사용을 똑똑하게 관리하고, 절감한 만큼 보상까지 받아보세요.
      </div>

      <div className="signup-feature-list">
        {features.map((feature) => (
          <FeatureCard
            key={feature.label}
            label={feature.label}
            title={feature.title}
          />
        ))}
      </div>
    </div>
  );
}
