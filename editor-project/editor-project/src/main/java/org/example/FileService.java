package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileService {

    public void save(Path path, String content) throws IOException {
        if (path == null) throw new IllegalArgumentException("path must not be null");
        if (content == null) content = "";
        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    public String load(Path path) throws IOException {
        if (path == null) throw new IllegalArgumentException("path must not be null");
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
