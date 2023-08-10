package com.svalero.multidescarga.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.svalero.multidescarga.task.DownloadTask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.HBox;

public class AppController implements Initializable {
    @FXML
    private TextField tfUrl;
    @FXML
    private TableColumn<Map, String> tcName;
    @FXML
    private TableColumn<Map, Object> tcProgress;
    @FXML
    private TableColumn<Map, String> tcStatus;
    @FXML
    private TableColumn<Map, String> tcSize;
    @FXML
    private TableColumn<Map, String> tcTime;
    @FXML
    private TableColumn<Map, Object> tcStop;
    @FXML
    private TableView<Map<String, Object>> tvDownloads;

    private DownloadTask downloadTask;
    private int timeout = 1;

    private ObservableList<Map<String, Object>> items;

    public AppController() {
        this.items = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tcName.setCellValueFactory(new MapValueFactory<>("name"));
        tcProgress.setCellValueFactory(new MapValueFactory<>("progress"));
        tcStatus.setCellValueFactory(new MapValueFactory<>("status"));
        tcSize.setCellValueFactory(new MapValueFactory<>("size"));
        tcTime.setCellValueFactory(new MapValueFactory<>("time"));
        tcStop.setCellValueFactory(new MapValueFactory<>("stop"));

        tvDownloads.setItems(items);
    }

    @FXML
    protected void onDownloadButtonClick() {
        String url = tfUrl.getText();

        tfUrl.setText("");

        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            File file = new File(fileName);
            file.createNewFile();

            downloadTask = new DownloadTask(url, file);

            ProgressBar progressBar = new ProgressBar(0);

            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(downloadTask.progressProperty());

            Label lbStatus = new Label("Descargando");
            downloadTask.stateProperty().addListener((observableValue, oldState,
                    newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    lbStatus.setText("Completado");
                }
            });

            Label lbProgressText = new Label("  0 %");
            downloadTask.progressProperty()
                    .addListener((observableValue, oldValue, newValue) -> lbProgressText
                            .setText("  " + Math.round(newValue.floatValue() * 100) + "%"));

            Label lbSize = new Label("");
            downloadTask.valueProperty()
                    .addListener((observableValue, oldValue, newValue) -> {
                        if (newValue != null)
                            lbSize.setText(newValue);
                    });

            Label lbTime = new Label("0 sec.");
            downloadTask.messageProperty()
                    .addListener((observableValue, oldValue, newValue) -> lbTime.setText(newValue));

            // Timer para iniciar descarga
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            new Thread(downloadTask).start();
                        }
                    },
                    1000 * this.timeout);

            createNewRow(fileName, progressBar, lbProgressText, lbStatus, lbSize, lbTime);

        } catch (MalformedURLException murle) {
            murle.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void createNewRow(String name, ProgressBar progressBar, Label progressText, Label status, Label size,
            Label time) {
        Map<String, Object> item = new HashMap<>();

        HBox progress = new HBox(progressBar, progressText);
        Button btStop = new Button("Parar");
        btStop.setOnAction(actionEvent -> System.out.println("Ha pulsado el boton de " + name));

        item.put("name", name);
        item.put("progress", progress);
        item.put("status", status);
        item.put("size", size);
        item.put("time", time);
        item.put("stop", btStop);

        items.add(item);
    }

}