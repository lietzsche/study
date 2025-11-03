package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentHistoryTest {

    private Document document;
    private DocumentHistory history;

    @BeforeEach
    void setUp() {
        document = new Document();
        history = new DocumentHistory(document);
    }

    @Test
    void shouldUndoLastAddOperation() {
        // 의도: addText 실행 후 undo 시 이전 상태로 복원
        history.executeAdd("Hello");
        history.executeAdd(" World");
        assertEquals("Hello World", document.getText());

        history.undo();
        assertEquals("Hello", document.getText(), "Undo 후 마지막 추가 텍스트가 제거되어야 함");
    }

    @Test
    void shouldRedoAfterUndo() {
        // 의도: undo 이후 redo로 다시 복원
        history.executeAdd("Test");
        history.undo();
        assertEquals("", document.getText());

        history.redo();
        assertEquals("Test", document.getText(), "Redo 후 텍스트가 복원되어야 함");
    }

    @Test
    void shouldNotCrashWhenUndoBeyondLimit() {
        // 의도: Undo 횟수가 한계를 넘어도 에러 없이 동작
        history.executeAdd("A");
        history.undo();
        history.undo(); // 한 번 더 실행해도 안전해야 함
        assertEquals("", document.getText());
    }

    @Test
    void shouldNotCrashWhenRedoBeyondLimit() {
        // 의도: Redo 횟수가 한계를 넘어도 에러 없이 동작
        history.executeAdd("B");
        history.undo();
        history.redo();
        history.redo(); // 한 번 더 실행해도 안전해야 함
        assertEquals("B", document.getText());
    }

    @Test
    void canUndoRedoFlagsAndLimit() {
        // 히스토리 용량 제한(2)으로 생성
        history = new DocumentHistory(document, 2);
        assertFalse(history.canUndo());
        assertFalse(history.canRedo());

        history.executeAdd("1");
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());

        history.executeAdd("2");
        history.executeAdd("3"); // 이 시점에서 가장 오래된 스냅샷은 버려짐

        history.undo(); // -> "12" 상태가 됨
        assertTrue(history.canRedo());

        history.undo(); // -> "1" 상태가 됨 (더 이상 undo 불가)
        assertFalse(history.canUndo());
    }
}
