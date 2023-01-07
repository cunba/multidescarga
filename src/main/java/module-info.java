module com.svalero.multidescarga {
    requires javafx.controls;
    requires javafx.fxml;
            
                            
    opens com.svalero.multidescarga to javafx.fxml;
    exports com.svalero.multidescarga;
}