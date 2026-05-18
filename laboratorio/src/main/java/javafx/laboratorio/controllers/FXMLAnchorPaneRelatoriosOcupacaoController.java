package javafx.laboratorio.controllers;

import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.laboratorio.models.dao.PesquisadorDAO;
import javafx.laboratorio.models.database.Database;
import javafx.laboratorio.models.database.DatabaseFactory;
import javafx.laboratorio.models.domain.Pesquisador;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class FXMLAnchorPaneRelatoriosOcupacaoController implements Initializable {

    @FXML private ComboBox<Pesquisador> comboBoxPesquisador;
    @FXML private Button buttonImprimir;

    private final Database database = DatabaseFactory.getDatabase("postgresql");
    private final Connection connection = database.conectar();
    private final PesquisadorDAO pesquisadorDAO = new PesquisadorDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pesquisadorDAO.setConnection(connection);
        List<Pesquisador> lista = pesquisadorDAO.listar();
        comboBoxPesquisador.setItems(FXCollections.observableArrayList(lista));
    }

    @FXML
    public void handleButtonImprimir() {
        Pesquisador selecionado = comboBoxPesquisador.getSelectionModel().getSelectedItem();
        
        if (selecionado != null) {
            HashMap<String, Object> parametros = new HashMap<>();
            parametros.put("p_matricula", selecionado.getMatricula());

            try {
                java.io.InputStream stream = getClass().getResourceAsStream("/relatorios/RelatorioOcupacao.jasper");
                JasperPrint jasperPrint = JasperFillManager.fillReport(stream, parametros, connection);
                
                // Verifica se o relatório tem páginas (dados)
                if (jasperPrint.getPages().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Relatório sem Dados");
                    alert.setHeaderText("Nenhuma reserva encontrada");
                    alert.setContentText("O pesquisador selecionado não possui reservas registradas.");
                    alert.show();
                    return;
                }
                
                JasperViewer viewer = new JasperViewer(jasperPrint, false);
                viewer.setTitle("Extrato de Ocupação - " + selecionado.getNome());
                viewer.setVisible(true);
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Erro ao gerar relatório: " + ex.getMessage());
                alert.show();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Por favor, selecione um pesquisador.");
            alert.show();
        }
    }
}