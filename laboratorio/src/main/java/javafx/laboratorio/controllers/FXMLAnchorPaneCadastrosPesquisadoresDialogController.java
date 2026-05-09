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
            this.textFieldNome.setText(this.pesquisador.getNome());
            this.textFieldEmail.setText(this.pesquisador.getEmail());
            this.textFieldCPF.setText(this.pesquisador.getCpf());
            this.textFieldTelefone.setText(this.pesquisador.getTelefone());
            this.checkBoxSuspenso.setSelected(this.pesquisador.isSuspenso());
        }
    }
    
    private boolean validarEntradaDeDados() {
        String errorMessage = "";

        // Valida Matrícula (Máximo 8 caracteres)
        if (textFieldMatricula.getText() == null || textFieldMatricula.getText().trim().isEmpty()) {
            errorMessage += "Matrícula inválida! Não pode estar vazia.\n";
        } else if (textFieldMatricula.getText().length() > 8) {
            errorMessage += "Matrícula muito longa! Máximo de 8 caracteres.\n";
        }

        // Valida Nome (Máximo 100 caracteres)
        if (textFieldNome.getText() == null || textFieldNome.getText().trim().isEmpty()) {
            errorMessage += "Nome inválido! Não pode estar vazio.\n";
        } else if (textFieldNome.getText().length() > 100) {
            errorMessage += "Nome muito longo! Máximo de 100 caracteres.\n";
        }

        // Valida E-mail (Máximo 50 caracteres)
        if (textFieldEmail.getText() == null || textFieldEmail.getText().trim().isEmpty()) {
            errorMessage += "E-mail inválido! Não pode estar vazio.\n";
        } else if (textFieldEmail.getText().length() > 50) {
            errorMessage += "E-mail muito longo! Máximo de 50 caracteres.\n";
        }

        // Valida CPF (Máximo 11 caracteres)
        if (textFieldCPF.getText() == null || textFieldCPF.getText().trim().isEmpty()) {
            errorMessage += "CPF inválido! Não pode estar vazio.\n";
        } else if (textFieldCPF.getText().length() > 11) {
            errorMessage += "CPF muito longo! Máximo de 11 caracteres (apenas números).\n";
        }

        // Valida Telefone (Opcional, mas se preenchido, máximo 11 caracteres)
        if (textFieldTelefone.getText() != null && textFieldTelefone.getText().length() > 11) {
            errorMessage += "Telefone muito longo! Máximo de 11 caracteres.\n";
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
            pesquisador.setMatricula(textFieldMatricula.getText());
            pesquisador.setNome(textFieldNome.getText());
            pesquisador.setEmail(textFieldEmail.getText());
            pesquisador.setCpf(textFieldCPF.getText());
            pesquisador.setTelefone(textFieldTelefone.getText());
            pesquisador.setSuspenso(checkBoxSuspenso.isSelected());

            buttonConfirmarClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    public void handleButtonCancelar() {
        dialogStage.close();
    }
}