package com.svalero.multidescarga.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
//Quitar comentarios para visualizar tiempo con Libería Estándar de Java
//import java.time.Duration;
//import java.time.Instant;

import javafx.concurrent.Task;

public class DownloadTask extends Task<Integer> {

    private URL url;
    private File file;

    public DownloadTask(String urlText, File file) throws MalformedURLException {
        this.url = new URL(urlText);
        this.file = file;
    }

    @Override
    protected Integer call() throws Exception {
        updateMessage("Conectando con el servidor . . .");

        URLConnection urlConnection = url.openConnection();
        double fileSize = urlConnection.getContentLength();
        // Quitar comentario para limitar el tamaño de la descarga.
        double megaSize = fileSize / 1048576;

        BufferedInputStream in = new BufferedInputStream(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte dataBuffer[] = new byte[1024];
        int bytesRead;
        int totalRead = 0;
        double downloadProgress = 0;
        // Quitar comentarios para visualizar tiempo con Libería Estándar de Java
        /*
         * Instant start = Instant.now();
         * Instant current;
         * float elapsedTime;
         */

        // Quitar comentarios para visualizar tiempo con Libería de Apache Commons
        /*
         * StopWatch watch = new StopWatch();
         * watch.start();
         */

        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            downloadProgress = ((double) totalRead / fileSize);

            updateProgress(downloadProgress, 1);
            updateMessage(Math.round(downloadProgress * 100) + "%");

            // Quitar comentarios para visualizar tiempo con Libería Estándar de Java
            /*
             * current = Instant.now();
             * elapsedTime = Duration.between(start, current).toSeconds();
             * updateMessage(Math.round(downloadProgress * 100) + " %\t\t\t\t" +
             * Math.round(elapsedTime) + "sec.");
             */
            // Quitar comentarios para visualizar tiempo con Libería de Apache Commons
            // updateMessage(Math.round(downloadProgress * 100) + " %\t\t\t\t" +
            // Math.round(watch.getTime()/1000)+ " sec.");

            // Comentar para acelerar la descarga.
            Thread.sleep(1);

            fileOutputStream.write(dataBuffer, 0, bytesRead);
            totalRead += bytesRead;

            if (isCancelled()) {
                return null;
            }
        }

        updateProgress(1, 1);
        updateMessage("100 %");

        return null;
    }
}
