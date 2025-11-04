package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 간단한 사용자 환경설정 저장/로드 서비스.
 * 사용자 홈의 ".tdd-editor/prefs.properties" 에 저장합니다.
 */
public class PreferencesService {
    private final Path storePath;
    private final Properties props = new Properties();

    public PreferencesService(Path storePath) {
        this.storePath = storePath;
        load();
    }

    public boolean getBoolean(String key, boolean def) {
        String v = props.getProperty(key);
        if (v == null) return def;
        return Boolean.parseBoolean(v);
    }

    public double getDouble(String key, double def) {
        String v = props.getProperty(key);
        if (v == null) return def;
        try { return Double.parseDouble(v); } catch (NumberFormatException e) { return def; }
    }

    public int getInt(String key, int def) {
        String v = props.getProperty(key);
        if (v == null) return def;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return def; }
    }

    public void setBoolean(String key, boolean value) { props.setProperty(key, Boolean.toString(value)); }
    public void setDouble(String key, double value) { props.setProperty(key, Double.toString(value)); }
    public void setInt(String key, int value) { props.setProperty(key, Integer.toString(value)); }

    public void save() {
        try {
            Files.createDirectories(storePath.getParent());
            try (var out = Files.newOutputStream(storePath)) {
                props.store(out, "editor preferences");
            }
        } catch (IOException ignored) {}
    }

    private void load() {
        try {
            if (!Files.exists(storePath)) return;
            try (var in = Files.newInputStream(storePath)) {
                props.load(in);
            }
        } catch (IOException ignored) {}
    }
}

