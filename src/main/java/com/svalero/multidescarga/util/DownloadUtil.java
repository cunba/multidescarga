package com.svalero.multidescarga.util;

import java.io.File;
import java.io.IOException;

import com.svalero.multidescarga.data.DownloadData;
import com.svalero.multidescarga.task.DownloadTask;

import javafx.concurrent.Worker;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

public class DownloadUtil {

    public DownloadTask downloadFile(String fileName, String url, String directory)
            throws IOException {
        File file = new File(directory + File.separator + fileName);

        if (file.exists()) {
            int i = 1;
            String[] fileNameString = fileName.split(File.separator + ".");
            fileName = fileNameString[0] + " (" + i + ")." + fileNameString[1];
            file = new File(directory + File.separator + fileName);

            while (file.exists()) {
                i++;
                fileName = fileNameString[0] + " (" + i + ")." + fileNameString[1];
                file = new File(directory + File.separator + fileName);
            }
        }

        file.createNewFile();

        DownloadTask downloadTask;
        downloadTask = new DownloadTask(url, file);

        return downloadTask;
    }

    public DownloadData createNewRow(DownloadTask downloadTask, int id, String name) {
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
        });

        HBox progress = new HBox(progressBar, lbProgressText);
        return new DownloadData(
                id,
                name,
                progress,
                lbStatus,
                lbSize,
                lbTime,
                lbVelocity,
                btButton);
    }
}
