package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void sanityCheck() {
        assertEquals(2, 1 + 1);
    }

    @Test
    void appClassExists() {
        // GUI 실행 없이 존재성만 확인
        assertNotNull(new App());
    }
}

