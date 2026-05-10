package javafx.laboratorio.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.laboratorio.models.domain.Pesquisador;
import javafx.scene.control.Alert;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;

public class FXMLAnchorPaneCadastrosPesquisadoresDialogController implements Initializable {

    @FXML
    private TextField textFieldMatricula;
    @FXML
    private TextField textFieldNome;
    @FXML
    private TextField textFieldEmail;
    @FXML
    private TextField textFieldCPF;
    @FXML
    private TextField textFieldTelefone;
    @FXML
    private CheckBox checkBoxSuspenso;
    @FXML
    private Button buttonConfirmar;
    @FXML
    private Button buttonCancelar;

    private Stage dialogStage;
    private boolean buttonConfirmarClicked = false;
    private Pesquisador pesquisador;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Validação em tempo real: Matrícula (Máx 8 caracteres)
        textFieldMatricula.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().length() <= 8 ? change : null));
        textFieldMatricula.setPromptText("Máx. 8 caracteres");
        textFieldMatricula.setTooltip(new Tooltip("Digite a matrícula do pesquisador (até 8 caracteres)."));

        // Validação em tempo real: Nome (Máx 100 caracteres)
        textFieldNome.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().length() <= 100 ? change : null));
        textFieldNome.setPromptText("Nome completo");

        // Validação em tempo real: E-mail (Máx 50 caracteres)
        textFieldEmail.setTextFormatter(
                new TextFormatter<>(change -> change.getControlNewText().length() <= 50 ? change : null));
        textFieldEmail.setPromptText("exemplo@email.com");

        // Validação em tempo real: CPF (Apenas números, Máx 11)
        textFieldCPF.setTextFormatter(new TextFormatter<>(change -> {
            String novoTexto = change.getControlNewText();
            return (novoTexto.matches("\\d*") && novoTexto.length() <= 11) ? change : null;
        }));
        textFieldCPF.setPromptText("Apenas números (11 dígitos)");
        textFieldCPF.setTooltip(new Tooltip("Digite apenas os 11 números do seu CPF."));

        // Validação em tempo real: Telefone (Apenas números, Máx 11)
        textFieldTelefone.setTextFormatter(new TextFormatter<>(change -> {
            String novoTexto = change.getControlNewText();
            return (novoTexto.matches("\\d*") && novoTexto.length() <= 11) ? change : null;
        }));
        textFieldTelefone.setPromptText("Apenas números com DDD");
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isButtonConfirmarClicked() {
        return buttonConfirmarClicked;
    }

    public Pesquisador getPesquisador() {
        return pesquisador;
    }

    public void setPesquisador(Pesquisador pesquisador) {
        this.pesquisador = pesquisador;
        if (this.pesquisador.getMatricula() != null) {
            this.textFieldMatricula.setText(this.pesquisador.getMatricula());
            this.textFieldMatricula.setEditable(false);
            this.textFieldMatricula.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #555555; -fx-cursor: not-allowed; -fx-focus-color: transparent;");
            this.textFieldMatricula.setTooltip(new Tooltip("A matrícula é a identidade do registro e não pode ser alterada."));
            this.textFieldNome.setText(this.pesquisador.getNome());
            this.textFieldEmail.setText(this.pesquisador.getEmail());
            this.textFieldCPF.setText(this.pesquisador.getCpf());
            this.textFieldTelefone.setText(this.pesquisador.getTelefone());
            this.checkBoxSuspenso.setSelected(this.pesquisador.isSuspenso());
        }
    }

    private boolean validarEntradaDeDados() {
        String errorMessage = "";

        String matricula = textFieldMatricula.getText() == null ? "" : textFieldMatricula.getText().trim();
        String nome = textFieldNome.getText() == null ? "" : textFieldNome.getText().trim();
        String email = textFieldEmail.getText() == null ? "" : textFieldEmail.getText().trim();
        String cpfNumerico = manterApenasDigitos(textFieldCPF.getText());
        String telefoneNumerico = manterApenasDigitos(textFieldTelefone.getText());

        // Valida Matrícula (Máximo 8 caracteres)
        if (matricula.isEmpty()) {
            errorMessage += "Matrícula inválida! Não pode estar vazia.\n";
        } else if (matricula.length() > 8) {
            errorMessage += "Matrícula muito longa! Máximo de 8 caracteres.\n";
        }

        // Valida Nome (Máximo 100 caracteres)
        if (nome.isEmpty()) {
            errorMessage += "Nome inválido! Não pode estar vazio.\n";
        } else if (nome.length() > 100) {
            errorMessage += "Nome muito longo! Máximo de 100 caracteres.\n";
        }

        // Valida E-mail (Máximo 50 caracteres)
        if (email.isEmpty()) {
            errorMessage += "E-mail inválido! Não pode estar vazio.\n";
        } else if (email.length() > 50) {
            errorMessage += "E-mail muito longo! Máximo de 50 caracteres.\n";
        }

        // Valida CPF (11 dígitos)
        if (cpfNumerico.isEmpty()) {
            errorMessage += "CPF inválido! Não pode estar vazio.\n";
        } else if (cpfNumerico.length() != 11) {
            errorMessage += "CPF inválido! Informe exatamente 11 dígitos.\n";
        }

        // Valida Telefone (Opcional; se informado, 10 ou 11 dígitos)
        if (!telefoneNumerico.isEmpty()
                && telefoneNumerico.length() != 10
                && telefoneNumerico.length() != 11) {
            errorMessage += "Telefone inválido! Informe 10 ou 11 dígitos.\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro na validação");
            alert.setHeaderText("Campos inválidos, por favor, corrija...");
            alert.setContentText(errorMessage);
            alert.show();
            return false; // Retorna falso, o Dialog não fecha
        }
    }

    @FXML
    public void handleButtonConfirmar() {
        if (validarEntradaDeDados()) {
            String matricula = textFieldMatricula.getText().trim();
            String nome = textFieldNome.getText().trim();
            String email = textFieldEmail.getText().trim().toLowerCase();
            String cpf = manterApenasDigitos(textFieldCPF.getText());
            String telefone = manterApenasDigitos(textFieldTelefone.getText());

            pesquisador.setMatricula(matricula);
            pesquisador.setNome(nome);
            pesquisador.setEmail(email);
            pesquisador.setCpf(cpf);
            pesquisador.setTelefone(telefone.isBlank() ? null : telefone);
            pesquisador.setSuspenso(checkBoxSuspenso.isSelected());

            buttonConfirmarClicked = true;
            dialogStage.close();
        }
    }

    private String manterApenasDigitos(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replaceAll("\\D", "");
    }

    @FXML
    public void handleButtonCancelar() {
        dialogStage.close();
    }
}