package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecentFilesServiceTest {
    @Test
    void lruAndPersist(@TempDir Path tmp) throws Exception {
        Path store = tmp.resolve("recent.properties");
        RecentFilesService s = new RecentFilesService(store, 3);
        Path a = tmp.resolve("a.txt");
        Path b = tmp.resolve("b.txt");
        Path c = tmp.resolve("c.txt");
        Path d = tmp.resolve("d.txt");
        Files.writeString(a, "a");
        Files.writeString(b, "b");
        Files.writeString(c, "c");
        Files.writeString(d, "d");

        s.push(a); s.push(b); s.push(c);
        List<Path> l1 = s.list();
        assertEquals(3, l1.size());
        assertTrue(l1.contains(a) && l1.contains(b) && l1.contains(c));

        // push d -> a가 밀려나야 함
        s.push(d);
        List<Path> l2 = s.list();
        assertEquals(3, l2.size());
        assertFalse(l2.contains(a));

        // 재로딩
        RecentFilesService s2 = new RecentFilesService(store, 3);
        List<Path> l3 = s2.list();
        assertEquals(l2, l3);
    }
}

