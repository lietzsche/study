package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    @Test
    void sanityCheck() {
        // 기본적인 테스트 환경이 작동하는지 확인
        assertEquals(2, 1 + 1, "JUnit이 제대로 작동해야 합니다.");
    }

    @Test
    void appShouldLaunchWithoutErrors() {
        // 단순하게 App 클래스가 존재하는지 확인
        App app = new App();
        assertNotNull(app, "App 인스턴스가 null이면 안 됩니다.");
    }
}