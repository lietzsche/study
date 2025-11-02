package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    private Document document;

    @BeforeEach
    void setUp() {
        document = new Document();
    }

    @Test
    void shouldBeEmptyWhenCreated() {
        // 의도: 새로 생성된 문서는 비어 있어야 한다.
        assertEquals("", document.getText(), "초기 문서는 비어 있어야 합니다.");
    }

    @Test
    void shouldAddTextToDocument() {
        // 의도: 텍스트를 추가하면 내용이 누적되어야 한다.
        document.addText("Hello");
        assertEquals("Hello", document.getText());

        document.addText(" World");
        assertEquals("Hello World", document.getText(), "문서 내용이 누적되어야 합니다.");
    }

    @Test
    void shouldClearDocument() {
        // 의도: clear() 호출 시 문서가 비워져야 한다.
        document.addText("Temporary text");
        document.clear();
        assertEquals("", document.getText(), "clear() 이후 문서는 비어 있어야 합니다.");
    }

    @Test
    void shouldRemoveTextFromDocument() {
        // 의도: 지정한 범위(start, end)의 텍스트가 삭제되어야 한다.
        document.addText("Hello World");
        document.removeText(6, 11); // "World" 삭제
        assertEquals("Hello ", document.getText(), "부분 삭제 후 나머지 텍스트만 남아야 합니다.");
    }

    @Test
    void shouldHandleInvalidRangeGracefully() {
        // 의도: 잘못된 범위(start > end 등)는 예외로 처리되어야 한다.
        document.addText("Test Text");
        assertThrows(IllegalArgumentException.class,
                () -> document.removeText(8, 3), "start가 end보다 크면 IllegalArgumentException을 던져야 합니다.");
    }

    @Test
    void shouldHandleOutOfBoundsRangeGracefully() {
        // 의도: 문서 길이를 벗어나는 범위는 예외로 처리되어야 한다.
        document.addText("Sample");
        assertThrows(IndexOutOfBoundsException.class,
                () -> document.removeText(0, 100), "범위를 벗어나면 IndexOutOfBoundsException을 던져야 합니다.");
    }

}
