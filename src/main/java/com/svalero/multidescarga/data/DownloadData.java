package com.svalero.multidescarga.data;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadData {
    int id;
    String name;
    HBox progress;
    Label status;
    Label size;
    Label time;
    Label velocity;
    Button button;
}
