package com.svalero.multidescarga.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.svalero.multidescarga.util.R;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
    Tab tabRecord;

    Stage stage;

    public AppController(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Tab tabSelected = tabPane.getSelectionModel().getSelectedItem();
        FXMLLoader loaderDownloads = new FXMLLoader();
        FXMLLoader loaderRecord = new FXMLLoader();

        loaderDownloads.setLocation(R.getUI("downloads-view.fxml"));
        loaderDownloads.setController(new DownloadsController(stage));

        loaderRecord.setLocation(R.getUI("record-view.fxml"));
        loaderRecord.setController(new RecordController());

        if (tabSelected == tabDownloads)
            try {
                ScrollPane downloads = loaderDownloads.load();
                tabDownloads.setContent(downloads);
                ScrollPane record = loaderRecord.load();
                tabRecord.setContent(record);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        else
            try {
                ScrollPane record = loaderRecord.load();
                tabRecord.setContent(record);
                ScrollPane downloads = loaderDownloads.load();
                tabDownloads.setContent(downloads);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

}