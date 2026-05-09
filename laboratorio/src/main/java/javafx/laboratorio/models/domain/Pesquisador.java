package javafx.laboratorio.models.domain;

import java.io.Serializable;

public class Pesquisador implements Serializable {

    private String matricula; // PK
    private String nome;      // NOT NULL
    private String email;     // NOT NULL, UNIQUE
    private String cpf;       // NOT NULL, UNIQUE
    private String telefone;  // UNIQUE, NULL
    private boolean suspenso; // NOT NULL

    public Pesquisador() {
    }

    public Pesquisador(String matricula, String nome, String email, String cpf, String telefone) {
        this.matricula = matricula;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.telefone = telefone;
    }

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
    
    public boolean isSuspenso() {
    return suspenso;
    }

    public void setSuspenso(boolean suspenso) {
        this.suspenso = suspenso;
    }

    @Override
    public String toString() {
        return this.nome;
    }
}