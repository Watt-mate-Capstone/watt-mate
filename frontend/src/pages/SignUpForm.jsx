import React, { useState } from 'react';
import '../css/SignUpForm.css';
import SignUpFormIntro from '../components/signup/SignUpFormIntro';
import SignUpFormCard from '../components/signup/SignUpFormCard';

export default function SignUpForm({ onNavigate }) {

  const [form, setForm] = useState({
    name: '',
    email: '',
    username: '',
    password: '',
    confirmPassword: '',
    region: '',
    houseType: '',
    houseVerifyMethod: '',
    houseProofFile: null,
    agree: false,
  });

  const handleChange = (e) => {
    const { name, value, type, checked, files } = e.target;

    setForm((prev) => ({
      ...prev,
      [name]:
        type === 'checkbox'
          ? checked
          : type === 'file'
          ? files[0]
          : value,
    }));
  };

  const handleCheckDuplicate = () => {
    if (!form.username.trim()) {
      alert('아이디를 입력해주세요.');
      return;
    }
    alert('사용 가능한 아이디입니다.');
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (!form.username || !form.password) {
      alert('아이디와 비밀번호 입력하세요');
      return;
    }

    if (form.password !== form.confirmPassword) {
      alert('비밀번호가 다릅니다');
      return;
    }

    alert('회원가입 완료!');
    onNavigate('login');
  };

  return (
    <div className="signupform-page">
      <div className="signupform-container">
        <SignUpFormIntro />

        <SignUpFormCard
          form={form}
          onChange={handleChange}
          onCheckDuplicate={handleCheckDuplicate}
          onSubmit={handleSubmit}
          onNavigate={onNavigate}
          onChangeRegion={(region) => {
            setForm((prev) => ({
              ...prev,
              region,
            }));
          }}
          onChangeLocation={({ lat, lon, region }) => {
            setForm((prev) => ({
              ...prev,
              lat,
              lon,
              region,
            }));
          }}
        />

      </div>
    </div>
  );
}
