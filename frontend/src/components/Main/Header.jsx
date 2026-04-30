import React, { useState, useEffect } from 'react';

export default function Header({ onNavigate }) {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  useEffect(() => {
    const handleClickOutside = () => setIsDropdownOpen(false);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  const handleLogout = () => {
    alert('안전하게 로그아웃 되었습니다.');
    onNavigate('login');
  };

  return (
    <header>
      <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
        <img src="favicon.svg" alt="와트메이트 로고" style={{ width: '36px', height: '36px' }} />
        <h2>와트메이트</h2>
      </div>
      <div className="profile-wrapper">
        <div
          className="profile-icon"
          onClick={(e) => {
            e.stopPropagation();
            setIsDropdownOpen(!isDropdownOpen);
          }}
        >
          <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="#333" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            <circle cx="12" cy="7" r="4"></circle>
          </svg>
        </div>
        <div className={`dropdown-menu ${isDropdownOpen ? 'show' : ''}`}>
          <span onClick={() => onNavigate('mypage')}>내 정보</span>
          <span onClick={handleLogout}>로그아웃</span>
        </div>
      </div>
    </header>
  );
}