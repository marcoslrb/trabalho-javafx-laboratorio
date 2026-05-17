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
import javafx.laboratorio.models.dao.ReservaDAO;
import javafx.laboratorio.models.database.Database;
import javafx.laboratorio.models.database.DatabaseFactory;

public class FXMLAnchorPaneGraficosReservasController implements Initializable {
    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis categoryAxis;
    @FXML private NumberAxis numberAxis;

    private final Database database = DatabaseFactory.getDatabase("postgresql");
    private final Connection connection = database.conectar();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reservaDAO.setConnection(connection);
        numberAxis.setTickUnit(1.0);
        numberAxis.setMinorTickVisible(false);
        carregarGrafico();
    }

    private void carregarGrafico() {
        Map<String, Integer> dados = reservaDAO.obterDadosGraficoReservas();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reservas Efetuadas");
        for (Map.Entry<String, Integer> entry : dados.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        barChart.getData().add(series);
    }
}