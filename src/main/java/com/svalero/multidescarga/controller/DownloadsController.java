package com.svalero.multidescarga.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import com.svalero.multidescarga.data.DownloadData;
import com.svalero.multidescarga.task.DownloadTask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class DownloadsController implements Initializable {
    @FXML
    private TextField tfUrl;
    @FXML
    private Hyperlink hlPath;
    @FXML
    private TableColumn<DownloadData, String> tcId;
    @FXML
    private TableColumn<DownloadData, String> tcName;
    @FXML
    private TableColumn<DownloadData, Object> tcProgress;
    @FXML
    private TableColumn<DownloadData, String> tcStatus;
    @FXML
    private TableColumn<DownloadData, String> tcSize;
    @FXML
    private TableColumn<DownloadData, String> tcTime;
    @FXML
    private TableColumn<DownloadData, String> tcVelocity;
    @FXML
    private TableColumn<DownloadData, Object> tcButton;
    @FXML
    private TableView<DownloadData> tvDownloads;

    private Map<Integer, DownloadTask> downloadTasks;
    private int timeout = 1;
    private int id;
    private DirectoryChooser directoryChooser;
    private ObservableList<DownloadData> items;
    private Stage stage;

    public DownloadsController(Stage stage) {
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

        tcId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tcProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        tcStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tcSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        tcTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        tcVelocity.setCellValueFactory(new PropertyValueFactory<>("velocity"));
        tcButton.setCellValueFactory(new PropertyValueFactory<>("button"));

        tvDownloads.setItems(items);
        downloadTasks = new HashMap<>();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Al cerrar la aplicación se pararán todas las descargas en curso");
        stage.setOnCloseRequest((t) -> {
            if (!downloadTasks.isEmpty()) {
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    downloadTasks.forEach((id, downloadTask) -> downloadTask.cancel());
                    stage.close();
                } else {
                    t.consume();
                }
            }
        });
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

        Button btButton = new Button("Parar");

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

                btButton.setText("Eliminar");
                btButton.setOnAction(actionEvent -> {
                    downloadTask.deleteFile();
                    downloadTasks.forEach((id, v) -> {
                        if (v == downloadTask) {
                            items.removeIf(value -> value.getId() == id);
                            downloadTasks.remove(id);
                        }
                    });
                });
            }
        });

        HBox progress = new HBox(progressBar, lbProgressText);

        downloadTasks.put(id, downloadTask);

        DownloadData newDownload = new DownloadData(
                id,
                name,
                progress,
                lbStatus,
                lbSize,
                lbTime,
                lbVelocity,
                btButton);

        items.add(newDownload);
        btButton.setOnAction(actionEvent -> downloadTask.cancel());
        id++;
    }

    @FXML
    protected void onCleanTableButtonClick() {
        downloadTasks.forEach((id, downloadTask) -> {
            if (!downloadTask.isRunning()) {
                items.removeIf(value -> value.getId() == id);
                downloadTasks.remove(id);
            }
        });
    }
}