# TDD 기반 텍스트 에디터 (JavaFX)

본 프로젝트는 문서 모델/히스토리/파일 IO를 TDD로 구축하고, JavaFX UI로 동작하는 간단한 텍스트 에디터입니다.

## 주요 기능
- 문서 모델(`Document`): 추가/삭제/전체 교체/초기화
- 히스토리(`DocumentHistory`): Undo/Redo, canUndo/canRedo, 용량 제한(기본 1000)
- 파일 입출력(`FileService`): UTF-8 저장/불러오기, 디렉터리 자동 생성
- 자동 저장(`AutoSaveService`): 변경 시 주기적 임시 파일 저장
- JavaFX UI(`App`): 메뉴(New/Open/Save/Save As/Exit), 단축키(Ctrl+Z/Y/Shift+Z), 종료 시 저장 확인, 오류 알림

## 실행 방법
- 요구사항: JDK 17, 인터넷(초회 JavaFX 의존성 다운로드)
- 실행: `./gradlew run`
- 테스트: `./gradlew test`

Windows에서 한글 인코딩 혼선을 줄이기 위해 컴파일 및 실행 시 UTF-8을 사용하도록 Gradle을 구성했습니다.

## 프로젝트 구조
- `src/main/java/org/example/Document.java`
- `src/main/java/org/example/DocumentHistory.java`
- `src/main/java/org/example/FileService.java`
- `src/main/java/org/example/AutoSaveService.java`
- `src/main/java/org/example/App.java`
- `src/main/java/org/example/EditorController.java`
- `src/test/java/org/example/*.java`

## 변경 사항 하이라이트
- UI와 도메인 로직 분리를 위해 `EditorController`를 도입했습니다.
- 파일 저장 시 UTF-8 인코딩을 명시적으로 사용합니다.
- 컨트롤러 단위 테스트(`EditorControllerTest`)를 추가하여 동작을 검증합니다.

## 향후 개선 아이디어
- 명령(Command) 객체로 리팩터링(Add/Remove 등)
- 상태 표시줄(Undo/Redo 가능여부, 파일명, 인코딩 등)
- 자동 저장 복구 UI(크래시/비정상 종료 시 임시 파일 복원 안내)
- 다국어(i18n) 리소스 번들 적용
- CI 설정(GitHub Actions)으로 테스트 자동화
