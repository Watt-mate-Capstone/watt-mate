// 3. 마이페이지
import React from 'react';
import '../css/MyPage.css';

const MyPage = ({ onNavigate }) => {
  const handleLogout = () => {
    alert('안전하게 로그아웃 되었습니다.');
    onNavigate('login');
  };

  return (
    <>
      <header>
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          <img src="favicon.svg" alt="와트메이트 로고" style={{ width: '40px', height: '40px' }} />
          <h2>와트메이트</h2>
        </div>
        <div className="nav-links">
          <span onClick={() => onNavigate('main')}>대시보드</span>
          <span style={{ fontWeight: 900, color: '#B4C6B6' }}>내 정보</span>
          <span onClick={handleLogout}>로그아웃</span>
        </div>
      </header>

      <div className="container" style={{ marginTop: '120px' }}>
        <div className="page-title">내 정보 관리</div>

        <div className="mypage-grid">
          <div className="card">
            <h3>기본 프로필</h3>
            <div className="profile-section">
              <div className="profile-img-circle">👤</div>
              <div className="user-name">OOO</div>
              <div className="user-role">일반 회원</div>
            </div>
            <div className="info-row">
              <span className="info-label">거주 지역</span>
              <span className="info-value">광주광역시</span>
            </div>
            <div className="info-row">
              <span className="info-label">주거 형태</span>
              <span className="info-value">1인 가구</span>
            </div>
            <button className="btn">프로필 수정하기</button>
          </div>

          <div className="right-column">
            <div className="card" style={{ minHeight: 'auto' }}>
              <h3>나의 에너지 목표 설정</h3>
              <div className="info-row">
                <span className="info-label">이번 달 목표 전력 사용량</span>
                <span className="info-value">150 kWh</span>
              </div>
              <div className="info-row">
                <span className="info-label">예측 대비 절감 목표</span>
                <span className="info-value" style={{ color: '#e53935' }}>-10%</span>
              </div>
              <div className="info-row">
                <span className="info-label">푸시 알림 수신</span>
                <span className="info-value">켜짐</span>
              </div>
              <button className="btn btn-secondary">목표 재설정</button>
            </div>

            <div className="card" style={{ minHeight: 'auto' }}>
              <h3>리워드 및 활동 내역</h3>
              <div className="info-row">
                <span className="info-label">누적 와트 포인트</span>
                <span className="info-value" style={{ color: '#4CAF50', fontSize: '32px' }}>1,500 P</span>
              </div>
              <div className="info-row">
                <span className="info-label">성공한 미션 횟수</span>
                <span className="info-value">총 4회</span>
              </div>
              <div className="info-row">
                <span className="info-label">현재 해당 가구 랭킹 등급</span>
                <span className="info-value" style={{ color: '#FFC107' }}>B등급 (상위 30%)</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

export default MyPage;
