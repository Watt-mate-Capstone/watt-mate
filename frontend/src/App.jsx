import React, { useState } from 'react'; // { useState } 를 추가!
import './App.css';
import Login from './pages/Login.jsx';
import Main from './pages/Main.jsx';
import MyPage from './pages/MyPage.jsx';
import SignUp from './pages/SignUp.jsx';
import SignUpForm from './pages/SignUpForm.jsx';

export default function App() {
  const [currentPage, setCurrentPage] = useState('signup');

  const handleNavigate = (page) => {
    setCurrentPage(page);
    window.scrollTo(0, 0); // 페이지 이동 시 맨 위로 스크롤
  };

  return (
    <div className="app-container">
      {/* 상태(currentPage)에 따라 다른 컴포넌트를 보여줍니다 */}
      {currentPage === 'signupform' && <SignUpForm onNavigate={handleNavigate} />}
      {currentPage === 'signup' && <SignUp onNavigate={handleNavigate} />}
      {currentPage === 'login' && <Login onNavigate={handleNavigate} />}
      {currentPage === 'main' && <Main onNavigate={handleNavigate} />}
      {currentPage === 'mypage' && <MyPage onNavigate={handleNavigate} />}
    </div>
  );
}