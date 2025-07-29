# 🌱 WeCanFarm - AI 농업 도우미 앱
> 농부들을 위한 스마트한 농업 관리 솔루션
>
> AI 작물 진단부터 농산물 마켓까지 한 번에! 📱🌾

## 📋 프로젝트 개요
WeCanFarm은 현대 주말 농장을 운영하는 초보 농부들이 겪을 어려움을 줄여주고자 작물의 병충해 현황 및 솔루션을 제공해주는 애플리케이션입니다.
스마트폰 카메라를 통한 AI 작물 질병 진단, 실시간 농장 관리 대시보드, 농산물 직거래 마켓 등 
농부들이 필요로 하는 모든 기능을 하나의 앱에서 제공합니다.

## ✨ 주요 기능

### 🔍 AI 작물 진단
- **실시간 카메라 분석**: 스마트폰으로 작물을 촬영하면 즉시 AI 진단
- **질병 감지**: CNN 기반 딥러닝 모델로 정확한 질병 분류
- **맞춤형 처방**: 질병별 구체적인 치료법과 관리 방법 제시
- **진단 기록 저장**: 모든 검사 결과를 자동으로 저장하여 이력 관리

### 📊 스마트 농장 대시보드
- **실시간 현황**: 농장 전체 작물 상태를 한눈에 확인
- **통계 분석**: AI 진단 횟수, 건강한 작물 비율 등 데이터 시각화
- **진단 히스토리**: 과거 검사 기록과 이미지를 카드 형태로 표시
- **성장 단계 추적**: 파종부터 수확까지 작물별 생육 단계 관리

### 🛒 농산물 마켓플레이스
- **직거래 플랫폼**: 농부와 소비자를 직접 연결하는 중간 유통업체 없는 거래
- **상품 등록**: 간단한 폼으로 농산물 판매 등록
- **카테고리별 검색**: 채소, 과일, 곡물 등 분류별 상품 탐색
- **신선도 보장**: 농장에서 바로 배송하는 신선한 농산물

### 👤 사용자 맞춤 서비스
- **농부/소비자 구분**: 사용자 유형에 따른 차별화된 화면 제공
- **온보딩 시스템**: 앱 기능을 쉽게 이해할 수 있는 단계별 안내
- **세션 관리**: 안전한 로그인 상태 유지 및 개인정보 보호

## 🚀 Tech Stack

### Frontend (Android)
- **Kotlin** + **Jetpack Compose**
- **Material Design 3**
- **Coil** (이미지 로딩)
- **Camera API** (카메라 기능)

### Data & Storage
- **Gson** (JSON 파싱)
- **Base64 이미지 인코딩**
- **In-Memory Storage** (현재)
- **Firebase** (향후 연동 예정)

### Network & API
- **RESTful API**
- **HttpURLConnection**
- **JWT 토큰 인증**

### 필수 요구사항
```
Android Studio Arctic Fox 이상
Kotlin 1.9.0+
Minimum SDK: API 24 (Android 7.0)
Target SDK: API 34 (Android 14)
```

### 프로젝트 설정
```bash
# 프로젝트 클론
git clone https://github.com/yourusername/WeCanFarm-Android.git
cd WeCanFarm-Android

# 의존성 설치
./gradlew build

# 앱 실행 (기기 연결 또는 에뮬레이터)
./gradlew installDebug
```

### 🔑 서버 설정
`MainActivity.kt`에서 서버 URL을 수정하세요:
```kotlin
val serverUrl = "https://your-server-url.ngrok-free.app"
```

## 📁 프로젝트 구조
'''
📁 com/example/cv_project2_test/
├── 📱 ui/
│   ├── 🎨 onboarding/
│   │   ├── OnBoardingScreen.kt
│   │   └── OnBoarding2Screen.kt  
│   ├── 🔐 auth/
│   │   ├── LoginScreen.kt
│   │   └── SignUpScreen.kt
│   ├── 🏠 farmer/
│   │   └── FarmerDashboardScreen.kt
│   ├── 🔍 plant/
│   │   └── PlantCheckScreen.kt
│   └── 🛒 market/
│       ├── MarketScreen.kt
│       └── ProductRegisterScreen.kt
├── 📊 data/
│   ├── model/                      # 데이터 모델들
│   ├── session/                    # 세션 관리
│   └── history/                    # 진단 기록 관리  
├── 🌐 network/                     # API 통신
├── 📷 camera/                      # 카메라 관련
└── 🛠️ utils/                       # 유틸리티

'''
## 📱 주요 화면 및 기능

### 1. 온보딩 (OnBoarding.kt, OnBoarding2.kt)
```
🎯 앱 소개 → 기능 설명 → 로그인 유도
- 앱의 가치 제안 명확히 전달
- 주요 기능 3가지 소개 (진단, 마켓, 커뮤니티)
- 스킵 가능한 2단계 온보딩
```

### 2. 로그인/회원가입 (MainActivity.kt)
```
🔐 사용자 인증 시스템
- 사용자명/비밀번호 로그인
- 농부/일반사용자 역할 구분
- JWT 토큰 기반 세션 관리
```

### 3. 농부 대시보드 (Farmer.kt)
```
📊 농장 현황 한눈에 보기
- 실시간 AI 진단 기록 피드
- 농장 통계 (진단 횟수, 건강한 작물 수)
- 작물별 생육 상태 카드
- 최근 활동 타임라인
```

### 4. AI 작물 진단 (PlantCheck.kt)
```
🔍 스마트폰으로 즉시 진단
- 실시간 카메라 프리뷰
- 원터치 촬영 및 AI 분석
- 질병명, 신뢰도, 치료법 제시
- 자동 기록 저장 및 이력 관리
```

### 5. 농산물 마켓 (MarketScreen.kt)
```
🛒 신선한 농산물 직거래
- 카테고리별 상품 분류
- 상품 상세 정보 및 이미지
- 판매자 정보 및 평점
- 장바구니 및 주문 시스템 (개발 예정)
```

## 🔄 앱 플로우

```
📱 앱 실행
    ↓
🎯 온보딩 (앱 소개)
    ↓
🔐 로그인/회원가입
    ↓
👤 사용자 유형 확인
    ↓
┌─────────────────┬─────────────────┐
│   🌾 농부 모드    │   👤 일반 모드    │
├─────────────────┼─────────────────┤
│ 📊 농부 대시보드   │ 🛒 마켓 화면     │
│ 🔍 작물 진단     │ 🔍 작물 진단     │
│ 🛒 마켓 이동     │ 📋 구매 내역     │
└─────────────────┴─────────────────┘
```


### 코딩 컨벤션
- **Kotlin**: 공식 스타일 가이드 준수
- **Compose**: 함수명 PascalCase 사용
- **주석**: 한국어로 작성 (코드는 영어)
- **네이밍**: 의미 있고 명확한 변수명 사용


## 🚀 주요 기
### ✅ 현재 구현된 기능

| 기능 | 설명 | 상태 |
|------|------|------|
| 🎨 온보딩 | 앱 소개 및 기능 안내 | ✅ 완료 |
| 🔐 로그인/회원가입 | 사용자 인증 시스템 | ✅ 완료 |
| 🏠 농부 대시보드 | 농장 현황 및 통계 | ✅ 완료 |
| 📸 AI 작물 진단 | 실시간 카메라 + AI 분석 | ✅ 완료 |
| 📋 진단 기록 관리 | 검사 이력 저장 및 조회 | ✅ 완료 |
| 🛒 마켓 기본 구조 | 상품 목록 및 등록 | ✅ 완료 |

### 🚧 개발 예정 기능

- 🔥 Firebase 연동 (영구 데이터 저장)
- 💬 농업 커뮤니티 및 채팅
- 🔔 푸시 알림 시스템
- 📊 고급 농장 분석 대시보드
- 🌍 날씨 및 농업 정보 연동


## 🛠️ 기술 스택

### Frontend (Android)
- **언어**: Kotlin
- **UI 프레임워크**: Jetpack Compose
- **아키텍처**: MVVM Pattern
- **상태 관리**: Compose State
- **이미지 로딩**: Coil
- **네트워크**: HttpURLConnection + Gson
- **카메라**: Android Camera API

### 개발 도구
- **IDE**: Android Studio
- **버전 관리**: Git & GitHub
- **API 테스트**: Postman

## 📦 설치 및 실행

### 필수 요구사항
- Android Studio Arctic Fox 이상
- Kotlin 1.9.0+
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)

### 설치 방법

1. **저장소 클론**
```bash
git clone https://github.com/yourusername/WeCanFarm-Android.git
cd WeCanFarm-Android
```

2. **Android Studio에서 프로젝트 열기**
- Android Studio 실행
- "Open an existing project" 선택
- 클론한 폴더 선택

3. **의존성 설치**
```bash
./gradlew build
```

4. **앱 실행**
- Android 기기 연결 또는 에뮬레이터 실행
- Run 버튼 클릭 또는 `Ctrl + R`

### 🔧 설정

#### 서버 URL 변경
`MainActivity.kt`에서 서버 URL을 수정하세요:
```kotlin
val serverUrl = "YOUR_SERVER_URL_HERE"
```

#### 카메라 권한
앱에서 카메라 사용을 위해 권한이 필요합니다. 첫 실행 시 자동으로 권한을 요청합니다.

## 📖 API 문서

### 인증 API
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "farmuser",
  "password": "password123"
}
```

### AI 분석 API
```http
POST /api/analyze
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "image_base64": "base64_encoded_image"
}
