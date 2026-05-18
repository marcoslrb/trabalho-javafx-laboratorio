package javafx.laboratorio.controllers;

import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.laboratorio.models.database.Database;
import javafx.laboratorio.models.database.DatabaseFactory;
import javafx.laboratorio.models.domain.Laboratorio;
import javafx.laboratorio.services.LaboratorioService;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

/**
 * Controller para a tela do Relatório 2: Log de Incidentes e Impacto em Reservas.
 * Integra-se diretamente com o Jaspersoft (JasperReports) para gerar o relatório impresso/PDF
 * permitindo filtragem por laboratório selecionado.
 */
public class FXMLAnchorPaneRelatoriosIncidentesController implements Initializable {

    @FXML
    private ComboBox<Laboratorio> comboBoxLaboratorio;
    @FXML
    private Button buttonVisualizar;

    private final Database database = DatabaseFactory.getDatabase("postgresql");
    private final Connection connection = database.conectar();
    private final LaboratorioService laboratorioService = new LaboratorioService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        carregarComboBoxLaboratorios();
    }

    /**
     * Carrega a lista de laboratórios com uma opção padrão para "Todos os Laboratórios".
     */
    private void carregarComboBoxLaboratorios() {
        List<Laboratorio> laboratorios = laboratorioService.listarTodos();

        Laboratorio todos = new Laboratorio();
        todos.setId(-1);
        todos.setNome("Todos os Laboratórios");
        todos.setFuncional(true);

        ObservableList<Laboratorio> obsLabs = FXCollections.observableArrayList();
        obsLabs.add(todos);
        obsLabs.addAll(laboratorios);

        comboBoxLaboratorio.setItems(obsLabs);
        comboBoxLaboratorio.getSelectionModel().select(todos);
    }

    /**
     * Gera e exibe o relatório no JasperViewer aplicando o filtro do laboratório selecionado.
     */
    @FXML
    public void handleButtonVisualizar() {
        try {
            java.io.InputStream stream = getClass().getResourceAsStream("/relatorios/RelatorioIncidentes.jasper");
            if (stream == null) {
                throw new RuntimeException("Arquivo de relatório compilado '/relatorios/RelatorioIncidentes.jasper' não encontrado.");
            }

            Laboratorio selecionado = comboBoxLaboratorio.getSelectionModel().getSelectedItem();
            int idLab = selecionado != null ? selecionado.getId() : -1;

            // Envia o parâmetro de filtro do laboratório para o Jaspersoft
            HashMap<String, Object> parametros = new HashMap<>();
            parametros.put("p_id_laboratorio", idLab);

            JasperPrint jasperPrint = JasperFillManager.fillReport(stream, parametros, connection);

            // Verifica se o relatório tem páginas (dados)
            if (jasperPrint.getPages().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Relatório sem Dados");
                alert.setHeaderText("Nenhum incidente encontrado");
                alert.setContentText("Não existem incidentes registrados para o laboratório selecionado.");
                alert.show();
                return;
            }

            // Abre o visualizador nativo do Jaspersoft
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Relatório - Log de Incidentes e Impacto em Reservas");
            viewer.setVisible(true);

        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro ao Gerar Relatório");
            alert.setHeaderText("Ocorreu uma falha ao renderizar o relatório do Jaspersoft.");
            alert.setContentText(ex.getMessage());
            alert.show();
        }
    }
}
