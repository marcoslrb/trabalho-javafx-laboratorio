package javafx.laboratorio.controllers;

import java.net.URL;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.laboratorio.models.dao.LaboratorioDAO;
import javafx.laboratorio.models.dao.PesquisadorDAO;
import javafx.laboratorio.models.dao.ReservaDAO;
import javafx.laboratorio.models.database.Database;
import javafx.laboratorio.models.database.DatabaseFactory;
import javafx.laboratorio.models.domain.Laboratorio;
import javafx.laboratorio.models.domain.Pesquisador;
import javafx.laboratorio.models.domain.Reserva;

public class FXMLAnchorPaneProcessosReservasDialogController implements Initializable {

    @FXML private ComboBox<Pesquisador> comboBoxReservaPesquisador;
    @FXML private ComboBox<Laboratorio> comboBoxReservaLaboratorio;
    @FXML private DatePicker datePickerDataInicio;
    @FXML private TextField textFieldHoraInicio;
    @FXML private DatePicker datePickerDataFim;
    @FXML private TextField textFieldHoraFim;
    @FXML private Button buttonConfirmar;
    @FXML private Button buttonCancelar;

    private Stage dialogStage;
    private boolean buttonConfirmarClicked = false;
    private Reserva reserva;

    private final Database database = DatabaseFactory.getDatabase("postgresql");
    private final Connection connection = database.conectar();
    private final PesquisadorDAO pesquisadorDAO = new PesquisadorDAO();
    private final LaboratorioDAO laboratorioDAO = new LaboratorioDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pesquisadorDAO.setConnection(connection);
        laboratorioDAO.setConnection(connection);
        reservaDAO.setConnection(connection);
        carregarComboBoxes();

        // Validação de Hora Início (Máx 5 caracteres, só permite números e :)
        textFieldHoraInicio.setTextFormatter(new TextFormatter<>(change -> {
            String novoTexto = change.getControlNewText();
            return (novoTexto.matches("[0-9:]*") && novoTexto.length() <= 5) ? change : null;
        }));
        textFieldHoraInicio.setPromptText("HH:mm");
        textFieldHoraInicio.setTooltip(new Tooltip("Digite a hora no formato HH:mm (ex: 14:30)"));

        // Validação de Hora Fim (Máx 5 caracteres, só permite números e :)
        textFieldHoraFim.setTextFormatter(new TextFormatter<>(change -> {
            String novoTexto = change.getControlNewText();
            return (novoTexto.matches("[0-9:]*") && novoTexto.length() <= 5) ? change : null;
        }));
        textFieldHoraFim.setPromptText("HH:mm");
        textFieldHoraFim.setTooltip(new Tooltip("Digite a hora no formato HH:mm (ex: 16:00)"));
    }

    public void carregarComboBoxes() {
        List<Pesquisador> listPesquisadores = pesquisadorDAO.listar();
        comboBoxReservaPesquisador.setItems(FXCollections.observableArrayList(listPesquisadores));

        List<Laboratorio> listLaboratorios = laboratorioDAO.listar();
        comboBoxReservaLaboratorio.setItems(FXCollections.observableArrayList(listLaboratorios));
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;

        // Se o ID for diferente de 0, significa que estamos alterando uma reserva existente
        if (reserva.getIdReserva() != 0) {
            // 1. Seleciona o Pesquisador no ComboBox comparando pela Matrícula
            for (Pesquisador p : comboBoxReservaPesquisador.getItems()) {
                if (p.getMatricula().equals(reserva.getPesquisador().getMatricula())) {
                    comboBoxReservaPesquisador.getSelectionModel().select(p);
                    break;
                }
            }

            // 2. Seleciona o Laboratório no ComboBox comparando pelo ID
            for (Laboratorio l : comboBoxReservaLaboratorio.getItems()) {
                if (l.getId() == reserva.getLaboratorio().getId()) {
                    comboBoxReservaLaboratorio.getSelectionModel().select(l);
                    break;
                }
            }

            // 3. Preenche as Datas e Horas (Formatando o LocalDateTime)
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            datePickerDataInicio.setValue(reserva.getDataHoraInicio().toLocalDate());
            textFieldHoraInicio.setText(reserva.getDataHoraInicio().toLocalTime().format(timeFormatter));
            
            datePickerDataFim.setValue(reserva.getDataHoraFim().toLocalDate());
            textFieldHoraFim.setText(reserva.getDataHoraFim().toLocalTime().format(timeFormatter));
        }
    }

    @FXML
    public void handleButtonConfirmar() {
        if (validarEntradaDeDados()) {
            reserva.setPesquisador(comboBoxReservaPesquisador.getSelectionModel().getSelectedItem());
            reserva.setLaboratorio(comboBoxReservaLaboratorio.getSelectionModel().getSelectedItem());
            
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");
            reserva.setDataHoraInicio(LocalDateTime.of(datePickerDataInicio.getValue(), LocalTime.parse(textFieldHoraInicio.getText(), timeFormatter)));
            reserva.setDataHoraFim(LocalDateTime.of(datePickerDataFim.getValue(), LocalTime.parse(textFieldHoraFim.getText(), timeFormatter)));

            buttonConfirmarClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    public void handleButtonCancelar() {
        dialogStage.close();
    }

    private boolean validarEntradaDeDados() {
        String errorMessage = "";
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("H:mm");

        if (comboBoxReservaPesquisador.getSelectionModel().getSelectedItem() == null) errorMessage += "Selecione um pesquisador!\n";
        if (comboBoxReservaLaboratorio.getSelectionModel().getSelectedItem() == null) errorMessage += "Selecione um laboratório!\n";
        if (datePickerDataInicio.getValue() == null) errorMessage += "Data de início inválida!\n";
        if (datePickerDataFim.getValue() == null) errorMessage += "Data de término inválida!\n";

        LocalTime horaInicio = null;
        LocalTime horaFim = null;

        try {
            horaInicio = LocalTime.parse(textFieldHoraInicio.getText(), timeFormatter);
        } catch (DateTimeParseException | NullPointerException e) {
            errorMessage += "Hora de início inválida!\n";
        }

        try {
            horaFim = LocalTime.parse(textFieldHoraFim.getText(), timeFormatter);
        } catch (DateTimeParseException | NullPointerException e) {
            errorMessage += "Hora de término inválida!\n";
        }

        if (errorMessage.length() == 0) {
            Pesquisador p = comboBoxReservaPesquisador.getValue();
            Laboratorio l = comboBoxReservaLaboratorio.getValue();
            LocalDateTime inicio = LocalDateTime.of(datePickerDataInicio.getValue(), horaInicio);
            LocalDateTime fim = LocalDateTime.of(datePickerDataFim.getValue(), horaFim);

            if (!fim.isAfter(inicio)) {
                errorMessage += "O término deve ser após o início!\n";
            } else {
                // Validações no banco passando o ID atual para ignorar a própria reserva
                if (reservaDAO.isLaboratorioOcupado(l.getId(), inicio, fim, reserva.getIdReserva())) {
                    errorMessage += "Laboratório ocupado neste horário!\n";
                }
                if (reservaDAO.contarReservasNaSemana(p.getMatricula(), l.getId(), inicio, reserva.getIdReserva()) >= 5) {
                    errorMessage += "Limite de 5 reservas semanais excedido!\n";
                }
            }
        }

        if (errorMessage.length() == 0) return true;

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro na Reserva");
        alert.setContentText(errorMessage);
        alert.show();
        return false;
    }

    public void setDialogStage(Stage dialogStage) { this.dialogStage = dialogStage; }
    public boolean isButtonConfirmarClicked() { return buttonConfirmarClicked; }
}