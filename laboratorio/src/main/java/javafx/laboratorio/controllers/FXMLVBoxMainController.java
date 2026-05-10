package javafx.laboratorio.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;

public class FXMLVBoxMainController implements Initializable {

    @FXML
    private MenuItem menuItemCadastrosPesquisadores;
    @FXML
    private MenuItem menuItemCadastrosLaboratorios;
    @FXML
    private MenuItem menuItemProcessosReservas;
    @FXML
    private MenuItem menuItemProcessosManutencoes;
    @FXML
    private MenuItem menuItemGraficosConfiabilidade;
    @FXML
    private MenuItem menuItemRelatoriosOcupacao;
    @FXML
    private MenuItem menuItemRelatoriosIncidentes;

    @FXML
    private AnchorPane anchorPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicialização primária, se necessária
    }

    // --- MÉTODOS PARA CARREGAR OS ECRÃS --- //
    @FXML
    public void handleMenuItemCadastrosPesquisadores() throws IOException {
        AnchorPane a = (AnchorPane) FXMLLoader.load(getClass().getResource("/javafx/laboratorio/views/FXMLAnchorPaneCadastrosPesquisadores.fxml"));
        anchorPane.getChildren().setAll(a);
    }

    @FXML
    public void handleMenuItemProcessosReservas() throws IOException {
        // AnchorPane a = (AnchorPane) FXMLLoader.load(getClass().getResource("/javafx/laboratorio/views/FXMLAnchorPaneProcessosReservas.fxml"));
        // anchorPane.getChildren().setAll(a);
        System.out.println("Ecrã de Reservas será implementado em breve.");
    }

    @FXML
    public void handleMenuItemCadastrosLaboratorios() throws IOException {
        AnchorPane a = (AnchorPane) FXMLLoader.load(getClass().getResource("/javafx/laboratorio/views/FXMLAnchorPaneCadastrosLaboratorios.fxml"));
        anchorPane.getChildren().setAll(a);
    }
    @FXML
    public void handleMenuItemProcessosManutencoes() throws IOException {
        AnchorPane a = (AnchorPane) FXMLLoader.load(getClass().getResource("/javafx/laboratorio/views/FXMLAnchorPaneProcessosManutencoes.fxml"));
        anchorPane.getChildren().setAll(a);
    }
    @FXML
    public void handleMenuItemGraficosConfiabilidade() { System.out.println("Não implementado"); }
    @FXML
    public void handleMenuItemRelatoriosOcupacao() { System.out.println("Não implementado"); }
    @FXML
    public void handleMenuItemRelatoriosIncidentes() { System.out.println("Não implementado"); }
}