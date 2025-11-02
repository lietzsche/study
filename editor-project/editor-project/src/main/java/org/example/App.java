package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        TextArea textArea = new TextArea();
        Scene scene = new Scene(textArea, 600, 400);
        stage.setScene(scene);
        stage.setTitle("TDD Text Editor Prototype");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}