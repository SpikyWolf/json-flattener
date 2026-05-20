package com.spiky.jsonflattener;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class JsonFlattenerApplication extends Application {
    @Override
    public void start(@NotNull Stage stage) throws IOException {
        // First, we create the loader instance so we can talk to it later
        FXMLLoader fxmlLoader = new FXMLLoader(JsonFlattenerApplication.class.getResource("main-view.fxml"));

        // We run the load method and save the result as a Parent (the root of your UI)
        Scene scene = new Scene(fxmlLoader.load(), 640, 360);

        // After load() is called, the loader has created the MainViewController!
        // We grab that instance here:
        MainViewController controller = fxmlLoader.getController();

        // Now we create our data model and "inject" it into the controller
        JsonFlattenerModel model = new JsonFlattenerModel();
        controller.setModel(model);

        stage.setTitle("JSON Flattener");
        stage.setMinWidth(640);
        stage.setMinHeight(360);
        stage.setScene(scene);
        stage.show();
    }

}