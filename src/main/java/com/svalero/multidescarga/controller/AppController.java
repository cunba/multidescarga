package com.svalero.multidescarga.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class AppController implements Initializable {
    @FXML
    private TextField tfUrl;
    @FXML
    private Hyperlink hlPath;
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
    private DirectoryChooser directoryChooser;
    private ObservableList<Map<String, Object>> items;
    private Stage stage;

    public AppController(Stage stage) {
        this.directoryChooser = new DirectoryChooser();
        this.items = FXCollections.observableArrayList();
        this.id = 0;
        this.stage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String path = System.getProperty("user.dir") + "\\downloads";
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        directoryChooser.setInitialDirectory(new File(path));
        hlPath.setText(path);

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
    }

    @FXML
    protected void onHyperlinkPathClick() {
        File selectedDirectory = new File(hlPath.getText());
        directoryChooser.setInitialDirectory(new File(hlPath.getText()));
        selectedDirectory = directoryChooser.showDialog(stage);
        hlPath.setText(selectedDirectory.getAbsolutePath());
    }

    @FXML
    protected void onDownloadButtonClick() {
        String url = tfUrl.getText();
        tfUrl.setText("");

        try {
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            File file = new File(hlPath.getText() + "\\" + fileName);

            if (file.exists()) {
                int i = 1;
                String[] fileNameString = fileName.split("\\.");
                fileName = fileNameString[0] + " (" + i + ")." + fileNameString[1];
                file = new File(hlPath.getText() + "\\" + fileName);

                while (file.exists()) {
                    i++;
                    fileName = fileNameString[0] + " (" + i + ")." + fileNameString[1];
                    file = new File(hlPath.getText() + "\\" + fileName);
                }
            }

            file.createNewFile();

            DownloadTask downloadTask = new DownloadTask(url, file);

            // Timer para iniciar descarga
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            new Thread(downloadTask).start();
                        }
                    },
                    1000 * this.timeout);

            createNewRow(fileName, downloadTask);

        } catch (MalformedURLException murle) {
            murle.printStackTrace();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void createNewRow(String name, DownloadTask downloadTask) {
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

        Button btStop = new Button("Parar");

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

            if (newState == Worker.State.SUCCEEDED || newState == Worker.State.CANCELLED
                    || newState == Worker.State.FAILED) {

                btStop.setText("Eliminar");
                btStop.setOnAction(actionEvent -> {
                    downloadTasks.forEach((id, v) -> {
                        if (v == downloadTask)
                            items.removeIf(value -> value.get("id") == id);
                    });
                    downloadTask.deleteFile();
                });
            }
        });

        HBox progress = new HBox(progressBar, lbProgressText);

        downloadTasks.put(id, downloadTask);

        Map<String, Object> item = new HashMap<>();
        item.put("id", id);
        item.put("name", name);
        item.put("progress", progress);
        item.put("status", lbStatus);
        item.put("size", lbSize);
        item.put("time", lbTime);
        item.put("velocity", lbVelocity);
        item.put("stop", btStop);

        items.add(item);
        btStop.setOnAction(actionEvent -> downloadTask.cancel());
        id++;
    }

    @FXML
    protected void onCleanTableButtonClick() {
        downloadTasks.forEach((id, downloadTask) -> {
            if (!downloadTask.isRunning()) {
                items.removeIf(value -> value.get("id") == id);
            }
        });
    }
}