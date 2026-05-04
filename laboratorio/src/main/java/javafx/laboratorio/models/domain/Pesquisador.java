package javafx.laboratorio.models.domain;

import java.io.Serializable;

/**
 * Entidade que representa o Pesquisador no sistema de laboratórios.
 * Atende aos requisitos de Matrícula, Nome, Email, CPF e Telefone.
 */
public class Pesquisador implements Serializable {

    private String matricula; // PK
    private String nome;      // NOT NULL
    private String email;     // NOT NULL, UNIQUE
    private String cpf;       // NOT NULL, UNIQUE
    private String telefone;  // UNIQUE, NULL

    // Construtor padrão (necessário para alguns frameworks e flexibilidade)
    public Pesquisador() {
    }

    // Construtor completo para facilitar a criação via Controller ou DAO
    public Pesquisador(String matricula, String nome, String email, String cpf, String telefone) {
        this.matricula = matricula;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.telefone = telefone;
    }

    // Getters e Setters
    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    // toString útil para depuração e para exibir o nome em ComboBoxes do JavaFX
    @Override
    public String toString() {
        return this.nome;
    }
}