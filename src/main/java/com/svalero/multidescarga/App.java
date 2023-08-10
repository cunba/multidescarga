package com.svalero.multidescarga;

import java.io.IOException;

import com.svalero.multidescarga.controller.AppController;
import com.svalero.multidescarga.util.R;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void init() throws Exception {
        super.init();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();

        loader.setLocation(R.getUI("main-view.fxml"));
        loader.setController(new AppController());
        ScrollPane scrollPane = loader.load();

        Scene scene = new Scene(scrollPane);
        stage.setTitle("Downloader");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

    public static void main(String[] args) {
        launch();
    }
}