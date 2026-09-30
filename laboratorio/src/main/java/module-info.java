module javafx.laboratorio {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;
    requires java.sql;
    requires jasperreports;
    requires java.desktop;
    // gRPC / Protobuf (consulta remota de laboratórios)
    requires io.grpc;
    requires io.grpc.netty.shaded;
    requires io.grpc.protobuf;
    requires io.grpc.stub;
    // O jar protobuf-java nao declara Automatic-Module-Name,
    // entao o nome do modulo derivado do arquivo eh "protobuf.java"
    requires protobuf.java;
    // Guava (usado pelos stubs gerados: ListenableFuture)
    requires com.google.common;
    // Anotacao @Generated presente no codigo gerado do gRPC
    requires java.annotation;

    opens javafx.laboratorio to javafx.fxml;
    opens javafx.laboratorio.controllers to javafx.fxml;
    opens javafx.laboratorio.models.domain to javafx.base;

    exports javafx.laboratorio;
    exports javafx.laboratorio.services;
}
