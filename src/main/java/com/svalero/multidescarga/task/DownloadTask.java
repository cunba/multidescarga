package com.svalero.multidescarga.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.Instant;

import javafx.concurrent.Task;

public class DownloadTask extends Task<String> {

    private URL url;
    private File file;

    public DownloadTask(String urlText, File file) throws MalformedURLException {
        this.url = new URL(urlText);
        this.file = file;
    }

    @Override
    protected String call() throws Exception {
        updateMessage("Conectando");

        URLConnection urlConnection = url.openConnection();
        double fileSize = urlConnection.getContentLength();
        double megaSize = fileSize / 1048576;
        updateValue(Math.round(megaSize * 100) / 100 + "MB");

        BufferedInputStream in = new BufferedInputStream(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte dataBuffer[] = new byte[1024];
        int bytesRead;
        int totalRead = 0;
        double downloadProgress = 0;

        Instant start = Instant.now();
        Instant current;
        float elapsedTime;

        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            downloadProgress = Math.round(((double) totalRead / fileSize) * 100);

            updateProgress(downloadProgress, 100);

            current = Instant.now();
            elapsedTime = Duration.between(start, current).toSeconds();
            if (Math.round(elapsedTime) < 60)
                updateMessage(Math.round(elapsedTime) + " sec.");
            else
                updateMessage(Math.round(elapsedTime / 60) + " min. " + Math.round(elapsedTime % 60) + " sec.");

            // Comentar para acelerar la descarga.
            Thread.sleep(1);

            fileOutputStream.write(dataBuffer, 0, bytesRead);
            totalRead += bytesRead;

            if (isCancelled()) {
                return null;
            }
        }

        updateProgress(100, 100);

        return null;
    }
}
