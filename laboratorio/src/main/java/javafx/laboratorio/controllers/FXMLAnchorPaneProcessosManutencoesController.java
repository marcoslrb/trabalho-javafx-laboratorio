package javafx.laboratorio.controllers;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.laboratorio.models.domain.Laboratorio;
import javafx.laboratorio.models.domain.PedidoManutencao;
import javafx.laboratorio.models.domain.Pesquisador;
import javafx.laboratorio.models.domain.Reserva;
import javafx.laboratorio.services.LaboratorioService;
import javafx.laboratorio.services.PedidoManutencaoService;

/**
 * Controller da tela de Pedido de Manutenção.
 *
 * FLUXO DA TELA (passo a passo):
 *   1. Usuário digita a matrícula e clica em "Validar Pesquisador"
 *      -> O sistema consulta o banco e exibe o nome do pesquisador se válido
 *   2. Usuário seleciona o laboratório no ComboBox e clica em "Verificar Reserva"
 *      -> O sistema verifica se existe reserva nos últimos 5 dias
 *      -> Exibe a data da reserva encontrada (ou bloqueia se não houver)
 *   3. Usuário preenche a descrição do problema
 *   4. Usuário clica em "Registrar Pedido"
 *      -> O Service executa todas as 4 regras de negócio
 *      -> Exibe sucesso ou mensagem de erro específica
 *   5. A TableView é recarregada mostrando o novo pedido
 */
public class FXMLAnchorPaneProcessosManutencoesController implements Initializable {

    // Formulário de cadastro (lado esquerdo/superior)
    @FXML
    private TextField textFieldMatricula;
    @FXML
    private Button buttonValidarPesquisador;
    @FXML
    private Label labelNomePesquisador;

    @FXML
    private ComboBox<Laboratorio> comboBoxLaboratorio;
    @FXML
    private Button buttonVerificarReserva;
    @FXML
    private Label labelInfoReserva;

    @FXML
    private TextArea textAreaDescricao;
    @FXML
    private Button buttonRegistrarPedido;

    @FXML
    private TableView<PedidoManutencao> tableViewPedidos;
    @FXML
    private TableColumn<PedidoManutencao, Integer> tableColumnPedidoId;
    @FXML
    private TableColumn<PedidoManutencao, String> tableColumnPedidoPesquisador;
    @FXML
    private TableColumn<PedidoManutencao, String> tableColumnPedidoLaboratorio;
    @FXML
    private TableColumn<PedidoManutencao, String> tableColumnPedidoHora;
    @FXML
    private TableColumn<PedidoManutencao, String> tableColumnPedidoStatus;

    // Botões de gerenciamento dos pedidos na tabela 
    @FXML
    private Button buttonMarcarResolvido;
    @FXML
    private Button buttonAlterarPedido;
    @FXML
    private Button buttonRemoverPedido;   // remove o pedido selecionado

    // Estado interno do controller 
    // Esses atributos guardam o estado atual da validação em andamento
    private Pesquisador pesquisadorValidado = null;   // pesquisador após RN1
    private Reserva reservaEncontrada = null;          // reserva após RN3

    private final PedidoManutencaoService pedidoService = new PedidoManutencaoService();
    private final LaboratorioService laboratorioService = new LaboratorioService();

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Validação em tempo real: Matrícula (Máx 8 caracteres)
        textFieldMatricula.setTextFormatter(new TextFormatter<>(change -> 
            change.getControlNewText().length() <= 8 ? change : null));
        textFieldMatricula.setPromptText("Máx. 8 caracteres");
        textFieldMatricula.setTooltip(new Tooltip("Digite a matrícula do pesquisador (até 8 caracteres)."));

        // Validação em tempo real: Descrição (Máx 500 caracteres)
        textAreaDescricao.setTextFormatter(new TextFormatter<>(change -> 
            change.getControlNewText().length() <= 500 ? change : null));
        textAreaDescricao.setPromptText("Descreva o problema detalhadamente (máx 500 caracteres)");

        // Carrega os laboratórios no ComboBox
        carregarComboBoxLaboratorio();

        // Configura a TableView de pedidos
        carregarTableViewPedidos();

        // Desabilita todos os botões de ação até haver seleção na tabela
        buttonVerificarReserva.setDisable(true);
        buttonRegistrarPedido.setDisable(true);
        buttonMarcarResolvido.setDisable(true);
        buttonAlterarPedido.setDisable(true);
        buttonRemoverPedido.setDisable(true);

        // Habilita "Verificar Reserva" quando pesquisador validado E lab selecionado
        comboBoxLaboratorio.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, novo) -> atualizarEstadoBotoes());

        // Listener na TableView: habilita/desabilita botões de ação conforme seleção
        tableViewPedidos.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, novo) -> {
                    boolean temSelecao = novo != null;
                    buttonAlterarPedido.setDisable(!temSelecao);
                    buttonRemoverPedido.setDisable(!temSelecao);
                    // Desabilita "Marcar Resolvido" se já estiver resolvido
                    buttonMarcarResolvido.setDisable(!temSelecao || novo.isStatusResolvido());
                });
    }

    // PASSO 1: Validar Pesquisador (Regra de Negócio 1)
    /**
     * Chamado ao clicar em "Validar Pesquisador".
     * Consulta o banco via Service e exibe o nome se válido.
     */
    @FXML
    public void handleButtonValidarPesquisador() {
        String matricula = textFieldMatricula.getText().trim();

        if (matricula.isEmpty()) {
            labelNomePesquisador.setText("⚠ Digite a matrícula.");
            pesquisadorValidado = null;
            atualizarEstadoBotoes();
            return;
        }

        // Chama o Service que executa a RN1
        pesquisadorValidado = pedidoService.validarEBuscarPesquisador(matricula);

        if (pesquisadorValidado != null) {
            labelNomePesquisador.setText("✔ " + pesquisadorValidado.getNome());
        } else {
            labelNomePesquisador.setText("✘ Matrícula não encontrada ou pesquisador suspenso.");
            pesquisadorValidado = null;
        }

        // Reseta a reserva (pois mudou o pesquisador)
        reservaEncontrada = null;
        labelInfoReserva.setText("");
        atualizarEstadoBotoes();
    }

    // PASSO 2: Verificar Reserva (Regra de Negócio 3)
    /**
     * Chamado ao clicar em "Verificar Reserva".
     * Verifica se há reserva nos últimos 5 dias para pesquisador + laboratório.
     */
    @FXML
    public void handleButtonVerificarReserva() {
        Laboratorio laboratorioSelecionado = comboBoxLaboratorio.getSelectionModel().getSelectedItem();

        if (pesquisadorValidado == null || laboratorioSelecionado == null) {
            labelInfoReserva.setText("⚠ Valide o pesquisador e selecione o laboratório.");
            return;
        }

        // Chama o Service que executa a RN3 (busca reserva nos últimos 5 dias)
        reservaEncontrada = pedidoService.buscarReservaValida(
                pesquisadorValidado.getMatricula(),
                laboratorioSelecionado.getId());

        if (reservaEncontrada != null) {
            String dataFim = reservaEncontrada.getDataHoraFim() != null
                    ? reservaEncontrada.getDataHoraFim().format(FORMATTER)
                    : "?";
            labelInfoReserva.setText("✔ Reserva #" + reservaEncontrada.getIdReserva()
                    + " encontrada. Uso em: " + dataFim);
        } else {
            labelInfoReserva.setText("✘ Nenhuma reserva nos últimos 5 dias para este laboratório.");
            reservaEncontrada = null;
        }

        atualizarEstadoBotoes();
    }

    // PASSO 3: Registrar Pedido (Regras de Negócio 2, 3 e 4)
    /**
     * Chamado ao clicar em "Registrar Pedido".
     * O Service executa internamente as RNs 1, 2, 3 e 4.
     */
    @FXML
    public void handleButtonRegistrarPedido() {
        Laboratorio laboratorioSelecionado = comboBoxLaboratorio.getSelectionModel().getSelectedItem();
        String descricao = textAreaDescricao.getText().trim();

        if (descricao.isEmpty()) {
            exibirAlertaErro("Campo obrigatório", "A descrição do problema é obrigatória.");
            return;
        }
        if (descricao.length() > 500) {
            exibirAlertaErro("Campo inválido", "A descrição deve ter no máximo 500 caracteres.");
            return;
        }

        // Chama o Service que executa TODAS as regras de negócio (RN1, RN2, RN3, RN4)
        String resultado = pedidoService.registrarPedido(
                pesquisadorValidado.getMatricula(),
                laboratorioSelecionado.getId(),
                descricao);

        if (resultado.startsWith("SUCESSO")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Pedido Registrado");
            alert.setHeaderText(null);
            alert.setContentText(resultado);
            alert.showAndWait();

            // Limpa o formulário e recarrega a tabela
            limparFormulario();
            carregarTableViewPedidos();
            // Recarrega o ComboBox pois o lab mudou de status (funcional -> não funcional)
            carregarComboBoxLaboratorio();

        } else {
            // Exibe a mensagem de erro específica de qual regra falhou
            exibirAlertaErro("Pedido não registrado", resultado);
        }
    }

    // BOTÃO: Marcar Pedido como Resolvido 
    @FXML
    public void handleButtonMarcarResolvido() {
        PedidoManutencao pedidoSelecionado = tableViewPedidos.getSelectionModel().getSelectedItem();
        if (pedidoSelecionado != null) {
            // Validação de estado redundante por segurança
            if (pedidoSelecionado.isStatusResolvido()) {
                exibirAlertaErro("Operação inválida", "Este pedido já está marcado como resolvido.");
                return;
            }

            String msg = pedidoService.marcarComoResolvido(pedidoSelecionado.getId());
            if ("SUCESSO".equals(msg)) {
                carregarTableViewPedidos();
            } else {
                exibirAlertaErro("Erro", msg);
            }
        }
    }

    // BOTÃO: Alterar Pedido (editar descrição e status)
    /**
     * Abre um diálogo simples para editar a descrição e o status_resolvido
     * do pedido selecionado na tabela.
     */
    @FXML
    public void handleButtonAlterarPedido() {
        PedidoManutencao pedidoSelecionado = tableViewPedidos.getSelectionModel().getSelectedItem();
        if (pedidoSelecionado == null) return;

        // Cria um diálogo de edição inline sem precisar de uma nova tela FXML
        javafx.scene.control.Dialog<Boolean> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Editar Pedido #" + pedidoSelecionado.getId());
        dialog.setHeaderText("Edite a descrição e o status do pedido.");

        // Botões do diálogo
        javafx.scene.control.ButtonType btnSalvar =
                new javafx.scene.control.ButtonType("Salvar", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType btnCancelar =
                new javafx.scene.control.ButtonType("Cancelar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, btnCancelar);

        // Conteúdo do diálogo
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(15));

        TextArea taDescricao = new TextArea(pedidoSelecionado.getDescricao());
        taDescricao.setWrapText(true);
        taDescricao.setPrefRowCount(4);
        taDescricao.setPrefWidth(350);
        taDescricao.setPromptText("Descreva o problema detalhadamente (máx 500 caracteres)");

        javafx.scene.control.CheckBox cbResolvido = new javafx.scene.control.CheckBox("Marcado como resolvido");
        cbResolvido.setSelected(pedidoSelecionado.isStatusResolvido());

        grid.add(new Label("Descrição:"), 0, 0);
        grid.add(taDescricao, 1, 0);
        grid.add(new Label("Status:"), 0, 1);
        grid.add(cbResolvido, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Converte o resultado do diálogo
        dialog.setResultConverter(buttonType -> buttonType == btnSalvar);

        java.util.Optional<Boolean> resultado = dialog.showAndWait();
        if (resultado.isPresent() && resultado.get()) {
            String novaDescricao = taDescricao.getText().trim();
            boolean novoStatus = cbResolvido.isSelected();

            // Validações de campo no Controller
            if (novaDescricao.isEmpty()) {
                exibirAlertaErro("Validação", "A descrição não pode ser vazia.");
                return;
            }
            if (novaDescricao.length() > 500) {
                exibirAlertaErro("Validação", "A descrição deve ter no máximo 500 caracteres.");
                return;
            }

            // Evita operação redundante se nada mudou
            if (novaDescricao.equals(pedidoSelecionado.getDescricao()) && 
                novoStatus == pedidoSelecionado.isStatusResolvido()) {
                return;
            }

            pedidoSelecionado.setDescricao(novaDescricao);
            pedidoSelecionado.setStatusResolvido(novoStatus);
 
            String msg = pedidoService.alterar(pedidoSelecionado);
            if ("SUCESSO".equals(msg)) {
                carregarTableViewPedidos();
            } else {
                exibirAlertaErro("Erro ao alterar", msg);
            }
        }
    }

    // BOTÃO: Remover Pedido
    @FXML
    public void handleButtonRemoverPedido() {
        PedidoManutencao pedidoSelecionado = tableViewPedidos.getSelectionModel().getSelectedItem();
        if (pedidoSelecionado == null) return;

        // Pede confirmação antes de remover
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Remoção");
        confirmacao.setHeaderText("Remover Pedido #" + pedidoSelecionado.getId());
        
        String statusStr = pedidoSelecionado.isStatusResolvido() ? "RESOLVIDO" : "PENDENTE";
        confirmacao.setContentText("Tem certeza que deseja remover este pedido de manutenção (" + statusStr + ")? Esta ação não pode ser desfeita.");
 
        java.util.Optional<javafx.scene.control.ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isPresent() && resposta.get() == javafx.scene.control.ButtonType.OK) {
            String msg = pedidoService.remover(pedidoSelecionado.getId());
            if ("SUCESSO".equals(msg)) {
                carregarTableViewPedidos();
            } else {
                exibirAlertaErro("Erro ao remover", msg);
            }
        }
    }

    // Carregar o ComboBox de Laboratórios
    private void carregarComboBoxLaboratorio() {
        List<Laboratorio> laboratorios = laboratorioService.listarTodos();
        ObservableList<Laboratorio> obsLabs = FXCollections.observableArrayList(laboratorios);
        comboBoxLaboratorio.setItems(obsLabs);
    }

    // Carregar a TableView de Pedidos
    private void carregarTableViewPedidos() {
        // Coluna ID: propriedade 'id' do PedidoManutencao
        tableColumnPedidoId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Coluna Pesquisador: como é um objeto aninhado (reserva.pesquisador.nome),
        // usamos CellFactory customizada
        tableColumnPedidoPesquisador.setCellValueFactory(data -> {
            PedidoManutencao p = data.getValue();
            String nome = (p.getReserva() != null && p.getReserva().getPesquisador() != null)
                    ? p.getReserva().getPesquisador().getNome()
                    : "?";
            return new javafx.beans.property.SimpleStringProperty(nome);
        });

        // Coluna Laboratório
        tableColumnPedidoLaboratorio.setCellValueFactory(data -> {
            PedidoManutencao p = data.getValue();
            String nome = (p.getLaboratorio() != null) ? p.getLaboratorio().getNome() : "?";
            return new javafx.beans.property.SimpleStringProperty(nome);
        });

        // Coluna Hora do Pedido
        tableColumnPedidoHora.setCellValueFactory(data -> {
            PedidoManutencao p = data.getValue();
            String hora = (p.getHoraPedido() != null) ? p.getHoraPedido().format(FORMATTER) : "?";
            return new javafx.beans.property.SimpleStringProperty(hora);
        });

        // Coluna Status
        tableColumnPedidoStatus.setCellValueFactory(data -> {
            boolean resolvido = data.getValue().isStatusResolvido();
            return new javafx.beans.property.SimpleStringProperty(
                    resolvido ? "✔ Resolvido" : "⏳ Pendente");
        });

        List<PedidoManutencao> lista = pedidoService.listarTodos();
        ObservableList<PedidoManutencao> obsList = FXCollections.observableArrayList(lista);
        tableViewPedidos.setItems(obsList);
    }

    // Atualizar estado dos botões (habilitar/desabilitar)
    private void atualizarEstadoBotoes() {
        boolean pesquisadorOk = pesquisadorValidado != null;
        boolean labSelecionado = comboBoxLaboratorio.getSelectionModel().getSelectedItem() != null;
        boolean reservaOk = reservaEncontrada != null;

        // "Verificar Reserva" só habilita se pesquisador válido E laboratório selecionado
        buttonVerificarReserva.setDisable(!(pesquisadorOk && labSelecionado));

        // "Registrar Pedido" só habilita se pesquisador válido E reserva encontrada
        buttonRegistrarPedido.setDisable(!(pesquisadorOk && reservaOk));
    }

    // Limpar o formulário após cadastro bem-sucedido
    private void limparFormulario() {
        textFieldMatricula.clear();
        labelNomePesquisador.setText("");
        comboBoxLaboratorio.getSelectionModel().clearSelection();
        labelInfoReserva.setText("");
        textAreaDescricao.clear();
        pesquisadorValidado = null;
        reservaEncontrada = null;
        atualizarEstadoBotoes();
    }

    // Método auxiliar para alertas de erro
    private void exibirAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.show();
    }
}
