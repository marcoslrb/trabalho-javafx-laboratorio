package javafx.laboratorio.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class FXMLVBoxMainController implements Initializable {

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
    
    private void alterarTituloJanela(String subtitulo) {
        if (anchorPane != null && anchorPane.getScene() != null) {
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            if (stage != null) {
                stage.setTitle("Sistema de Gerenciamento de Laboratórios - " + subtitulo);
            }
        }
    }

    @FXML
    public void handleMenuItemCadastrosPesquisadores() throws IOException {
        carregarTelaNoCentro("/javafx/laboratorio/views/FXMLAnchorPaneCadastrosPesquisadores.fxml");
        alterarTituloJanela("Cadastro de Pesquisadores");
    }

    @FXML
    public void handleMenuItemProcessosReservas() throws IOException {
        // Chamamos o método utilitário que o grupo criou para injetar o FXML no centro da tela.
        // Isso garante que a tabela de reservas ocupe todo o espaço disponível.
        carregarTelaNoCentro("/javafx/laboratorio/views/FXMLAnchorPaneProcessosReservas.fxml");
        
        // Atualiza o título da janela principal para o utilizador saber exatamente onde está.
        alterarTituloJanela("Processos - Reservas");
    }

    @FXML
    public void handleMenuItemCadastrosLaboratorios() throws IOException {
        carregarTelaNoCentro("/javafx/laboratorio/views/FXMLAnchorPaneCadastrosLaboratorios.fxml");
        alterarTituloJanela("Cadastro de Laboratórios");
    }
    
    @FXML
    public void handleMenuItemProcessosManutencoes() throws IOException {
        carregarTelaNoCentro("/javafx/laboratorio/views/FXMLAnchorPaneProcessosManutencoes.fxml");
        alterarTituloJanela("Pedido de Manutenção");
    }
    @FXML
    public void handleMenuItemGraficosConfiabilidade() throws IOException {
        carregarTelaNoCentro("/javafx/laboratorio/views/FXMLAnchorPaneGraficosConfiabilidade.fxml");
        alterarTituloJanela("Gráfico de Confiabilidade");
    }
    @FXML
    public void handleMenuItemGraficosReservas() throws IOException {
        carregarTelaNoCentro("/javafx/laboratorio/views/FXMLAnchorPaneGraficosReservas.fxml");
        alterarTituloJanela("Gráfico de Reservas");
    }
    @FXML
    public void handleMenuItemRelatoriosIncidentes() throws IOException {
        carregarTelaNoCentro("/javafx/laboratorio/views/FXMLAnchorPaneRelatoriosIncidentes.fxml");
        alterarTituloJanela("Relatório - Log de Incidentes");
    }
    @FXML
    public void handleMenuItemRelatoriosOcupacao() throws IOException {
        carregarTelaNoCentro("/javafx/laboratorio/views/FXMLAnchorPaneRelatoriosOcupacao.fxml");
        alterarTituloJanela("Relatório - Extrato de Ocupação");
    }
}