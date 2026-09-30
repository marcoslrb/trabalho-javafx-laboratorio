package javafx.laboratorio.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import javafx.laboratorio.grpc.ConsultarPorIdRequest;
import javafx.laboratorio.grpc.ConsultarPorIdResponse;
import javafx.laboratorio.grpc.LaboratorioServiceGrpc;
import javafx.laboratorio.models.domain.Laboratorio;
import javafx.laboratorio.services.LaboratorioService;

/**
 * Controller da tela de Cadastro de Laboratórios.
 *
 * Segue exatamente o mesmo padrão do FXMLAnchorPaneCadastrosPesquisadoresController.
 * Responsabilidades:
 *   - Carregar a TableView com dados do banco via Service
 *   - Exibir detalhes do item selecionado nos Labels
 *   - Abrir o Dialog de inserção/edição
 *   - Chamar o Service para inserir, alterar e remover
 */
public class FXMLAnchorPaneCadastrosLaboratoriosController implements Initializable {

    // --- Componentes da TableView ---
    @FXML
    private TableView<Laboratorio> tableViewLaboratorios;
    @FXML
    private TableColumn<Laboratorio, Integer> tableColumnLaboratorioId;
    @FXML
    private TableColumn<Laboratorio, String> tableColumnLaboratorioNome;
    @FXML
    private TableColumn<Laboratorio, String> tableColumnLaboratorioArea;

    // --- Labels de detalhe (lado direito) ---
    @FXML
    private Label labelLaboratorioId;
    @FXML
    private Label labelLaboratorioNome;
    @FXML
    private Label labelLaboratorioArea;
    @FXML
    private Label labelLaboratorioDescricao;
    @FXML
    private Label labelLaboratorioFuncional;

    // --- Consulta via gRPC (componentes novos da tela) ---
    @FXML
    private TextField tfId;
    @FXML
    private Label lblNome;
    @FXML
    private Label lblDepartamento;
    @FXML
    private Label lblCapacidade;
    @FXML
    private Button btnBuscar;

    // --- Endereço do servidor gRPC (ajuste se o servidor estiver em outra máquina) ---
    private static final String GRPC_HOST = "localhost";
    private static final int GRPC_PORT = 50051;

    // --- Listas para a TableView ---
    private List<Laboratorio> listLaboratorios;
    private ObservableList<Laboratorio> observableListLaboratorios;

    // --- Service (camada de negócio) ---
    private final LaboratorioService laboratorioService = new LaboratorioService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configura as colunas e carrega a tabela
        carregarTableViewLaboratorio();

        // Limpa os detalhes ao abrir
        selecionarItemTableViewLaboratorio(null);

        // Listener: quando selecionar um item na tabela, exibe os detalhes
        tableViewLaboratorios.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selecionarItemTableViewLaboratorio(newValue));
    }

    public void carregarTableViewLaboratorio() {
        // Configura quais propriedades do objeto Laboratorio mapeiam para cada coluna
        tableColumnLaboratorioId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnLaboratorioNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tableColumnLaboratorioArea.setCellValueFactory(new PropertyValueFactory<>("area"));

        // Busca os dados via Service
        listLaboratorios = laboratorioService.listarTodos();

        // Converte para ObservableList (necessário para o JavaFX reagir a mudanças)
        observableListLaboratorios = FXCollections.observableArrayList(listLaboratorios);
        tableViewLaboratorios.setItems(observableListLaboratorios);
    }

    // Exibir detalhes do laboratório selecionado
    public void selecionarItemTableViewLaboratorio(Laboratorio laboratorio) {
        if (laboratorio != null) {
            labelLaboratorioId.setText(String.valueOf(laboratorio.getId()));
            labelLaboratorioNome.setText(laboratorio.getNome());
            labelLaboratorioArea.setText(laboratorio.getArea());
            labelLaboratorioDescricao.setText(
                    laboratorio.getDescricao() != null ? laboratorio.getDescricao() : "(sem descrição)");
            labelLaboratorioFuncional.setText(laboratorio.isFuncional() ? "✔ Funcional" : "✘ Em Manutenção");
        } else {
            labelLaboratorioId.setText("");
            labelLaboratorioNome.setText("");
            labelLaboratorioArea.setText("");
            labelLaboratorioDescricao.setText("");
            labelLaboratorioFuncional.setText("");
        }
    }

    // Abre o Dialog de cadastro/edição
    public boolean showFXMLAnchorPaneCadastrosLaboratoriosDialog(Laboratorio laboratorio) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLAnchorPaneCadastrosLaboratoriosController.class.getResource(
                "/javafx/laboratorio/views/FXMLAnchorPaneCadastrosLaboratoriosDialog.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Cadastro de Laboratório");
        dialogStage.initOwner(tableViewLaboratorios.getScene().getWindow());
        dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        dialogStage.setScene(new Scene(page));

        FXMLAnchorPaneCadastrosLaboratoriosDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setLaboratorio(laboratorio);

        dialogStage.showAndWait();

        return controller.isButtonConfirmarClicked();
    }

    // BOTÃO INSERIR
    @FXML
    public void handleButtonInserir() throws IOException {
        Laboratorio laboratorio = new Laboratorio();
        boolean confirmar = showFXMLAnchorPaneCadastrosLaboratoriosDialog(laboratorio);
        if (confirmar) {
            String resultado = laboratorioService.inserir(laboratorio);
            if ("SUCESSO".equals(resultado)) {
                carregarTableViewLaboratorio();
            } else {
                exibirAlertaErro("Erro na Inserção", resultado);
            }
        }
    }

    // BOTÃO ALTERAR
    @FXML
    public void handleButtonAlterar() throws IOException {
        Laboratorio laboratorio = tableViewLaboratorios.getSelectionModel().getSelectedItem();
        if (laboratorio != null) {
            boolean confirmar = showFXMLAnchorPaneCadastrosLaboratoriosDialog(laboratorio);
            if (confirmar) {
                String resultado = laboratorioService.alterar(laboratorio);
                if ("SUCESSO".equals(resultado)) {
                    carregarTableViewLaboratorio();
                    selecionarItemTableViewLaboratorio(laboratorio);
                } else {
                    exibirAlertaErro("Erro na Alteração", resultado);
                }
            }
        } else {
            exibirAlertaErro("Seleção Necessária",
                    "Por favor, selecione um laboratório na tabela para alterar.");
        }
    }

    // BOTÃO REMOVER
    @FXML
    public void handleButtonRemover() {
        Laboratorio laboratorio = tableViewLaboratorios.getSelectionModel().getSelectedItem();
        if (laboratorio != null) {
            boolean removido = laboratorioService.remover(laboratorio);
            if (removido) {
                carregarTableViewLaboratorio();
                selecionarItemTableViewLaboratorio(null);
            } else {
                exibirAlertaErro("Erro na Remoção",
                        "Não foi possível remover o laboratório. "
                        + "Verifique se existem reservas ou pedidos de manutenção vinculados.");
            }
        } else {
            exibirAlertaErro("Seleção Necessária",
                    "Por favor, selecione um laboratório na tabela para remover.");
        }
    }

    // BOTÃO BUSCAR gRPC
    @FXML
    public void buscarLaboratorioGrpc() {
        // 1) Lê e valida o ID digitado no TextField
        String texto = tfId.getText();
        if (texto == null || texto.isBlank()) {
            exibirAlertaErro("Consulta gRPC", "Informe o ID do laboratório.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            exibirAlertaErro("Consulta gRPC", "ID inválido: digite um número inteiro.");
            return;
        }

        // 2) Desabilita o botão para evitar cliques duplicados durante a chamada
        btnBuscar.setDisable(true);

        // 3) Executa a chamada gRPC bloqueante em uma thread separada:
        //    a FX Application Thread não pode ser bloqueada, senão a UI trava
        Thread worker = new Thread(() -> {
            // Obs.: ManagedChannel NAO implementa AutoCloseable,
            // entao usamos try/finally com shutdownNow() explicito
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(GRPC_HOST, GRPC_PORT)
                    .usePlaintext() // sem TLS (ambiente local)
                    .build();

            try {
                // Stub bloqueante: consultarPorId() só retorna quando a resposta chega
                LaboratorioServiceGrpc.LaboratorioServiceBlockingStub stub =
                        LaboratorioServiceGrpc.newBlockingStub(channel);

                ConsultarPorIdRequest request =
                        ConsultarPorIdRequest.newBuilder()
                                .setId(id)
                                .build();

                // Chamada síncrona com deadline, para nunca ficar pendurada
                ConsultarPorIdResponse response = stub
                        .withDeadlineAfter(5, TimeUnit.SECONDS)
                        .consultarPorId(request);

                final String nome = response.getNome();
                final String departamento = response.getDepartamento();
                final int capacidade = response.getCapacidade();

                // 4) Atualiza os Labels de volta na thread da UI (obrigatório no JavaFX)
                Platform.runLater(() -> {
                    lblNome.setText(nome);
                    lblDepartamento.setText(departamento);
                    lblCapacidade.setText(String.valueOf(capacidade));
                });

            } catch (StatusRuntimeException e) {
                // Erro de status retornado pelo servidor gRPC
                Status status = e.getStatus();
                if (status.getCode() == Status.Code.NOT_FOUND) {
                    // ID não encontrado (caso tratado no servidor)
                    Platform.runLater(() -> {
                        lblNome.setText("Não encontrado");
                        lblDepartamento.setText("-");
                        lblCapacidade.setText("-");
                    });
                } else {
                    // INTERNAL (falha no banco) ou qualquer outro código
                    Platform.runLater(() -> exibirAlertaErro(
                            "Erro gRPC", status.getDescription()));
                }
            } catch (Exception e) {
                // Falha de rede, timeout, servidor indisponível, etc.
                Platform.runLater(() -> exibirAlertaErro(
                        "Falha gRPC", "Não foi possível comunicar com o servidor gRPC: "
                        + e.getMessage()));
            } finally {
                // Fecha o canal gRPC (descarta conexoes e RPCs em andamento)
                channel.shutdownNow();

                // Sempre reabilita o botão, de volta na thread da UI
                Platform.runLater(() -> btnBuscar.setDisable(false));
            }
        }, "grpc-lab-search");

        worker.setDaemon(true); // não impede o JVM de encerrar quando a aplicação fechar
        worker.start();
    }

    // Método auxiliar para exibir alertas de erro
    private void exibirAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.show();
    }
}
