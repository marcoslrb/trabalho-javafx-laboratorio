module javafx.laboratorio {
    requires javafx.controls;
    requires javafx.fxml;

    opens javafx.laboratorio to javafx.fxml;
    exports javafx.laboratorio;
}
