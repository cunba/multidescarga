package com.svalero.multidescarga.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.svalero.multidescarga.util.R;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class AppController implements Initializable {
    @FXML
    TabPane tabPane;
    @FXML
    Tab tabDownloads;
    @FXML
    Tab tabDownloadsFromFile;
    @FXML
    Tab tabRecord;

    Stage stage;

    public AppController(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FXMLLoader loaderDownloads = new FXMLLoader();
        FXMLLoader loaderDownloadsFromFile = new FXMLLoader();
        FXMLLoader loaderRecord = new FXMLLoader();

        RecordController recordController = new RecordController();
        loaderRecord.setLocation(R.getUI("record-view.fxml"));
        loaderRecord.setController(recordController);

        DownloadsController downloadsController = new DownloadsController(stage, recordController);
        loaderDownloads.setLocation(R.getUI("downloads-view.fxml"));
        loaderDownloads.setController(downloadsController);

        DownloadsFromFileController downloadsFromFileController = new DownloadsFromFileController(stage,
                recordController);
        loaderDownloadsFromFile.setLocation(R.getUI("downloads_from_file-view.fxml"));
        loaderDownloadsFromFile.setController(downloadsFromFileController);

        try {
            ScrollPane downloads = loaderDownloads.load();
            tabDownloads.setContent(downloads);
            ScrollPane downloadsFromFile = loaderDownloadsFromFile.load();
            tabDownloadsFromFile.setContent(downloadsFromFile);
            ScrollPane record = loaderRecord.load();
            tabRecord.setContent(record);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Al cerrar la aplicación se pararán todas las descargas en curso");
        stage.setOnCloseRequest((t) -> {
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                downloadsController.onCloseRequest();
                downloadsFromFileController.onCloseRequest();
                stage.close();
            } else {
                t.consume();
            }
        });
    }
}