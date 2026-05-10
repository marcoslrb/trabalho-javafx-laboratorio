package javafx.laboratorio.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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

    private void carregarTelaNoCentro(String caminhoFxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFxml));
        Parent conteudo = loader.load();

        // Garante que o conteúdo ocupe 100% da área do AnchorPane principal.
        AnchorPane.setTopAnchor(conteudo, 0.0);
        AnchorPane.setRightAnchor(conteudo, 0.0);
        AnchorPane.setBottomAnchor(conteudo, 0.0);
        AnchorPane.setLeftAnchor(conteudo, 0.0);

        anchorPane.getChildren().setAll(conteudo);
    }

    // --- MÉTODOS PARA CARREGAR OS ECRÃS --- //
    @FXML
    public void handleMenuItemCadastrosPesquisadores() throws IOException {
        carregarTelaNoCentro("/javafx/laboratorio/views/FXMLAnchorPaneCadastrosPesquisadores.fxml");
    }

    @FXML
    public void handleMenuItemProcessosReservas() throws IOException {
        // AnchorPane a = (AnchorPane) FXMLLoader.load(getClass().getResource("/javafx/laboratorio/views/FXMLAnchorPaneProcessosReservas.fxml"));
        // anchorPane.getChildren().setAll(a);
        System.out.println("Ecrã de Reservas será implementado em breve.");
    }

    @FXML
    public void handleMenuItemCadastrosLaboratorios() throws IOException {
        carregarTelaNoCentro("/javafx/laboratorio/views/FXMLAnchorPaneCadastrosLaboratorios.fxml");
    }
    @FXML
    public void handleMenuItemProcessosManutencoes() throws IOException {
        carregarTelaNoCentro("/javafx/laboratorio/views/FXMLAnchorPaneProcessosManutencoes.fxml");
    }
    @FXML
    public void handleMenuItemGraficosConfiabilidade() { System.out.println("Não implementado"); }
    @FXML
    public void handleMenuItemRelatoriosOcupacao() { System.out.println("Não implementado"); }
    @FXML
    public void handleMenuItemRelatoriosIncidentes() { System.out.println("Não implementado"); }
}