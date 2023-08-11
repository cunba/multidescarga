package com.svalero.multidescarga.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordDownloadData {
    String date;
    String name;
    String status;
    String size;
    String totalTime;
}