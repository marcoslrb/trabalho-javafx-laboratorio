package javafx.laboratorio.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.laboratorio.models.domain.Laboratorio;

/**
 * Controller do Dialog de cadastro/edição de Laboratório.
 */
public class FXMLAnchorPaneCadastrosLaboratoriosDialogController implements Initializable {

    @FXML
    private TextField textFieldNome;
    @FXML
    private TextField textFieldArea;
    @FXML
    private TextArea textAreaDescricao;
    @FXML
    private CheckBox checkBoxFuncional;

    private Stage dialogStage;
    private boolean buttonConfirmarClicked = false;
    private Laboratorio laboratorio;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Validação em tempo real: Nome (Máx 100 caracteres)
        textFieldNome.setTextFormatter(new TextFormatter<>(change -> 
            change.getControlNewText().length() <= 100 ? change : null));
        textFieldNome.setPromptText("Nome do Laboratório (máx 100 caracteres)");

        // Validação em tempo real: Área (Máx 50 caracteres)
        textFieldArea.setTextFormatter(new TextFormatter<>(change -> 
            change.getControlNewText().length() <= 50 ? change : null));
        textFieldArea.setPromptText("Área de Atuação (máx 50 caracteres)");

        // Validação em tempo real: Descrição (Máx 300 caracteres)
        textAreaDescricao.setTextFormatter(new TextFormatter<>(change -> 
            change.getControlNewText().length() <= 300 ? change : null));
        textAreaDescricao.setPromptText("Descrição detalhada (máx 300 caracteres)");
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

    /**
     * Pré-popula o formulário com os dados do laboratório.
     * Se for um laboratório novo (id == 0), os campos ficam em branco.
     * Se for edição, preenche com os dados atuais.
     */
    public void setLaboratorio(Laboratorio laboratorio) {
        this.laboratorio = laboratorio;
        if (this.laboratorio.getId() != 0) {
            // Modo edição: preenche os campos
            textFieldNome.setText(this.laboratorio.getNome());
            textFieldArea.setText(this.laboratorio.getArea());
            textAreaDescricao.setText(this.laboratorio.getDescricao());
            checkBoxFuncional.setSelected(this.laboratorio.isFuncional());
        } else {
            // Modo inserção: checkbox marcado por padrão (laboratório começa funcional)
            checkBoxFuncional.setSelected(true);
        }
    }

    // =========================================================
    // Validação dos campos do formulário
    // =========================================================
    private boolean validarEntradaDeDados() {
        String errorMessage = "";

        if (textFieldNome.getText() == null || textFieldNome.getText().trim().isEmpty()) {
            errorMessage += "Nome é obrigatório!\n";
        } else if (textFieldNome.getText().length() > 100) {
            errorMessage += "Nome deve ter no máximo 100 caracteres.\n";
        }

        if (textFieldArea.getText() == null || textFieldArea.getText().trim().isEmpty()) {
            errorMessage += "Área é obrigatória!\n";
        } else if (textFieldArea.getText().length() > 50) {
            errorMessage += "Área deve ter no máximo 50 caracteres.\n";
        }

        if (textAreaDescricao.getText() != null && textAreaDescricao.getText().length() > 300) {
            errorMessage += "Descrição deve ter no máximo 300 caracteres.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro na validação");
            alert.setHeaderText("Campos inválidos, por favor corrija:");
            alert.setContentText(errorMessage);
            alert.show();
            return false;
        }
    }

    // =========================================================
    // BOTÃO CONFIRMAR
    // =========================================================
    @FXML
    public void handleButtonConfirmar() {
        if (validarEntradaDeDados()) {
            // Preenche o objeto laboratorio com os dados do formulário
            laboratorio.setNome(textFieldNome.getText().trim());
            laboratorio.setArea(textFieldArea.getText().trim());
            laboratorio.setDescricao(textAreaDescricao.getText()); // pode ser null/vazio
            laboratorio.setFuncional(checkBoxFuncional.isSelected());

            buttonConfirmarClicked = true;
            dialogStage.close();
        }
    }

    // =========================================================
    // BOTÃO CANCELAR
    // =========================================================
    @FXML
    public void handleButtonCancelar() {
        dialogStage.close();
    }
}
