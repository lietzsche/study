package org.example;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * 순수 로직 컨트롤러: 문서/히스토리/파일 IO를 캡슐화하여
 * UI(JavaFX)와 분리된 상태로 테스트 가능하도록 한다.
 */
public class EditorController {
    private final Document document;
    private final DocumentHistory history;
    private final FileService fileService;

    private Path currentFile;
    private String lastSavedText = "";

    public EditorController(Document document, DocumentHistory history, FileService fileService) {
        this.document = Objects.requireNonNull(document);
        this.history = Objects.requireNonNull(history);
        this.fileService = Objects.requireNonNull(fileService);
    }

    public void applyUserEdit(String newText) {
        if (!Objects.equals(newText, document.getText())) {
            history.executeSet(newText);
        }
    }

    public String getText() {
        return document.getText();
    }

    public void newDocument() {
        history.executeSet("");
        currentFile = null;
        lastSavedText = document.getText();
    }

    public void open(Path path) throws IOException {
        String content = fileService.load(path);
        history.executeSet(content);
        currentFile = path;
        lastSavedText = document.getText();
    }

    public void save() throws IOException {
        if (currentFile == null) throw new IllegalStateException("no current file");
        fileService.save(currentFile, document.getText());
        lastSavedText = document.getText();
    }

    public void saveAs(Path path) throws IOException {
        fileService.save(path, document.getText());
        currentFile = path;
        lastSavedText = document.getText();
    }

    public void undo() {
        history.undo();
    }

    public void redo() {
        history.redo();
    }

    public boolean canUndo() {
        return history.canUndo();
    }

    public boolean canRedo() {
        return history.canRedo();
    }

    public boolean isDirty() {
        return !Objects.equals(document.getText(), lastSavedText);
    }

    public Path getCurrentFile() {
        return currentFile;
    }
}

