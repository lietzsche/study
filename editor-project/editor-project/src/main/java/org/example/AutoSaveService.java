package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 문서 내용을 주기적으로 임시 파일에 저장하는 서비스.
 * - 지정한 경로 제공자(PathSupplier)를 통해 대상 경로를 동적으로 결정
 * - 일정 시간 간격으로 현재 Document 내용을 저장
 */
public class AutoSaveService {
    public interface PathSupplier {
        Path getPath();
    }

    private final Document document;
    private final FileService fileService;
    private final ScheduledExecutorService scheduler;
    private final Clock clock;
    private final long intervalMillis;
    private final PathSupplier pathSupplier;

    private ScheduledFuture<?> future;
    private String lastSavedSnapshot = null;

    public AutoSaveService(Document document,
                           FileService fileService,
                           PathSupplier pathSupplier,
                           long intervalMillis,
                           Clock clock) {
        this.document = Objects.requireNonNull(document);
        this.fileService = Objects.requireNonNull(fileService);
        this.pathSupplier = Objects.requireNonNull(pathSupplier);
        this.intervalMillis = Math.max(250, intervalMillis);
        this.clock = Objects.requireNonNull(clock);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "autosave");
            t.setDaemon(true);
            return t;
        });
    }

    public synchronized void start() {
        if (future != null && !future.isCancelled()) return;
        future = scheduler.scheduleAtFixedRate(this::tick, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if (future != null) {
            future.cancel(false);
            future = null;
        }
    }

    private void tick() {
        try {
            String current = document.getText();
            if (lastSavedSnapshot != null && lastSavedSnapshot.equals(current)) {
                return; // 변경 없음
            }
            Path path = pathSupplier.getPath();
            if (path == null) return;
            fileService.save(path, current);
            lastSavedSnapshot = current;
        } catch (IOException ignored) {
            // 자동저장 실패는 UI에 방해 주지 않도록 조용히 무시
        } catch (Exception ignored) {
        }
    }
}

