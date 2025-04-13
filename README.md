# Camera Filter App

이 프로젝트는 OpenGL ES와 Android Camera2 API를 기반으로, 실시간으로 필터(흑백 및 밝기 증가)를 적용하는 카메라 애플리케이션입니다.

---

## 🔧 기술 스택 및 아키텍처

- **Architecture**: MVVM + Repository 패턴
- **Camera API**: Android Camera2 API
- **OpenGL 처리**: GLSurfaceView + SurfaceTexture + Shader 기반 실시간 렌더링
- **Shader 파일 관리**: `assets/shaders` 디렉터리

---

## 📁 프로젝트 구조


```
📁 src
├── 📁 assets
│   └── 📁 shaders
│       ├── default.vs             // 공통 Vertex Shader
│       ├── original.fs            // 원본 필터 Fragment Shader
│       ├── grayscale.fs           // 흑백 필터 Fragment Shader
│       └── brightness.fs          // 밝기 증가 필터 Fragment Shader

📦 com.example.camera_filter_app
├── 📁 camera
│   ├── CameraHandler.java         // Camera2 API를 이용한 카메라 제어 클래스
│   └── CameraFilterProcessor.java // CameraRenderer와 CameraHandler를 연결하는 중심 처리 클래스
│
├── 📁 gl
│   └── CameraRenderer.java        // OpenGL ES 기반 영상 렌더링 및 필터 셰이더 적용
│
├── 📁 model
│   └── FilterType.java            // 필터 타입을 정의하는 enum 클래스
│
├── 📁 ui
│   └── 📁 filter
│       └── FilterActivity.java    // UI 구성 및 ViewModel과 연동되는 필터 액티비티
│
├── 📁 viewmodel
│   └── FilterViewModel.java       // 선택된 필터 상태를 관리하는 LiveData 및 로직
│
├── 📁 utils
│   └── ShaderLoader.java          // shader 파일을 assets에서 로드하는 유틸리티 클래스
```

## 🎨 필터 구현 방식

- **OpenGL ES Fragment Shader**를 활용하여 실시간 필터 처리
- Camera2 API의 SurfaceTexture를 `samplerExternalOES`로 처리
- 필터 전환 시 동적으로 Shader 재로드

### 지원 필터

- **Original**: 기본 카메라 출력
- **Grayscale**: RGB 값을 가중 평균하여 흑백 변환
- **Brightness**: RGB 값에 고정 상수(예: `+0.3f`)를 더하여 밝기 증가

---

## 🧩 주요 이슈 및 해결 방식

- **SurfaceTexture 사용 시점**  
  `GLSurfaceView.Renderer`의 `onSurfaceCreated()` 이전에는 SurfaceTexture가 준비되지 않음  
  → OnRendererReadyCallback 인터페이스를 정의하여 콜백 방식으로 해결

- **GLSurfaceView vs TextureView**  
  GLSurfaceView는 OpenGL 렌더링에 적합하지만, UI 요소와의 통합에 제약이 있음  
  → 본 프로젝트에서는 렌더링에 집중된 구조를 위해 GLSurfaceView 사용

---

## 🧪 개발 순서 및 학습 과정

1. Android Studio 설치 후 빈 프로젝트 생성하여 프로젝트 구조 학습
2. MVVM + Repository 아키텍처 학습 및 프로젝트 구조 설계
3. OpenGL ES 및 셰이더 작성 → 필터 효과 적용 뷰 테스트
4. Camera2 API 연동 → SurfaceTexture와 OpenGL 연결 구현

---

## 📈 확장 고려 사항
향후 이미지나 동영상에도 필터를 적용하는 방향으로 확장한다고 가정하면, 다음과 같은 점들을 고려

### 📷 이미지 필터 적용

- 사용자 사진첩 접근을 통해 이미지를 선택하고, 선택된 이미지를 OpenGL 텍스처로 변환해 필터를 적용한 뒤 결과를 다시 저장하는 방식으로 구성해야 함
- 최종 결과 저장 시, 사용자 단말 내 갤러리에 저장하거나 앱 전용 디렉터리에 저장하는 형태도 고려 가능

#### 🔄 처리 위치에 대한 생각

이미지의 경우는 렌더링 비용이 상대적으로 낮고, 개인정보가 포함된 경우가 많다 보니  - 
**사용자 기기에서 필터를 적용하고 결과만 저장하는 방식**이 좀 더 현실적이지 않나 싶다.  
물론, 서버에서 처리하면 다양한 필터 적용이나 품질 관리 면에서는 유리하지만, 업로드/다운로드 처리나 프라이버시 측면도 함께 고민해야 할 듯하다.

---

### 🎥 동영상 필터 적용

동영상까지 확장하게 되면 고려해야 할 점이 훨씬 많아진다.

- 동영상 디코딩 → 프레임 단위로 OpenGL에 전달 → 필터 적용 → 다시 인코딩 및 저장이라는 복잡한 파이프라인이 필요
- MediaCodec이나 FFmpeg 등을 사용해서 디코딩/인코딩을 구현해야 하고, 오디오 싱크나 프레임 드롭 같은 문제도 신경 써야 함
- 저장 용량, 렌더링 시간, 배터리 사용량 등도 무시할 수 없음

#### 🔄 처리 위치에 대한 생각

내 생각엔, 동영상 렌더링은 기기 성능에 따라 체감 차이가 클 수 있어서  
**서버에서 렌더링을 수행하고 결과만 사용자에게 전달하는 방식**이 더 적절할 수도 있다.  
다만 이 경우에도 파일 전송 시간이나 서버 인프라 비용 같은 현실적인 제약도 함께 고려해야 한다.  
클라이언트 단에서 저해상도 프리뷰를 먼저 제공한 뒤, 서버에서 고해상도 렌더링을 완료해 다운로드하도록 하는 하이브리드 방식도 검토해볼 만하다.
