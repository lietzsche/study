package org.example;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class App extends Application {
    private Document document;
    private DocumentHistory history;
    private TextArea textArea;
    private FileService fileService;
    private boolean programmaticUpdate = false;
    private AutoSaveService autoSaveService;
    private EditorController controller;
    private Label statusBar;

    @Override
    public void start(Stage stage) {
        document = new Document();
        history = new DocumentHistory(document);
        textArea = new TextArea();
        fileService = new FileService();
        controller = new EditorController(document, history, fileService);
        autoSaveService = new AutoSaveService(
                document,
                fileService,
                this::autoSavePath,
                2_000,
                java.time.Clock.systemDefaultZone()
        );

        textArea.setText(controller.getText());
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcuts);
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            if (programmaticUpdate) return;
            controller.applyUserEdit(newText);
            updateWindowTitle(stage);
            updateStatusBar();
        });

        MenuBar menuBar = createMenuBar(stage);
        statusBar = buildStatusBar();
        BorderPane root = new BorderPane(textArea);
        root.setTop(menuBar);
        root.setBottom(statusBar);
        Scene scene = new Scene(root, 800, 600);

        stage.setScene(scene);
        updateWindowTitle(stage);
        stage.setOnCloseRequest(e -> {
            if (!confirmClose(stage)) {
                e.consume();
            } else {
                autoSaveService.stop();
            }
        });
        stage.show();

        autoSaveService.start();
    }

    private void handleShortcuts(KeyEvent event) {
        KeyCombination undo = new KeyCodeCombination(javafx.scene.input.KeyCode.Z, KeyCombination.CONTROL_DOWN);
        KeyCombination redo = new KeyCodeCombination(javafx.scene.input.KeyCode.Y, KeyCombination.CONTROL_DOWN);
        KeyCombination redoAlt = new KeyCodeCombination(
                javafx.scene.input.KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

        if (undo.match(event)) {
            controller.undo();
            applyDocumentToEditor();
            event.consume();
        } else if (redo.match(event) || redoAlt.match(event)) {
            controller.redo();
            applyDocumentToEditor();
            event.consume();
        }
    }

    private void applyDocumentToEditor() {
        programmaticUpdate = true;
        try {
            textArea.setText(controller.getText());
            textArea.positionCaret(controller.getText().length());
        } finally {
            programmaticUpdate = false;
        }
        updateWindowTitle((Stage) textArea.getScene().getWindow());
        updateStatusBar();
    }

    private MenuBar createMenuBar(Stage stage) {
        Menu menuFile = new Menu("File");
        MenuItem miNew = new MenuItem("New");
        MenuItem miOpen = new MenuItem("Open...");
        MenuItem miSave = new MenuItem("Save");
        MenuItem miSaveAs = new MenuItem("Save As...");
        MenuItem miExit = new MenuItem("Exit");

        miNew.setAccelerator(new KeyCodeCombination(javafx.scene.input.KeyCode.N, KeyCombination.CONTROL_DOWN));
        miOpen.setAccelerator(new KeyCodeCombination(javafx.scene.input.KeyCode.O, KeyCombination.CONTROL_DOWN));
        miSave.setAccelerator(new KeyCodeCombination(javafx.scene.input.KeyCode.S, KeyCombination.CONTROL_DOWN));

        miNew.setOnAction(e -> {
            controller.newDocument();
            updateWindowTitle(stage);
            applyDocumentToEditor();
        });

        miOpen.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Open File");
            java.io.File file = chooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    controller.open(file.toPath());
                    updateWindowTitle(stage);
                    applyDocumentToEditor();
                } catch (Exception ex) {
                    showError("파일 열기 실패", ex);
                }
            }
        });

        miSave.setOnAction(e -> doSave(stage, false));
        miSaveAs.setOnAction(e -> doSave(stage, true));
        miExit.setOnAction(e -> stage.close());

        menuFile.getItems().addAll(miNew, miOpen, miSave, miSaveAs, new SeparatorMenuItem(), miExit);

        Menu menuEdit = new Menu("Edit");
        MenuItem miUndo = new MenuItem("Undo");
        MenuItem miRedo = new MenuItem("Redo");
        MenuItem miFind = new MenuItem("Find...");
        miUndo.setAccelerator(new KeyCodeCombination(javafx.scene.input.KeyCode.Z, KeyCombination.CONTROL_DOWN));
        miRedo.setAccelerator(new KeyCodeCombination(javafx.scene.input.KeyCode.Y, KeyCombination.CONTROL_DOWN));
        miFind.setAccelerator(new KeyCodeCombination(javafx.scene.input.KeyCode.F, KeyCombination.CONTROL_DOWN));
        miUndo.setOnAction(e -> { controller.undo(); applyDocumentToEditor(); });
        miRedo.setOnAction(e -> { controller.redo(); applyDocumentToEditor(); });
        miFind.setOnAction(e -> doFind(stage));
        menuEdit.getItems().addAll(miUndo, miRedo, new SeparatorMenuItem(), miFind);

        Menu menuView = new Menu("View");
        MenuItem miWrap = new MenuItem("Toggle Word Wrap");
        miWrap.setOnAction(e -> textArea.setWrapText(!textArea.isWrapText()));
        menuView.getItems().addAll(miWrap);

        return new MenuBar(menuFile, menuEdit, menuView);
    }

    private void doSave(Stage stage, boolean forceChoose) {
        try {
            if (controller.getCurrentFile() == null || forceChoose) {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Save File");
                java.io.File file = chooser.showSaveDialog(stage);
                if (file == null) return;
                controller.saveAs(file.toPath());
            } else {
                controller.save();
            }
            updateWindowTitle(stage);
        } catch (Exception ex) {
            showError("파일 저장 실패", ex);
        }
    }

    public static void main(String[] args) { launch(); }

    private boolean isDirty() { return controller.isDirty(); }

    private boolean confirmClose(Stage stage) {
        if (!isDirty()) return true;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "변경 사항이 있습니다. 저장하겠습니까?",
                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setTitle("종료 확인");
        alert.initOwner(stage);
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (result == ButtonType.YES) {
            doSave(stage, controller.getCurrentFile() == null);
            return !isDirty();
        } else if (result == ButtonType.NO) {
            return true;
        }
        return false;
    }

    private void showError(String header, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("오류");
        alert.setHeaderText(header);
        alert.setContentText(ex.getMessage() == null ? ex.toString() : ex.getMessage());
        alert.showAndWait();
    }

    private java.nio.file.Path autoSavePath() {
        try {
            if (controller.getCurrentFile() != null) {
                java.nio.file.Path currentFile = controller.getCurrentFile();
                return currentFile.resolveSibling(currentFile.getFileName().toString() + ".autosave");
            }
            String tmp = System.getProperty("java.io.tmpdir");
            return java.nio.file.Paths.get(tmp).resolve("editor-project-autosave.txt");
        } catch (Exception e) {
            return null;
        }
    }

    private void updateWindowTitle(Stage stage) {
        String name = controller.getCurrentFile() != null
                ? controller.getCurrentFile().getFileName().toString()
                : "Untitled";
        String dirty = isDirty() ? "*" : "";
        stage.setTitle(dirty + name + " - TDD Text Editor");
    }

    private Label buildStatusBar() {
        Label l = new Label();
        l.setAlignment(Pos.CENTER_LEFT);
        l.setStyle("-fx-padding: 4 8; -fx-background-color: #f2f2f2; -fx-font-size: 11px;");
        updateStatusBar();
        textArea.caretPositionProperty().addListener((o, a, b) -> updateStatusBar());
        textArea.selectionProperty().addListener((o, a, b) -> updateStatusBar());
        return l;
    }

    private void updateStatusBar() {
        if (statusBar == null) return;
        int caret = textArea.getCaretPosition();
        String text = controller.getText();
        int line = 1, col = 1;
        for (int i = 0; i < Math.min(caret, text.length()); i++) {
            if (text.charAt(i) == '\n') { line++; col = 1; } else { col++; }
        }
        int length = text.length();
        statusBar.setText(String.format("Ln %d, Col %d | Chars %d", line, col, length));
    }

    private void doFind(Stage stage) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Find");
        dlg.setHeaderText("찾을 문자열을 입력하세요");
        dlg.setContentText("Text:");
        dlg.initOwner(stage);
        dlg.showAndWait().ifPresent(s -> {
            if (s.isEmpty()) return;
            String content = controller.getText();
            int start = Math.max(0, textArea.getCaretPosition());
            int idx = content.indexOf(s, start);
            if (idx < 0 && start > 0) idx = content.indexOf(s); // wrap-around
            if (idx >= 0) {
                programmaticUpdate = true;
                try { textArea.selectRange(idx, idx + s.length()); }
                finally { programmaticUpdate = false; }
                textArea.requestFocus();
            } else {
                showError("찾기", new RuntimeException("문자열을 찾을 수 없습니다."));
            }
        });
    }
}

