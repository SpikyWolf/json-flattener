package io.github.spiky.jsonflattener;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

public class JsonFlattenerApplication extends Application {
    @Override
    public void start(@NotNull Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(JsonFlattenerApplication.class.getResource("main-view.fxml")));
        JsonFlattenerModel model = new JsonFlattenerModel();
        Scene scene = new Scene(root, 640, 360);

        root.applyCss();
        root.layout();

        new MainViewController(root, model);

        stage.setTitle("JSON Flattener");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/io/github/spiky/jsonflattener/icon.png"))));
        stage.setMinWidth(640);
        stage.setMinHeight(360);
        stage.setScene(scene);
        stage.show();

    }
}
