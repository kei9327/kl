Knowlounge Android
======================

# 프로젝트 구조
  - fbwebapp\platform\android : 실제 안드로이드 프로젝트 소스
  - fbwebapp\platform\android\assets\www : 웹 소스 (html, javascript, css) 파일이 있는 디렉토리
  - fbwebapp\platform\android\libs : zico_android_sdk 라이브러리 파일이 있는 디렉토리
  - fbwebapp\platform\android\signature : 빌드에 필요한 키 스토어 파일이 있는 디렉토리
  - fbwebapp\platform\android\res : 안드로이드에서 참조하는 리소스 파일이 있는 디렉토리 (아이콘, 다국어처리 텍스트, 레이아웃 xml 파일 등)

# 개발환경 세팅
 - zico_android_sdk 설정
  - android\libs 경로에 alortc-sdk.aar 파일을 배치한다.
  - android\build.gradle 파일에 alortc-sdk 라이브러리의 dependency를 등록한다.
```
compile 'com.wescan.alo.rtc:alortc-sdk:1.2.3@aar'
(※ 버전을 꼭 맞춰서 등록해야 한다. 버전이 다르면 ClassNotFoundException이 발생한다.)
```
  - 라이브러리 경로 설정은 android\build.gradle 파일에서 repositories의 flatDir 구문을 참조하면 된다.

#  프로젝트 빌드하기 (Play Store 배포용 APK 빌드하기)
  - android\build.gradle 수정하기
     - versionCode와 versionName값을 수정한다. versionCode는 1씩 증가시키고, versionName은 상용은 짝수단위, 개발은 홀수단위로 증가시킨다.
     - debuggable 속성을 false로 수정한다.
  - AndroidManifest.xml 수정하기
     - Facebook SDK의 앱 아이디 값을 수정한다.
```
[개발버전]
<meta-data
	android:name="com.facebook.sdk.ApplicationId"
	android:value="@string/facebook_app_id_dev" />
```
```
[상용버전]
<meta-data
	android:name="com.facebook.sdk.ApplicationId"
	android:value="@string/facebook_app_id_release" />
```
  - google-services.json 파일 업데이트
    - 배포버전의 google-services.json 파일을 android 디렉토리 아래에 덮어쓴다.
  - fbwebapp\platform\android\res\strings.xml 수정하기
    - svr_flag 값을 수정한다. (0: 로컬, 1: 개발, 2: 상용)
  - fbwebapp\platform\android\assets\www\js\fb\common\prop.js 수정하기
    - svr.flag 값을 수정한다. (0: 로컬, 1: 개발, 2: 상용)
  - 안드로이드 스튜디오에서 좌측하단 탭 (밑에서 두번째)에 있는 Build Variants 탭을 눌러 release로 변경한다. 개발시에는 debug로 변경한다.
  - 위 단계가 모두 준비되면 Build > Generate Signed APK.. 메뉴를 선택한다.
  - KeyStore를 선택하는 창이 뜨는데 platforms\abdriud\signature에 있는 AndroidKey.wescan 파일을 지정한다. 그리고 양식에 아래의 정보를 입력하고 APK 생성을 계속 진행하면 된다.
    - Key store password : wescan
    - Key alias : wescan
    - Key password : wescan대박 (wescaneoqkr)
