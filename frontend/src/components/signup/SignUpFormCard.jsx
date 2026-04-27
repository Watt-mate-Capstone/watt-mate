import React from 'react';
import FormField from './FormField';
import LocationSelector from './LocationSelector';

export default function SignUpFormCard({
  form,
  onChange,
  onCheckDuplicate,
  onSubmit,
  onNavigate,
  onChangeRegion,
  onChangeLocation,
}) {
  return (
    <form className="signupform-card" onSubmit={onSubmit}>
      <div className="signupform-card-top">
        <div className="signupform-card-badge">회원가입</div>
        <h2 className="signupform-card-title">정보를 입력해주세요</h2>
        <p className="signupform-card-subtitle">
          와트메이트 서비스를 시작하기 위한 기본 정보입니다.
        </p>
      </div>

      <div className="signupform-grid">
        <FormField
          label="이름"
          name="name"
          placeholder="이름을 입력하세요"
          value={form.name}
          onChange={onChange}
          full
        />

        <FormField
          label="이메일"
          name="email"
          type="email"
          placeholder="example@email.com"
          value={form.email}
          onChange={onChange}
          full
        />

        <FormField label="아이디" full>
          <div className="signupform-inline-field">
            <input
              type="text"
              name="username"
              placeholder="아이디를 입력하세요"
              value={form.username}
              onChange={onChange}
            />
            <button
              type="button"
              className="signupform-check-btn"
              onClick={onCheckDuplicate}
            >
              중복확인
            </button>
          </div>
        </FormField>

        <FormField
          label="비밀번호"
          name="password"
          type="password"
          placeholder="비밀번호 입력"
          value={form.password}
          onChange={onChange}
        />

        <FormField
          label="비밀번호 확인"
          name="confirmPassword"
          type="password"
          placeholder="비밀번호 다시 입력"
          value={form.confirmPassword}
          onChange={onChange}
        />

        <FormField label="거주 지역" full>
          <LocationSelector
            region={form.region}
            onChangeRegion={onChangeRegion}
            onChangeLocation={onChangeLocation}
          />
        </FormField>

        <FormField label="가구 유형">
          <select
            name="houseType"
            value={form.houseType}
            onChange={onChange}
          >
            <option value="">가구 유형 선택</option>
            <option value="1인 가구">1인 가구</option>
            <option value="2인 가구">2인 가구</option>
            <option value="3인~4인 가구">3인~4인 가구</option>
            <option value="5인 이상 가구">5인 이상 가구</option>
          </select>
        </FormField>

        <FormField label="가구 정보 인증 방식">
          <select
            name="houseVerifyMethod"
            value={form.houseVerifyMethod}
            onChange={onChange}
          >
            <option value="">인증 방식 선택</option>
            <option value="증빙서류 업로드">증빙서류 업로드</option>
            <option value="주민등록등본 인증">주민등록등본 인증</option>
            <option value="공공 마이데이터 연동">공공 마이데이터 연동</option>
            <option value="한전 고객정보 연동">한전 고객정보 연동</option>
          </select>
        </FormField>

        <FormField label="가구 유형 증빙자료" full>
          <input
            type="file"
            name="houseProofFile"
            accept=".jpg,.jpeg,.png,.pdf"
            onChange={onChange}
          />
        </FormField>
      </div>

      <label className="signupform-agree">
        <input
          type="checkbox"
          name="agree"
          checked={form.agree}
          onChange={onChange}
        />
        <span>개인정보 수집 및 서비스 이용약관에 동의합니다.</span>
      </label>

      <div className="signupform-buttons">
        <button
          type="button"
          className="signupform-back-btn"
          onClick={() => onNavigate('signup')}
        >
          뒤로가기
        </button>

        <button type="submit" className="signupform-submit-btn">
          가입하기
        </button>
      </div>

      <div className="signupform-login-link">
        이미 계정이 있으신가요?{' '}
        <button type="button" onClick={() => onNavigate('login')}>
          로그인하기
        </button>
      </div>
    </form>
  );
}