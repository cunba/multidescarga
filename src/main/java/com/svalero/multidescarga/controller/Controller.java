package com.svalero.multidescarga.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class Controller {
    @FXML
    private TextField tfUrl;

    @FXML
    protected void onDownloadButtonClick() {
        tfUrl.setText("Welcome to JavaFX Application!");
    }
}