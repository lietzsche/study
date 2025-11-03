package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class AutoSaveServiceTest {
    private Document document;
    private FileService fileService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        document = new Document();
        fileService = new FileService();
        tempDir = Files.createTempDirectory("autosave-test-");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        }
    }

    private static Clock fixedClock() {
        return Clock.fixed(Instant.parse("2023-01-01T00:00:00Z"), ZoneId.of("UTC"));
    }

    @Test
    void autosavesOnChange() throws Exception {
        Path target = tempDir.resolve("draft.autosave");
        AutoSaveService.PathSupplier supplier = () -> target;
        AutoSaveService svc = new AutoSaveService(document, fileService, supplier, 200, fixedClock());

        document.setText("hello");
        svc.start();

        // 간단 대기 (스케줄러가 최소 한 번 실행되도록)
        Thread.sleep(350);
        String loaded = fileService.load(target);
        assertEquals("hello", loaded);

        // 동일 내용이면 저장하지 않음
        long before = Files.getLastModifiedTime(target).toMillis();
        Thread.sleep(300);
        long after = Files.getLastModifiedTime(target).toMillis();
        assertEquals(before, after);

        // 내용 변경 시 다시 저장됨
        document.setText("hello world");
        Thread.sleep(350);
        assertEquals("hello world", fileService.load(target));

        svc.stop();
    }
}

