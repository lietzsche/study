## 🧠 TDD 기반 문서 편집기 개발 로그

### 📁 파일 이름

`tdd-text-editor-progress.md`

---

### 🧩 프로젝트 개요

* **언어:** Java 17
* **GUI:** JavaFX
* **개발 방식:** TDD (Test-Driven Development)
* **현재까지 구현 완료:**

    1. Gradle 기반 JavaFX 실행 환경 구축
    2. JUnit5 테스트 환경 구성 및 기본 테스트 통과
    3. `Document` 클래스 작성 및 테스트 (add, remove, clear)
    4. `DocumentHistory` 구현 및 Undo/Redo 테스트 통과
    5. JavaFX UI 연동 (TextArea + Ctrl+Z / Ctrl+Y / Ctrl+Shift+Z)

---

### ✅ 현재 코드 구조

#### `Document.java`

```java
package org.example;

public class Document {
    private final StringBuilder text;

    public Document() {
        this.text = new StringBuilder();
    }

    public String getText() {
        return this.text.toString();
    }

    public void addText(String newText) {
        this.text.append(newText);
    }

    public void removeText(int start, int end) {
        if (start < 0 || end < 0)
            throw new IndexOutOfBoundsException("인덱스는 음수가 될 수 없습니다.");
        if (start > end)
            throw new IllegalArgumentException("start index must not exceed end index");
        if (end > this.text.length())
            throw new IndexOutOfBoundsException("end index가 문서 길이를 초과했습니다.");

        this.text.delete(start, end);
    }

    public void clear() {
        this.text.setLength(0);
    }
}
```

#### `DocumentHistory.java`

```java
package org.example;

import java.util.Deque;
import java.util.LinkedList;

public class DocumentHistory {
    private final Document document;
    private final Deque<String> history;
    private final Deque<String> futureHistory;

    public DocumentHistory(Document document) {
        this.document = document;
        this.history = new LinkedList<>();
        this.futureHistory = new LinkedList<>();
    }

    void executeAdd(String text) {
        history.addFirst(document.getText());
        document.addText(text);
        futureHistory.clear();
    }

    void undo() {
        doIt(futureHistory, history);
    }

    void redo() {
        doIt(history, futureHistory);
    }

    private void doIt(Deque<String> add, Deque<String> sub) {
        if (sub.isEmpty()) return;
        add.addFirst(document.getText());
        String tmp = sub.removeFirst();
        document.clear();
        document.addText(tmp);
    }
}
```

#### `App.java`

```java
package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {
    private Document document;
    private DocumentHistory history;
    private TextArea textArea;

    @Override
    public void start(Stage stage) {
        document = new Document();
        history = new DocumentHistory(document);
        textArea = new TextArea();

        textArea.setText(document.getText());
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcuts);

        textArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.equals(document.getText())) {
                String diff = newText.substring(document.getText().length());
                history.executeAdd(diff);
            }
        });

        BorderPane root = new BorderPane(textArea);
        Scene scene = new Scene(root, 600, 400);

        stage.setScene(scene);
        stage.setTitle("TDD Text Editor Prototype");
        stage.show();
    }

    private void handleShortcuts(KeyEvent event) {
        KeyCombination undo = new KeyCodeCombination(javafx.scene.input.KeyCode.Z, KeyCombination.CONTROL_DOWN);
        KeyCombination redo = new KeyCodeCombination(javafx.scene.input.KeyCode.Y, KeyCombination.CONTROL_DOWN);
        KeyCombination redoAlt = new KeyCodeCombination(javafx.scene.input.KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

        if (undo.match(event)) {
            history.undo();
            textArea.setText(document.getText());
            textArea.positionCaret(document.getText().length());
            event.consume();
        } else if (redo.match(event) || redoAlt.match(event)) {
            history.redo();
            textArea.setText(document.getText());
            textArea.positionCaret(document.getText().length());
            event.consume();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
```

---

### 🧪 테스트 코드

* `DocumentTest.java`: 문서 조작 (add/remove/clear)
* `DocumentHistoryTest.java`: Undo/Redo 기능

---

### 💾 현재 커밋 제안

**커밋 이름:** `feat: add TDD-based document editor prototype with undo/redo`

**커밋 메시지 상세:**

```
- Document 모델 작성 및 단위 테스트 추가
- DocumentHistory 구현 및 Undo/Redo 로직 통과
- JavaFX UI 연동 및 단축키(Ctrl+Z / Ctrl+Y / Ctrl+Shift+Z) 적용
- 전체 코드 TDD 사이클 3단계 완료 (RED → GREEN → REFACTOR)
```

---

### 🔜 다음에 이어서 할 것

1. **파일 저장 및 불러오기 기능 (TDD 4단계)**

    * `FileService` 클래스 작성
    * 테스트: `save()` / `load()` 검증 (Mock 파일 사용)
2. **명령(Command) 객체화 리팩터링**

    * `AddCommand`, `RemoveCommand` 등 구체 명령 클래스로 구조화
3. **UI 확장**

    * 메뉴바 (File, Edit)
    * 상태 표시줄 추가 (Undo/Redo 상태 표시)
    * 자동 저장 기능 (`ScheduledExecutorService` 활용)

---

### 💬 GPT 프롬프트 (다음 회차 이어서 시작용)

```
이어서 TDD 기반 문서 편집기 프로젝트를 진행하자.
우리는 이전까지 Document, DocumentHistory, JavaFX UI (Ctrl+Z / Ctrl+Y / Ctrl+Shift+Z)까지 완성했다.
이제 4단계로 파일 저장 및 불러오기 기능을 TDD로 추가하자.
먼저 FileService 테스트 코드부터 작성해줘 (RED 단계로 시작).
```
