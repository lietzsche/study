package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private FileService fileService;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        fileService = new FileService();
        tempDir = Files.createTempDirectory("editor-project-test-");
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

    @Test
    void saveAndLoadRoundTrip() throws IOException {
        String content = "한글 포함 Hello, World!";
        Path file = tempDir.resolve("notes").resolve("test.txt");

        fileService.save(file, content);

        assertTrue(Files.exists(file), "파일이 저장되어야 합니다");
        String loaded = fileService.load(file);
        assertEquals(content, loaded, "저장한 내용과 불러온 내용이 같아야 합니다");
    }

    @Test
    void saveNullContentTreatsAsEmpty() throws IOException {
        Path file = tempDir.resolve("empty.txt");
        fileService.save(file, null);

        String loaded = fileService.load(file);
        assertEquals("", loaded);
    }

    @Test
    void nullPathThrows() {
        assertThrows(IllegalArgumentException.class, () -> fileService.save(null, "x"));
        assertThrows(IllegalArgumentException.class, () -> fileService.load(null));
    }
}

