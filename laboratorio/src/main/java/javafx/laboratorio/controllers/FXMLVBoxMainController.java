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
        // Este é o método que vamos implementar a seguir!
        AnchorPane a = (AnchorPane) FXMLLoader.load(getClass().getResource("/javafx/laboratorio/views/FXMLAnchorPaneCadastrosPesquisadores.fxml"));
        anchorPane.getChildren().setAll(a);
    }

    @FXML
    public void handleMenuItemProcessosReservas() throws IOException {
        // Como o processo de reservas também é sua responsabilidade, deixei preparado:
        // AnchorPane a = (AnchorPane) FXMLLoader.load(getClass().getResource("/javafx/laboratorio/views/FXMLAnchorPaneProcessosReservas.fxml"));
        // anchorPane.getChildren().setAll(a);
        System.out.println("Ecrã de Reservas será implementado em breve.");
    }

    // Os métodos abaixo estão vazios para evitar erros ao clicar nos menus 
    // antes de os respetivos ficheiros FXML existirem. A tua dupla pode preenchê-los depois.
    @FXML
    public void handleMenuItemCadastrosLaboratorios() { System.out.println("Não implementado"); }
    @FXML
    public void handleMenuItemProcessosManutencoes() { System.out.println("Não implementado"); }
    @FXML
    public void handleMenuItemGraficosConfiabilidade() { System.out.println("Não implementado"); }
    @FXML
    public void handleMenuItemRelatoriosOcupacao() { System.out.println("Não implementado"); }
    @FXML
    public void handleMenuItemRelatoriosIncidentes() { System.out.println("Não implementado"); }
}