package com.svalero.multidescarga.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.svalero.multidescarga.task.DownloadTask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class AppController {
    @FXML
    private TextField tfUrl;
    @FXML
    private TableColumn<Map<String, Object>, String> tcName;
    @FXML
    private TableColumn<Map<String, Object>, Object> tcProgress;
    @FXML
    private TableColumn<Map<String, Object>, String> tcStatus;
    @FXML
    private TableColumn<Map<String, Object>, String> tcSize;
    @FXML
    private TableColumn<Map<String, Object>, Object> tcStop;
    @FXML
    private TableView<Map<String, Object>> tableView;

    private DownloadTask downloadTask;
    private int timeout = 1;

    private ObservableList<Map<String, Object>> items;

    public AppController() {
        this.items = FXCollections.<Map<String, Object>>observableArrayList();
        tcName = new TableColumn<>("name");
        tcName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tcProgress = new TableColumn<>("progress");
        tcProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        tcStatus = new TableColumn<>("status");
        tcStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tcSize = new TableColumn<>("size");
        tcSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        tcStop = new TableColumn<>("stop");
        tcStop.setCellValueFactory(new PropertyValueFactory<>("stop"));

        // tableView.getColumns().add(tcName);
        // tableView.getColumns().add(tcProgress);
        // tableView.getColumns().add(tcStatus);
        // tableView.getColumns().add(tcSize);
        // tableView.getColumns().add(tcStop);
    }

    @FXML
    protected void onDownloadButtonClick() {
        String url = tfUrl.getText();

        tfUrl.setText("");
        String filename = url.substring(url.lastIndexOf("/") + 1);
        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            File file = new File(fileName);
            file.createNewFile();

            downloadTask = new DownloadTask(url, file);

            ProgressBar progressBar = new ProgressBar(0);

            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(downloadTask.progressProperty());

            downloadTask.stateProperty().addListener((observableValue, oldState,
                    newState) -> {
                System.out.println(observableValue.toString());
                if (newState == Worker.State.SUCCEEDED) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText("La descarga ha terminado");
                    alert.show();
                }
            });

            Label lbStatus = new Label("In progress");
            downloadTask.messageProperty()
                    .addListener((observableValue, oldValue, newValue) -> lbStatus.setText(newValue));

            // Timer para iniciar descarga
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            new Thread(downloadTask).start();
                        }
                    },
                    1000 * this.timeout);

            createNewRow(fileName, progressBar, lbStatus, "0MB");

        } catch (MalformedURLException murle) {
            murle.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void createNewRow(String name, ProgressBar progressBar, Label status, String size) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("progress", progressBar);
        item.put("status", status);
        item.put("size", size);
        item.put("stop", "");

        items.add(item);
        tableView.getItems().addAll(items);
    }
}