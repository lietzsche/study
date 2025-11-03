package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
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

    @Override
    public void start(Stage stage) {
        document = new Document();
        history = new DocumentHistory(document);
        textArea = new TextArea();
        fileService = new FileService();

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
        stage.show();
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
                } catch (Exception ex) {
                    ex.printStackTrace();
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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
