import React from 'react';

export default function FeatureCard({ label, title }) {
  return (
    <div className="signup-feature-card">
      <div className="signup-feature-label">{label}</div>
      <div className="signup-feature-title">{title}</div>
    </div>
  );
}
