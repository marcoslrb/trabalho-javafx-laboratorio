module javafx.laboratorio {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    requires jasperreports;
    requires java.desktop;

    opens javafx.laboratorio to javafx.fxml;
    opens javafx.laboratorio.controllers to javafx.fxml;
    opens javafx.laboratorio.models.domain to javafx.base;
    opens sockets.thread to javafx.base;

    exports javafx.laboratorio;
    exports javafx.laboratorio.services;
}
