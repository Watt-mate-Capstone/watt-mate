# 🚀 Project Workflow Guide

이 가이드는 터미널을 사용하여 저장소를 설정하고, 수정된 코드를 안전하게 반영하는 방법을 설명합니다.

---

## 🛠 1. 초기 설정 및 실행

### ① 저장소 가져오기 (처음 시작할 때)
현재 폴더에 `frontend` 브랜치 내용을 복사해옵니다.
```bash
git clone -b frontend [https://github.com/Watt-mate-Capstone/watt-mate.git](https://github.com/Watt-mate-Capstone/watt-mate.git) .
② 디렉토리 이동 및 브랜치 확인

# frontend 폴더로 이동 (이미 폴더가 있는 경우)
cd frontend

# 현재 브랜치 확인
git branch

# 필요한 경우 브랜치 이동
git checkout [브랜치 이름]
③ 최신 데이터 동기화 및 실행

# 원격 저장소의 최신 데이터 가져오기
git pull origin frontend

# VS Code 실행
code .

# Docker를 이용한 React 실행
docker compose up
접속 주소: http://localhost:5173/watt-mate/

⬆️ 2. 수정 사항 반영하기 (Push)
코드를 수정한 후 서버에 올릴 때는 아래 순서를 따릅니다. Push 전에는 항상 신중하게 확인하세요!


# 1. 변경된 파일 목록 확인
git status

# 2. 변경 사항 스테이징 (장바구니에 담기)
git add .

# 3. 커밋 생성 (로컬 저장소에 기록)
git commit -m "수정 내용 메시지 작성"

# 4. 원격 서버로 전송
git push origin frontend
💡 Push 에러 발생 시: 서버에 다른 변경 사항이 먼저 올라온 경우입니다. git pull origin frontend를 먼저 수행하여 충돌(Conflict)을 해결한 뒤 다시 Push 하세요.

⏪ 3. 작업 되돌리기 (Reset)
상황에 따라 커밋을 취소해야 할 때 사용합니다.

커밋만 취소 (작업한 코드는 보존)

git reset --soft HEAD~1
커밋과 코드 모두 삭제 (완전 초기화)

git reset --hard HEAD~1
이미 Push까지 완료한 경우 (강제 업데이트)
주의: 협업 중에는 팀원과 상의 후 사용하세요.


git push origin frontend --force

---
