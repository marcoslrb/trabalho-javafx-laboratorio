package javafx.laboratorio.controllers;

import java.net.URL;
import java.sql.Connection;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.laboratorio.models.dao.PedidoManutencaoDAO;
import javafx.laboratorio.models.database.Database;
import javafx.laboratorio.models.database.DatabaseFactory;

public class FXMLAnchorPaneGraficosConfiabilidadeController implements Initializable {

    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis categoryAxis;
    @FXML private NumberAxis numberAxis;

    private final Database database = DatabaseFactory.getDatabase("postgresql");
    private final Connection connection = database.conectar();
    private final PedidoManutencaoDAO pedidoManutencaoDAO = new PedidoManutencaoDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pedidoManutencaoDAO.setConnection(connection);
        
        // Configura o eixo Y para usar apenas números inteiros
        numberAxis.setTickUnit(1.0);
        numberAxis.setMinorTickVisible(false);
        
        carregarGrafico();
    }

    private void carregarGrafico() {
        Map<String, Integer> dados = pedidoManutencaoDAO.obterDadosGraficoConfiabilidade();
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Falhas Pendentes");
        
        for (Map.Entry<String, Integer> entry : dados.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        barChart.getData().add(series);
    }
}
