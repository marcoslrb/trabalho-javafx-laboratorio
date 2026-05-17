package javafx.laboratorio.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.laboratorio.models.dao.ReservaDAO;
import javafx.laboratorio.models.database.Database;
import javafx.laboratorio.models.database.DatabaseFactory;
import javafx.laboratorio.models.domain.Reserva;

public class FXMLAnchorPaneProcessosReservasController implements Initializable {

    @FXML private TableView<Reserva> tableViewReservas;
    @FXML private TableColumn<Reserva, String> tableColumnReservaPesquisador;
    @FXML private TableColumn<Reserva, String> tableColumnReservaLaboratorio;
    @FXML private Label labelReservaId;
    @FXML private Label labelReservaPesquisador;
    @FXML private Label labelReservaLaboratorio;
    @FXML private Label labelReservaInicio;
    @FXML private Label labelReservaFim;

    private List<Reserva> listReservas;
    private ObservableList<Reserva> observableListReservas;
    private final Database database = DatabaseFactory.getDatabase("postgresql");
    private final Connection connection = database.conectar();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        reservaDAO.setConnection(connection);
        carregarTableViewReserva();
        selecionarItemTableViewReserva(null);

        tableViewReservas.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selecionarItemTableViewReserva(newValue));
    }

    public void carregarTableViewReserva() {
        // Vincula as colunas da tabela aos atributos dos objetos (Pesquisador e Laboratorio)
        tableColumnReservaPesquisador.setCellValueFactory(new PropertyValueFactory<>("pesquisador"));
        tableColumnReservaLaboratorio.setCellValueFactory(new PropertyValueFactory<>("laboratorio"));

        // =====================================================================
        // INTERAÇÃO 1 AO BANCO DE DADOS: LISTAR (SELECT)
        // Busca todas as reservas para popular a tela principal do processo.
        // =====================================================================
        listReservas = reservaDAO.listar();

        observableListReservas = FXCollections.observableArrayList(listReservas);
        tableViewReservas.setItems(observableListReservas);
    }

    public void selecionarItemTableViewReserva(Reserva reserva) {
        if (reserva != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            labelReservaId.setText(String.valueOf(reserva.getIdReserva()));
            labelReservaPesquisador.setText(reserva.getPesquisador().getNome());
            labelReservaLaboratorio.setText(reserva.getLaboratorio().getNome());
            labelReservaInicio.setText(reserva.getDataHoraInicio().format(formatter));
            labelReservaFim.setText(reserva.getDataHoraFim().format(formatter));
        } else {
            labelReservaId.setText("");
            labelReservaPesquisador.setText("");
            labelReservaLaboratorio.setText("");
            labelReservaInicio.setText("");
            labelReservaFim.setText("");
        }
    }

    @FXML
    public void handleButtonInserir() throws IOException {
        Reserva reserva = new Reserva();
        boolean buttonConfirmarClicked = showFXMLAnchorPaneProcessosReservasDialog(reserva);

        if (buttonConfirmarClicked) {
            if (reservaDAO.inserir(reserva)) {
                carregarTableViewReserva(); // Recarrega a tabela para mostrar a nova reserva
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro na Inserção");
                alert.setHeaderText("Não foi possível cadastrar a reserva.");
                alert.setContentText("Ocorreu um erro ao salvar no banco de dados.");
                alert.show();
            }
        }
    }

    @FXML
    public void handleButtonRemover() {
        Reserva reserva = tableViewReservas.getSelectionModel().getSelectedItem();
        if (reserva != null) {
            // =====================================================================
            // INTERAÇÃO EXTRA AO BANCO DE DADOS: REMOVER (DELETE)
            // =====================================================================
            if (reservaDAO.remover(reserva)) {
                carregarTableViewReserva();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Erro ao remover reserva.");
                alert.show();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Selecione uma reserva na tabela.");
            alert.show();
        }
    }

    // Método para abrir a janela de diálogo (mesmo padrão dos outros controllers)
    public boolean showFXMLAnchorPaneProcessosReservasDialog(Reserva reserva) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        // Cuidado aqui: O nome do ficheiro FXML tem de ser exatamente igual ao que você criou na pasta views!
        loader.setLocation(FXMLAnchorPaneProcessosReservasController.class.getResource("/javafx/laboratorio/views/FXMLAnchorPaneProcessosReservasDialog.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Registo de Reserva");
        dialogStage.initOwner(tableViewReservas.getScene().getWindow());
        dialogStage.initStyle(javafx.stage.StageStyle.UTILITY);
        dialogStage.setResizable(false);
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        FXMLAnchorPaneProcessosReservasDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setReserva(reserva);

        dialogStage.showAndWait();

        return controller.isButtonConfirmarClicked();
    }
    
    @FXML
    public void handleButtonAlterar() throws IOException {
        // Primeiro, pegamos a reserva que o usuário clicou na tabela
        Reserva reserva = tableViewReservas.getSelectionModel().getSelectedItem();
        
        if (reserva != null) {
            // Abrimos o mesmo Dialog do "Inserir", mas passando a reserva selecionada.
            // Como ela já tem ID e dados, o Dialog vai abrir preenchido.
            boolean buttonConfirmarClicked = showFXMLAnchorPaneProcessosReservasDialog(reserva);
            
            if (buttonConfirmarClicked) {
                // Se o usuário editou e clicou em "Confirmar", chamamos o UPDATE no banco
                // Lembra que o nosso DAO agora ignora o ID atual nas regras de negócio!
                if (reservaDAO.alterar(reserva)) {
                    carregarTableViewReserva(); // Recarrega a lista atualizada
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Erro ao alterar");
                    alert.setContentText("Não foi possível salvar as alterações no banco de dados.");
                    alert.show();
                }
            }
        } else {
            // Se o usuário clicou no botão sem selecionar nada na tabela
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Seleção necessária");
            alert.setContentText("Por favor, selecione uma reserva na tabela para poder alterar.");
            alert.show();
        }
    }
}