package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {
    private Document document;
    private DocumentHistory history;
    private TextArea textArea;
    private FileService fileService;
    private java.nio.file.Path currentFile;
    private boolean programmaticUpdate = false;
    private String lastSavedText = "";
    private AutoSaveService autoSaveService;

    @Override
    public void start(Stage stage) {
        document = new Document();
        history = new DocumentHistory(document);
        textArea = new TextArea();
        fileService = new FileService();
        autoSaveService = new AutoSaveService(
                document,
                fileService,
                this::autoSavePath,
                2_000,
                java.time.Clock.systemDefaultZone()
        );

        textArea.setText(document.getText());

        textArea.addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcuts);

        textArea.textProperty().addListener((obs, oldText, newText) -> {
            if (programmaticUpdate) return;
            if (!newText.equals(document.getText())) {
                history.executeSet(newText);
            }
        });

        MenuBar menuBar = createMenuBar(stage);
        BorderPane root = new BorderPane(textArea);
        root.setTop(menuBar);
        Scene scene = new Scene(root, 600, 400);

        stage.setScene(scene);
        stage.setTitle("TDD Text Editor Prototype");
        stage.setOnCloseRequest(e -> {
            if (!confirmClose(stage)) {
                e.consume();
            } else {
                autoSaveService.stop();
            }
        });
        stage.show();

        // 앱 시작 후 자동저장 시작
        autoSaveService.start();
    }

    private void handleShortcuts(KeyEvent event) {
        KeyCombination undo = new KeyCodeCombination(javafx.scene.input.KeyCode.Z, KeyCombination.CONTROL_DOWN);
        KeyCombination redo = new KeyCodeCombination(javafx.scene.input.KeyCode.Y, KeyCombination.CONTROL_DOWN);
        KeyCombination redoAlt = new KeyCodeCombination(
                javafx.scene.input.KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);

        if (undo.match(event)) {
            history.undo();
            applyDocumentToEditor();
            event.consume();
        } else if (redo.match(event) || redoAlt.match(event)) {
            history.redo();
            applyDocumentToEditor();
            event.consume();
        }
    }

    private void applyDocumentToEditor() {
        programmaticUpdate = true;
        try {
            textArea.setText(document.getText());
            textArea.positionCaret(document.getText().length());
        } finally {
            programmaticUpdate = false;
        }
    }

    private MenuBar createMenuBar(Stage stage) {
        Menu menuFile = new Menu("File");
        MenuItem miNew = new MenuItem("New");
        MenuItem miOpen = new MenuItem("Open...");
        MenuItem miSave = new MenuItem("Save");
        MenuItem miSaveAs = new MenuItem("Save As...");
        MenuItem miExit = new MenuItem("Exit");

        miNew.setOnAction(e -> {
            history.executeSet("");
            currentFile = null;
            stage.setTitle("TDD Text Editor Prototype");
            applyDocumentToEditor();
            lastSavedText = document.getText();
        });

        miOpen.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Open File");
            java.io.File file = chooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    String content = fileService.load(file.toPath());
                    history.executeSet(content);
                    currentFile = file.toPath();
                    stage.setTitle(file.getName() + " - TDD Text Editor");
                    applyDocumentToEditor();
                    lastSavedText = document.getText();
                } catch (Exception ex) {
                    showError("파일 열기 실패", ex);
                }
            }
        });

        miSave.setOnAction(e -> doSave(stage, false));
        miSaveAs.setOnAction(e -> doSave(stage, true));
        miExit.setOnAction(e -> stage.close());

        menuFile.getItems().addAll(miNew, miOpen, miSave, miSaveAs, miExit);

        Menu menuEdit = new Menu("Edit");
        MenuItem miUndo = new MenuItem("Undo");
        MenuItem miRedo = new MenuItem("Redo");
        miUndo.setOnAction(e -> { history.undo(); applyDocumentToEditor(); });
        miRedo.setOnAction(e -> { history.redo(); applyDocumentToEditor(); });
        menuEdit.getItems().addAll(miUndo, miRedo);

        return new MenuBar(menuFile, menuEdit);
    }

    private void doSave(Stage stage, boolean forceChoose) {
        try {
            if (currentFile == null || forceChoose) {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Save File");
                java.io.File file = chooser.showSaveDialog(stage);
                if (file == null) return;
                currentFile = file.toPath();
            }
            fileService.save(currentFile, textArea.getText());
            stage.setTitle(currentFile.getFileName().toString() + " - TDD Text Editor");
            lastSavedText = document.getText();
        } catch (Exception ex) {
            showError("파일 저장 실패", ex);
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private boolean isDirty() {
        return !document.getText().equals(lastSavedText);
    }

    private boolean confirmClose(Stage stage) {
        if (!isDirty()) return true;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "변경 사항이 있습니다. 저장하시겠습니까?",
                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setTitle("종료 확인");
        alert.initOwner(stage);
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (result == ButtonType.YES) {
            doSave(stage, currentFile == null);
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
            if (currentFile != null) {
                return currentFile.resolveSibling(currentFile.getFileName().toString() + ".autosave");
            }
            String tmp = System.getProperty("java.io.tmpdir");
            return java.nio.file.Paths.get(tmp).resolve("editor-project-autosave.txt");
        } catch (Exception e) {
            return null;
        }
    }
}
