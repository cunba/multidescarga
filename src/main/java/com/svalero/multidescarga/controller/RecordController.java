package com.svalero.multidescarga.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.svalero.multidescarga.data.RecordDownloadData;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class RecordController implements Initializable {
    @FXML
    private TableColumn<RecordDownloadData, String> tcDate;
    @FXML
    private TableColumn<RecordDownloadData, String> tcName;
    @FXML
    private TableColumn<RecordDownloadData, String> tcStatus;
    @FXML
    private TableColumn<RecordDownloadData, String> tcSize;
    @FXML
    private TableColumn<RecordDownloadData, String> tcTotalTime;
    @FXML
    private TableView<RecordDownloadData> tvRecord;

    private ObservableList<RecordDownloadData> items;
    private BufferedReader reader;
    private FileReader fr;

    public RecordController() {
        this.items = FXCollections.observableArrayList();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tcDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        tcName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tcStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tcSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        tcTotalTime.setCellValueFactory(new PropertyValueFactory<>("totalTime"));
        tvRecord.setItems(items);

        try {
            fr = new FileReader("downloadsRecord.log");
            reader = new BufferedReader(fr);

            String line;
            while (((line = reader.readLine()) != null) && !line.isEmpty()) {
                String[] rowParameters = line.split(";");
                RecordDownloadData rdd = new RecordDownloadData(
                        rowParameters[0],
                        rowParameters[1],
                        rowParameters[2],
                        rowParameters[3],
                        rowParameters[4]);
                items.add(rdd);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNewDownload() {
        String row;
        try {
            row = reader.readLine();
            String[] rowParameters = row.split(";");
            RecordDownloadData rdd = new RecordDownloadData(
                    rowParameters[0],
                    rowParameters[1],
                    rowParameters[2],
                    rowParameters[3],
                    rowParameters[4]);
            items.add(rdd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeReader() {
        try {
            reader.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
