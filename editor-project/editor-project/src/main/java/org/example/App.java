package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {
    private Document document;
    private DocumentHistory history;
    private TextArea textArea;

    @Override
    public void start(Stage stage) {
        document = new Document();
        history = new DocumentHistory(document);
        textArea = new TextArea();

        textArea.setText(document.getText());

        textArea.addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcuts);

        textArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.equals(document.getText())) {
                String diff = newText.substring(document.getText().length());
                history.executeAdd(diff);
            }
        });

        BorderPane root = new BorderPane(textArea);
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
            textArea.setText(document.getText());
            textArea.positionCaret(document.getText().length());
            event.consume();
        } else if (redo.match(event) || redoAlt.match(event)) {
            history.redo();
            textArea.setText(document.getText());
            textArea.positionCaret(document.getText().length());
            event.consume();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}