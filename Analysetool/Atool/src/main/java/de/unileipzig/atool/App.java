package de.unileipzig.atool;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {


    private static Scene scene;

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {

        scene = new Scene(loadFXML("primary"));

        stage.setScene(scene);
        stage.setMaxWidth(1200);
        stage.setMaxHeight(800);
        stage.setMinHeight(600);
        stage.setMinWidth(600);
        stage.setOnHidden(e -> Platform.exit());
        stage.show();
    }

}