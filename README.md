# Camera Filter App

OpenGL ES와 Android Camera2 API를 기반으로, 실시간으로 필터(흑백 및 밝기 증가)를 적용하는 카메라 애플리케이션입니다.
<br/><br/><br/>

## 구현 내용 

- Android 앱에서 실시간으로 카메라 프리뷰를 보여주기 위해 **Camera2 API**와 **GLSurfaceView** 사용
- 카메라 프레임은 `SurfaceTexture`를 통해 OpenGL에 전달되고, `CameraRenderer` 클래스에서 **OpenGL ES 2.0의 Fragment Shader**를 통해 필터 효과 적용
- 각 필터는 픽셀의 RGB 값을 기반으로 간단한 연산을 수행하여 색상을 조정하는 방식으로 구현

#### 구현된 필터 종류
- **흑백 필터 (Grayscale)**  
  RGB 값을 평균 내어 하나의 회색조 값으로 변환합니다.  
  예: `gray = (r + g + b) / 3`

- **밝기 증가 필터 (Brightness Up)**  
  각 RGB 값에 일정한 밝기 값을 더해 화면을 더 밝게 만듭니다.  
  예: `r += 0.2`, `g += 0.2`, `b += 0.2`
<br/><br/><br/>



## 어려웠던 점
### **1. SurfaceTexture 사용 시점**
- `GLSurfaceView.Renderer`의 `onSurfaceCreated()` 이전에는 `SurfaceTexture`가 준비되지 않음.
- 카메라 렌더러에서 `SurfaceTexture`를 생성한 후에야 `SurfaceTexture`를 카메라에 넘겨줄 수 있기 때문에, `OnRendererReadyCallback` 인터페이스를 정의하여 콜백 방식으로 처리.
- 이를 통해 `SurfaceTexture`가 완성된 시점에서 카메라를 시작하도록 하여, 적절한 타이밍에 카메라를 시작할 수 있도록 개선함.
  
#### 해결책
- `SurfaceTexture`가 준비된 후에 카메라를 시작해야 하므로, `OnRendererReadyCallback`을 사용하여 `SurfaceTexture`가 완성된 후 카메라를 시작하도록 처리.
<br/>

### **2. CameraManager.openCamera() 호출 시 "No handler given" 오류 발생**
- `startCamera()`를 **GLThread (OpenGL 렌더링 쓰레드)**가 아닌 메인 스레드에서 실행해야 함.
- `CameraHandler.startCamera()`가 `onSurfaceCreated()` 안에서 호출되었는데, 이 메서드는 **GLThread**에서 실행됨.
  
#### 해결책
- **GLThread**에서는 OpenGL 관련 로직만 처리하고, 카메라와 같은 외부 로직은 메인 스레드에서 처리해야 함.
- `SurfaceTexture`가 준비된 후 카메라를 시작해야 하므로, `runOnUiThread`를 사용하여 메인 스레드에서 카메라를 시작함.
<br/>

### **3. OpenGL 명령어 호출 시점**

- `GLContext`가 생성되고 나서 OpenGL 명령어를 호출해야 함.
- OpenGL 명령어는 반드시 `Renderer.onSurfaceCreated()` 또는 `onDrawFrame()`과 같은 적절한 위치에서만 호출되어야 함.

<br/><br/><br/>


## 필터 적용

- **흑백 필터**는 색상을 제거하고 명암만을 남기는 방식으로, 이미지의 감성적 변화를 쉽게 구현 가능, 픽셀의 색상 정보를 그레이스케일로 변환하는 간단한 연산으로, 사용자는 빠르게 분위기 있는 이미지를 생성

- **밝기 증가 필터**는 이미지의 밝기를 조정하여 어두운 환경에서 세부 사항을 더 잘 보이게 함, 픽셀 밝기 값을 일정 비율로 증가시켜 저조도에서 촬영된 이미지를 개선하고 더 명확한 시각적 정보를 제공

이 두 필터는 간단한 연산을 통해 **효율적인 이미지 처리**를 가능하게 하며, 사용자가 빠르게 원하는 시각적 변화 적용
<br/><br/><br/>


## 확장 고려 사항
### 이미지 필터 적용
이미지에 필터를 적용할 때 렌더링 비용이 상대적으로 낮고 개인정보가 포함될 가능성이 크기 때문에, **사용자 기기에서 필터를 적용하고 결과만 저장하는 방식**이 더 현실적일 것임.  
서버에서 처리하는 방식은 필터의 다양성이나 품질 관리 측면에서 유리하지만, **업로드/다운로드 시간**과 **프라이버시** 문제가 따르므로, **사용자 기기에서 필터를 적용한 후 결과를 저장**하는 방식이 더 적합할 것으로 보임.
<br/>

### 동영상 필터 적용
동영상에 필터를 적용하려면 **동영상 디코딩 → 프레임 단위로 OpenGL에 전달 → 필터 적용 → 인코딩 및 저장**이라는 복잡한 파이프라인을 처리해야 함.  
이를 위해 **MediaCodec**이나 **FFmpeg**를 사용해 디코딩/인코딩을 구현하고, **오디오 싱크**와 **프레임 드롭** 문제도 신경 써야 함.  
또한, **저장 용량**, **렌더링 시간**, **배터리 사용량** 등을 고려해야 하므로 기기 성능에 따라 차이가 클 수 있음.

#### 처리 위치에 대한 전략
동영상 렌더링은 기기 성능에 따라 차이가 크게 나기 때문에, **서버에서 렌더링을 처리하고 결과만 사용자에게 전달**하는 방식이 더 효율적일 수 있음.  
하지만 이 방식도 **파일 전송 시간**과 **서버 인프라 비용** 등의 현실적인 제약이 있기 때문에, 하이브리드 방식도 고려할 필요가 있음. 예를 들어, **저해상도 프리뷰**를 클라이언트에서 제공하고, **서버에서 고해상도 렌더링 후 다운로드**하는 방식이 적합할 수 있음.

<br/><br/><br/>


## 기술 스택 및 아키텍처

- **Architecture**: MVVM 패턴
- **Camera API**: Android Camera2 API
- **OpenGL 처리**: GLSurfaceView + SurfaceTexture + Shader 기반 실시간 렌더링
- **Shader 파일 관리**: `assets/shaders` 디렉터리
<br/><br/><br/>


## UI 구성
- 필터 변경 버튼을 통해 실시간으로 필터를 변경할 수 있으며,  
  현재 적용된 필터는 버튼 상단의 TextView에 표시
<br/><br/><br/>



## 프로젝트 구조
```
camera_filter_app/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/camera_filter_app/
│           │   ├── 📁 camera/
│           │   │   └── CameraHandler.java            # Camera2 API를 이용한 카메라 제어 클래스
│           │   ├── 📁 gl/
│           │   │   ├── CameraRenderer.java           # OpenGL 렌더링 및 필터 적용
│           │   │   └── ShaderProgram.java            # 셰이더 프로그램 빌드 및 관리
│           │   ├── 📁 model/
│           │   │   └── FilterType.java               # 필터 enum 정의
│           │   ├── 📁 ui/filter/
│           │   │   └── FilterActivity.java           # 필터 화면 액티비티
│           │   ├── 📁 util/
│           │   │   └── ShaderLoader.java             # shader 파일을 assets에서 로드하는 유틸리티 클래스
│           │   └── 📁 viewmodel/
│           │       └── FilterViewModel.java          # LiveData로 필터 상태 관리
│           ├── res/
│           │   ├── 📁 layout/
│           │       └── activity_filter.xml           # UI 레이아웃
│           ├── 📁 assets/
│           │   ├── default.vs                        # 공통 Vertex Shader
│           │   ├── original.fs                       # 원본 필터 Fragment Shader
│           │   ├── grayscale.fs                      # 흑백 필터 Fragment Shader
│           │   └── brightness.fs                     # 밝기 증가 필터 Fragment Shader
│           └── AndroidManifest.xml
```
<br/><br/><br/>


## 개발 순서 및 학습 과정

1. **Android Studio 설치 및 프로젝트 구조 파악**
   - 빈 프로젝트 생성 후 `app`, `manifest`, `java`, `res` 등 폴더 구조와 역할 학습
   - 앱 진입점(`MainActivity`)과 전체 실행 흐름 이해

2. **아키텍처 선정 및 구조 설계**
   - 기존에는 MVC 패턴 기반의 UI–데이터 분리 경험 있음
   - 안드로이드에서 널리 사용되는 아키텍처(MVP, MVVM 등) 비교
   - 공식 Android Architecture Guide 참고하여 **MVVM + Repository 패턴** 채택

3. **MVVM + Repository 예제 코드 분석**
   - 버튼 클릭 시 `"hello world"`를 출력하는 간단한 구조로 개념 정리
   - `DataSource`: 메시지를 저장하는 역할 (예: DB처럼)
   - `Repository`: DataSource와 ViewModel 사이의 중계자 (예: API처럼)
   - `ViewModel`: UI 이벤트 처리 및 상태 관리 (LiveData 사용)
   - `Activity`: ViewModel을 관찰하고 사용자 입력을 전달하는 UI 계층

4. **OpenGL ES 및 셰이더 학습**
   - 기본 셰이더 작성법 및 GLSurfaceView 사용법 학습
   - OpenGL로 필터 효과가 적용되는 렌더링 뷰 구현 및 테스트

5. **Camera2 API 연동**
   - `SurfaceTexture`와 OpenGL 사이 연결 구조 구현
   - 카메라 미리보기 프레임을 OpenGL로 렌더링하여 실시간 필터 적용

<br/><br/><br/>
