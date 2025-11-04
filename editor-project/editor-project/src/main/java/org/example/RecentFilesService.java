package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

/**
 * 최근 파일 경로 목록을 LRU로 관리하고 디스크에 영속화한다.
 */
public class RecentFilesService {
    private final int limit;
    private final Path storePath;
    private final LinkedHashSet<String> lru = new LinkedHashSet<>();

    public RecentFilesService(Path storePath, int limit) {
        this.storePath = storePath;
        this.limit = Math.max(1, limit);
        tryLoad();
    }

    public synchronized void push(Path path) {
        if (path == null) return;
        String s = path.toAbsolutePath().toString();
        lru.remove(s);
        lru.add(s);
        while (lru.size() > limit) {
            String first = lru.iterator().next();
            lru.remove(first);
        }
        trySave();
    }

    public synchronized List<Path> list() {
        List<Path> out = new ArrayList<>();
        for (String s : lru) out.add(Path.of(s));
        // 최신이 마지막에 들어가므로, UI 표시 시 역순이 자연스러울 수 있으나 여기선 삽입 순서 유지
        return out;
    }

    private void tryLoad() {
        try {
            if (!Files.exists(storePath)) return;
            Properties p = new Properties();
            try (var in = Files.newInputStream(storePath)) { p.load(in); }
            int n = Integer.parseInt(p.getProperty("count", "0"));
            for (int i = 0; i < n; i++) {
                String s = p.getProperty("item." + i);
                if (s != null && !s.isBlank()) lru.add(s);
            }
        } catch (Exception ignored) {}
    }

    private void trySave() {
        try {
            Properties p = new Properties();
            List<String> items = new ArrayList<>(lru);
            p.setProperty("count", Integer.toString(items.size()));
            for (int i = 0; i < items.size(); i++) p.setProperty("item." + i, items.get(i));
            Files.createDirectories(storePath.getParent());
            try (var out = Files.newOutputStream(storePath)) { p.store(out, "recent files"); }
        } catch (IOException ignored) {}
    }
}

