package javafx.laboratorio.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.laboratorio.services.MuralSocketService;
import sockets.thread.AvisoGrupo;
import sockets.thread.ContadorGrupo;
import sockets.thread.RespostaMural;

public class FXMLAnchorPaneMuralAvisosController implements Initializable {

    // --- Controles de entrada ---
    @FXML
    private Spinner<Integer> spinnerGrupo;
    @FXML
    private Button btnConectar;

    // --- Identificação do grupo ---
    @FXML
    private Label labelGrupoId;
    @FXML
    private Label labelGrupoNome;

    // --- Tabela de ranking ---
    @FXML
    private TableView<ContadorGrupo> tabelaRanking;
    @FXML
    private TableColumn<ContadorGrupo, Integer> colPosicao;
    @FXML
    private TableColumn<ContadorGrupo, String> colNomeGrupo;
    @FXML
    private TableColumn<ContadorGrupo, Integer> colAcessos;

    // --- Destaque da mensagem em rotação ---
    @FXML
    private VBox painelDestaque;
    @FXML
    private Label labelDestaqueTitulo;
    @FXML
    private Label labelDestaqueMensagem;
    @FXML
    private Label labelDestaqueTimestamp;

    // --- Lista de todos os avisos do grupo ---
    @FXML
    private ListView<AvisoGrupo> listaAvisos;

    // --- Status e atualização ---
    @FXML
    private Label labelStatus;
    @FXML
    private Label labelUltimaAtualizacao;

    // --- Estado interno ---
    private final MuralSocketService socketService = new MuralSocketService();
    private Thread threadRotacao;
    private volatile boolean rotacaoAtiva = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Configura Spinner de 1 a 10
        spinnerGrupo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3));
        spinnerGrupo.setEditable(true);

        // Configura colunas da tabela
        configurarColunaRanking();

        // Configura renderização da lista de avisos
        listaAvisos.setCellFactory(lv -> new ListCell<AvisoGrupo>() {
            @Override
            protected void updateItem(AvisoGrupo aviso, boolean empty) {
                super.updateItem(aviso, empty);
                if (empty || aviso == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("[" + aviso.getTimestamp() + "]  " + aviso.getTitulo() + "  —  " + aviso.getMensagem());
                }
            }
        });

        setStatus("Aguardando conexão...", false);
    }

    private void configurarColunaRanking() {
        // Coluna "Posição" é calculada pelo índice na lista (não é um campo do modelo)
        colPosicao.setCellFactory(col -> new TableCell<ContadorGrupo, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1) + "º");
            }
        });

        colNomeGrupo.setCellValueFactory(new PropertyValueFactory<>("nomeGrupo"));
        colNomeGrupo.setCellFactory(col -> new TableCell<ContadorGrupo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    ContadorGrupo cg = getTableRow().getItem();
                    setText(cg.getIdGrupo() + " - " + cg.getNomeGrupo());
                }
            }
        });
        colAcessos.setCellValueFactory(new PropertyValueFactory<>("quantidadeUtilizacoes"));

        tabelaRanking.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    @FXML
    public void handleConectar() {
        int idGrupo = spinnerGrupo.getValue();

        // Para qualquer rotação anterior
        pararRotacao();

        setStatus("Conectando ao servidor... (Grupo " + idGrupo + ")", false);
        btnConectar.setDisable(true);

        // Task roda em thread de background para não bloquear a UI
        Task<RespostaMural> task = new Task<RespostaMural>() {
            @Override
            protected RespostaMural call() throws Exception {
                return socketService.consultarMural(idGrupo);
            }
        };

        task.setOnSucceeded(e -> {
            RespostaMural resposta = task.getValue();
            preencherTela(resposta);
            btnConectar.setDisable(false);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            String msg = ex != null ? ex.getMessage() : "Erro desconhecido";
            setStatus("Erro de conexão: " + msg, true);
            btnConectar.setDisable(false);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Conexão");
            alert.setHeaderText("Não foi possível conectar ao servidor.");
            alert.setContentText("Verifique se o servidor está rodando na porta 12345.\n\nDetalhes: " + msg);
            alert.showAndWait();
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void preencherTela(RespostaMural resposta) {
        // Identificação do grupo
        labelGrupoId.setText("Grupo " + resposta.getIdGrupo());
        labelGrupoNome.setText(resposta.getNomeGrupo());

        // Ranking
        List<ContadorGrupo> ranking = resposta.getRanking();
        ObservableList<ContadorGrupo> rankingObs = FXCollections.observableArrayList(ranking);
        tabelaRanking.setItems(rankingObs);

        // Lista de avisos
        List<AvisoGrupo> avisos = resposta.getAvisosGrupo();
        listaAvisos.setItems(FXCollections.observableArrayList(avisos));

        // Status e timestamp
        setStatus("Conectado com sucesso!", false);
        labelUltimaAtualizacao.setText("Última atualização: " + resposta.getUltimaAtualizacao());

        // Inicia rotação das mensagens em destaque
        if (avisos != null && !avisos.isEmpty()) {
            iniciarRotacao(avisos);
        } else {
            labelDestaqueTitulo.setText("Sem mensagens");
            labelDestaqueMensagem.setText("Nenhum aviso disponível para este grupo.");
            labelDestaqueTimestamp.setText("");
        }
    }

    private void iniciarRotacao(List<AvisoGrupo> avisos) {
        rotacaoAtiva = true;

        threadRotacao = new Thread(() -> {
            int indice = 0;
            while (rotacaoAtiva) {
                final AvisoGrupo aviso = avisos.get(indice % avisos.size());

                Platform.runLater(() -> exibirDestaqueAviso(aviso));

                indice++;
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        threadRotacao.setDaemon(true); // Encerra junto com a JVM
        threadRotacao.setName("Thread-RotacaoMural");
        threadRotacao.start();
    }

    private void exibirDestaqueAviso(AvisoGrupo aviso) {
        labelDestaqueTitulo.setText(aviso.getTitulo());
        labelDestaqueMensagem.setText(aviso.getMensagem());
        labelDestaqueTimestamp.setText(aviso.getTimestamp());
    }

    private void pararRotacao() {
        rotacaoAtiva = false;
        if (threadRotacao != null && threadRotacao.isAlive()) {
            threadRotacao.interrupt();
        }
    }

    private void setStatus(String mensagem, boolean erro) {
        labelStatus.setText(mensagem);
        if (erro) {
            labelStatus.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            labelStatus.setStyle("-fx-text-fill: #1fb75eff;");
        }
    }
}
