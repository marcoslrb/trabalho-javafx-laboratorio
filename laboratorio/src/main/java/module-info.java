module javafx.laboratorio {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens javafx.laboratorio to javafx.fxml;
    exports javafx.laboratorio;
}
