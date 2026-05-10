package javafx.laboratorio.controllers;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.laboratorio.models.dao.PesquisadorDAO;
import javafx.laboratorio.models.database.Database;
import javafx.laboratorio.models.database.DatabaseFactory;
import javafx.laboratorio.models.domain.Pesquisador;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class FXMLAnchorPaneCadastrosPesquisadoresController implements Initializable {

    @FXML
    private TableView<Pesquisador> tableViewPesquisadores;
    @FXML
    private TableColumn<Pesquisador, String> tableColumnPesquisadorMatricula;
    @FXML
    private TableColumn<Pesquisador, String> tableColumnPesquisadorNome;
    @FXML
    private Label labelPesquisadorMatricula;
    @FXML
    private Label labelPesquisadorNome;
    @FXML
    private Label labelPesquisadorEmail;
    @FXML
    private Label labelPesquisadorCPF;
    @FXML
    private Label labelPesquisadorTelefone;
    @FXML
    private Label labelPesquisadorStatus;

    private List<Pesquisador> listPesquisadores;
    private ObservableList<Pesquisador> observableListPesquisadores;

    // Atributos para manipulação de Banco de Dados
    private final Database database = DatabaseFactory.getDatabase("postgresql");
    private final Connection connection = database.conectar();
    private final PesquisadorDAO pesquisadorDAO = new PesquisadorDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pesquisadorDAO.setConnection(connection);
        carregarTableViewPesquisador();

        selecionarItemTableViewPesquisador(null);

        tableViewPesquisadores.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selecionarItemTableViewPesquisador(newValue));
    }

    public void carregarTableViewPesquisador() {
        tableColumnPesquisadorMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));

        tableColumnPesquisadorNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        listPesquisadores = pesquisadorDAO.listar();

        observableListPesquisadores = FXCollections.observableArrayList(listPesquisadores);
        tableViewPesquisadores.setItems(observableListPesquisadores);
    }

    public void selecionarItemTableViewPesquisador(Pesquisador pesquisador) {
        if (pesquisador != null) {
            labelPesquisadorMatricula.setText(pesquisador.getMatricula());
            labelPesquisadorNome.setText(pesquisador.getNome());
            labelPesquisadorEmail.setText(pesquisador.getEmail());
            labelPesquisadorCPF.setText(formatarCPF(pesquisador.getCpf()));
            labelPesquisadorTelefone.setText(formatarTelefone(pesquisador.getTelefone()));
            labelPesquisadorStatus.setText(pesquisador.isSuspenso() ? "✘ Suspenso" : "✔ Ativo");
        } else {
            labelPesquisadorMatricula.setText("");
            labelPesquisadorNome.setText("");
            labelPesquisadorEmail.setText("");
            labelPesquisadorCPF.setText("");
            labelPesquisadorTelefone.setText("");
            labelPesquisadorStatus.setText("");
        }
    }

    private String formatarCPF(String cpf) {
        String cpfNumerico = manterApenasDigitos(cpf);
        if (cpfNumerico.length() == 11) {
            return cpfNumerico.substring(0, 3) + "."
                    + cpfNumerico.substring(3, 6) + "."
                    + cpfNumerico.substring(6, 9) + "-"
                    + cpfNumerico.substring(9, 11);
        }
        return cpf == null ? "" : cpf;
    }

    private String formatarTelefone(String telefone) {
        String telefoneNumerico = manterApenasDigitos(telefone);
        if (telefoneNumerico.length() == 11) {
            return "(" + telefoneNumerico.substring(0, 2) + ") "
                    + telefoneNumerico.substring(2, 7) + "-"
                    + telefoneNumerico.substring(7, 11);
        }
        if (telefoneNumerico.length() == 10) {
            return "(" + telefoneNumerico.substring(0, 2) + ") "
                    + telefoneNumerico.substring(2, 6) + "-"
                    + telefoneNumerico.substring(6, 10);
        }
        return (telefone == null || telefone.isBlank()) ? "(não informado)" : telefone;
    }

    private String manterApenasDigitos(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replaceAll("\\D", "");
    }
    
    public boolean showFXMLAnchorPaneCadastrosPesquisadoresDialog(Pesquisador pesquisador) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLAnchorPaneCadastrosPesquisadoresController.class.getResource("/javafx/laboratorio/views/FXMLAnchorPaneCadastrosPesquisadoresDialog.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Registo de Pesquisador");
        dialogStage.initOwner(tableViewPesquisadores.getScene().getWindow());
        dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        FXMLAnchorPaneCadastrosPesquisadoresDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setPesquisador(pesquisador);

        dialogStage.showAndWait();

        return controller.isButtonConfirmarClicked();
    }

    @FXML
    public void handleButtonInserir() throws IOException {
        Pesquisador pesquisador = new Pesquisador();
        boolean buttonConfirmarClicked = showFXMLAnchorPaneCadastrosPesquisadoresDialog(pesquisador);
        if (buttonConfirmarClicked) {
            if (pesquisadorDAO.inserir(pesquisador)) {
                carregarTableViewPesquisador();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro na Inserção");
                alert.setHeaderText("Não foi possível cadastrar o pesquisador.");
                alert.setContentText("Verifique se a Matrícula, CPF ou E-mail já estão registados no sistema.");
                alert.show();
            }
        }
    }
    
    @FXML
    public void handleButtonAlterar() throws IOException {
        Pesquisador pesquisador = tableViewPesquisadores.getSelectionModel().getSelectedItem();
        
        if (pesquisador != null) {
            // Abre o Dialog com os dados do pesquisador selecionado
            boolean buttonConfirmarClicked = showFXMLAnchorPaneCadastrosPesquisadoresDialog(pesquisador);
            
            if (buttonConfirmarClicked) {
                if (pesquisadorDAO.alterar(pesquisador)) {
                    carregarTableViewPesquisador();
                    selecionarItemTableViewPesquisador(pesquisador);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Erro ao salvar as alterações no banco de dados.");
                    alert.show();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Seleção Necessária");
            alert.setHeaderText("Nenhum pesquisador selecionado");
            alert.setContentText("Por favor, escolha um pesquisador na tabela para poder alterar.");
            alert.show();
        }
    }
    @FXML
    public void handleButtonRemover() throws IOException {
        Pesquisador pesquisador = tableViewPesquisadores.getSelectionModel().getSelectedItem();
        if (pesquisador != null) {
            if (pesquisadorDAO.remover(pesquisador)) {
                carregarTableViewPesquisador();
                selecionarItemTableViewPesquisador(null);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Não foi possível remover o pesquisador. Verifique se ele possui reservas vinculadas.");
                alert.show();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Por favor, escolha um pesquisador na tabela!");
            alert.show();
        }
    }
      
}