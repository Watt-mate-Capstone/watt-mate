import React, { useState, useEffect } from 'react';
import './App.css';

// 1. 로그인
const Login = ({ onNavigate }) => {
  const [id, setId] = useState('');
  const [pw, setPw] = useState('');

  const handleLogin = () => {
    if (id === 'user' && pw === '1234') {
      alert('관리자님 환영합니다!');
      onNavigate('main');
    } else {
      alert('아이디 또는 비밀번호가 일치하지 않습니다.');
    }
  };

  return (
    <div className="login-page">
      <div className="login-container">
        <div className="header-section">
          <div className="subtitle">에너지가 가치가 되는 순간</div>
          <div className="title-wrapper">
            <img src="favicon.svg" className="logo-icon" alt="와트메이트 로고" />
            <h1 className="main-title">와트메이트</h1>
          </div>
        </div>

        <div className="form-section">
          <input
            type="text"
            className="input-field"
            placeholder="아이디"
            value={id}
            onChange={(e) => setId(e.target.value)}
          />
          <input
            type="password"
            className="input-field"
            placeholder="비밀번호"
            value={pw}
            onChange={(e) => setPw(e.target.value)}
          />
          <button onClick={handleLogin} className="login-button">
            와트메이트 로그인
          </button>
        </div>
      </div>
    </div>
  );
};

// 2. 메인페이지
const Main = ({ onNavigate }) => {
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
    <>
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

      <div className="container">
        <div className="banner">
          <div>
            <h1>지구를 구하는 작은 습관 🌍</h1>
            <p>이번 달 와트메이트 사용자들이 절약한 총 전력량은 <strong>~ Wh</strong> 입니다.</p>
          </div>
          <div style={{ fontSize: '60px', opacity: 0.8 }}>⚡</div>
        </div>

        <div className="main-grid">
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

          <div className="card">
            <h3>🏆 이번 달 명예의 전당</h3>
            <p style={{ fontSize: '18px', color: '#888', marginTop: '-15px', marginBottom: '25px' }}>
              가장 많은 에너지를 절약한 이웃들입니다.
            </p>
            <ul className="hof-list">
              <li><span className="hof-rank">1</span><span className="hof-name">김성진</span><span className="hof-score">-18% 절감</span></li>
              <li><span className="hof-rank">2</span><span className="hof-name">박재용</span><span className="hof-score">-15% 절감</span></li>
              <li><span className="hof-rank">3</span><span className="hof-name">이건양</span><span className="hof-score">-12% 절감</span></li>
              <li><span className="hof-rank">4</span><span className="hof-name">김도형</span><span className="hof-score">-9% 절감</span></li>
              <li><span className="hof-rank">5</span><span className="hof-name">김성준</span><span className="hof-score">-6% 절감</span></li>
            </ul>
          </div>
        </div>

        <section className="summary-grid">
          <div className="card">
            <h3 style={{ color: '#666', fontWeight: 700, fontSize: '22px' }}>실시간 전력 사용량</h3>
            <div className="value">0.45<span className="unit">kWh</span></div>
          </div>
          <div className="card">
            <h3 style={{ color: '#666', fontWeight: 700, fontSize: '22px' }}>이번 달 예상 요금</h3>
            <div className="value">38,200<span className="unit">원</span></div>
          </div>
          <div className="card">
            <h3 style={{ color: '#666', fontWeight: 700, fontSize: '22px' }}>보유 리워드 포인트</h3>
            <div className="value" style={{ color: '#4CAF50' }}>1,500<span className="unit">P</span></div>
          </div>
        </section>

        <section className="card chart-section">
          <h3>📊 AI 전력 소비 예측 분석</h3>
          <div className="chart-placeholder">
            [ LSTM 예측 그래프 영역: 실제 사용량 vs AI 예측량 ]
          </div>
          <div className="ai-tip">
            <strong>💡 와트메이트 AI 분석:</strong>
            어제보다 낮 시간 사용량이 15% 줄었습니다! 이대로 유지하면 이번 달 예상 요금을 방어할 수 있어요.
          </div>
        </section>
      </div>
    </>
  );
};

// 3. 마이페이지
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


export default function App() {
  const [currentPage, setCurrentPage] = useState('login');

  const handleNavigate = (page) => {
    setCurrentPage(page);
    window.scrollTo(0, 0); // - 페이지 이동할 때 맨 위쪽으로 스크롤 하는 부분!!!
  };

  return (
    <div className="app-container">
      {currentPage === 'login' && <Login onNavigate={handleNavigate} />}
      {currentPage === 'main' && <Main onNavigate={handleNavigate} />}
      {currentPage === 'mypage' && <MyPage onNavigate={handleNavigate} />}
    </div>
  );
}