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
    private TableColumn<Map, String> tcId;
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
    private TableColumn<Map, String> tcVelocity;
    @FXML
    private TableColumn<Map, Object> tcStop;
    @FXML
    private TableView<Map<String, Object>> tvDownloads;

    private Map<Integer, DownloadTask> downloadTasks;
    private int timeout = 1;
    private int id;

    private ObservableList<Map<String, Object>> items;

    public AppController() {
        this.items = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tcId.setCellValueFactory(new MapValueFactory<>("id"));
        tcName.setCellValueFactory(new MapValueFactory<>("name"));
        tcProgress.setCellValueFactory(new MapValueFactory<>("progress"));
        tcStatus.setCellValueFactory(new MapValueFactory<>("status"));
        tcSize.setCellValueFactory(new MapValueFactory<>("size"));
        tcTime.setCellValueFactory(new MapValueFactory<>("time"));
        tcVelocity.setCellValueFactory(new MapValueFactory<>("velocity"));
        tcStop.setCellValueFactory(new MapValueFactory<>("stop"));

        tvDownloads.setItems(items);
        downloadTasks = new HashMap<>();
        id = 0;
    }

    @FXML
    protected void onDownloadButtonClick() {
        String url = tfUrl.getText();

        tfUrl.setText("");

        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            File file = new File(fileName);
            file.createNewFile();

            DownloadTask downloadTask = new DownloadTask(url, file);

            Label lbProgressText = new Label("  0 %");
            downloadTask.progressProperty()
                    .addListener((observableValue, oldValue, newValue) -> lbProgressText
                            .setText("  " + Math.round(newValue.floatValue() * 100) + "%"));

            Label lbSize = new Label("");
            downloadTask.valueProperty()
                    .addListener((observableValue, oldValue, newValue) -> lbSize
                            .setText(newValue != null ? newValue : oldValue));

            Label lbTime = new Label("0 sec.");
            Label lbVelocity = new Label("");
            downloadTask.messageProperty()
                    .addListener((observableValue, oldValue, newValue) -> {
                        String[] newValueArray = newValue.split(";");
                        lbVelocity.setText(newValueArray[0]);
                        lbTime.setText(newValueArray[1]);
                    });

            ProgressBar progressBar = new ProgressBar(0);
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(downloadTask.progressProperty());

            Label lbStatus = new Label("Conectando");
            downloadTask.stateProperty().addListener((observableValue, oldState,
                    newState) -> {
                if (newState == Worker.State.SUCCEEDED)
                    lbStatus.setText("Completada");
                else if (newState == Worker.State.RUNNING)
                    lbStatus.setText("Descargando");
                else if (newState == Worker.State.CANCELLED)
                    lbStatus.setText("Cancelada");
                else if (newState == Worker.State.FAILED)
                    lbStatus.setText("Fallo");
            });

            // Timer para iniciar descarga
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            new Thread(downloadTask).start();
                        }
                    },
                    1000 * this.timeout);

            createNewRow(fileName,
                    progressBar,
                    lbProgressText,
                    lbStatus,
                    lbSize,
                    lbTime,
                    lbVelocity,
                    downloadTask);

        } catch (MalformedURLException murle) {
            murle.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void createNewRow(String name, ProgressBar progressBar, Label progressText, Label status, Label size,
            Label time, Label velocity, DownloadTask downloadTask) {

        Map<String, Object> item = new HashMap<>();

        HBox progress = new HBox(progressBar, progressText);
        Button btStop = new Button("Parar");

        downloadTasks.put(id, downloadTask);

        item.put("id", id);
        item.put("name", name);
        item.put("progress", progress);
        item.put("status", status);
        item.put("size", size);
        item.put("time", time);
        item.put("velocity", velocity);
        item.put("stop", btStop);

        items.add(item);
        btStop.setOnAction(actionEvent -> cancelDownload(item, btStop));
        id++;
    }

    private void cancelDownload(Map<String, Object> item, Button btStop) {
        DownloadTask downloadTask = downloadTasks.get(item.get("id").hashCode());
        if (downloadTask != null) {
            downloadTask.cancel();

            btStop.setText("Borrar");
            btStop.setOnAction(actionEvent -> {
                downloadTask.deleteFile();
                items.remove(item);
            });
        }

    }

}