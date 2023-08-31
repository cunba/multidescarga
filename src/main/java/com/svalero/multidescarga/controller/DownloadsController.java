package com.svalero.multidescarga.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.svalero.multidescarga.data.DownloadData;
import com.svalero.multidescarga.task.DownloadTask;
import com.svalero.multidescarga.util.DownloadUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
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
    private int id;
    private DirectoryChooser directoryChooser;
    private ObservableList<DownloadData> items;
    private Stage stage;
    private FileWriter fw;
    private BufferedWriter writer;
    private RecordController recordController;
    private DownloadUtil downloadUtil;

    public DownloadsController(Stage stage, RecordController recordController) {
        this.directoryChooser = new DirectoryChooser();
        this.items = FXCollections.observableArrayList();
        this.id = 0;
        this.stage = stage;
        this.recordController = recordController;
        downloadUtil = new DownloadUtil();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            fw = new FileWriter("downloadsRecord.log", true);
            writer = new BufferedWriter(fw);
            String path = System.getProperty("user.dir") + File.separator + "downloads";
            Files.createDirectories(Paths.get(path));
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
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            String fileName = url.substring(url.lastIndexOf(File.separator) + 1);
            DownloadTask downloadTask = downloadUtil.downloadFile(fileName, url, hlPath.getText());
            createNewRow(fileName, downloadTask);
            new Thread(downloadTask).start();
        } catch (MalformedURLException murle) {
            murle.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createNewRow(String name, DownloadTask downloadTask) {
        DownloadData newDownload = downloadUtil.createNewRow(downloadTask, id, name);

        Label totalTime = new Label("");
        Label downloadSize = new Label("");
        downloadTask.messageProperty()
                .addListener((observableValue, oldValue, newValue) -> {
                    String[] newValueArray = newValue.split(";");
                    try {
                        newDownload.getVelocity().setText(newValueArray[0]);
                        totalTime.setText(newValueArray[1]);
                        newDownload.getTime().setText(newValueArray[2]);
                        downloadSize.setText(newValueArray[3]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        downloadTask.stateProperty().addListener((observableValue, oldState,
                newState) -> {
            if (newState == Worker.State.SUCCEEDED || newState == Worker.State.CANCELLED
                    || newState == Worker.State.FAILED) {

                try {
                    writer.write(
                            new Date().toString() + ";" +
                                    name + ";" +
                                    newDownload.getStatus().getText() + ";" +
                                    downloadSize.getText() + ";" +
                                    totalTime.getText() + "\n");
                    writer.flush();
                    recordController.addNewDownload();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                newDownload.getButton().setText("Eliminar");
                newDownload.getButton().setOnAction(actionEvent -> {
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

        downloadTasks.put(id, downloadTask);
        items.add(newDownload);
        newDownload.getButton().setOnAction(actionEvent -> downloadTask.cancel());
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

    public void onCloseRequest() {
        if (!downloadTasks.isEmpty())
            downloadTasks.forEach((id, downloadTask) -> {
                downloadTask.cancel();
                try {
                    recordController.closeReader();
                    writer.close();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
    }
}
