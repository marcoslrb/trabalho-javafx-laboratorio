package javafx.laboratorio.models.domain;

public class Laboratorio {

    private int idLaboratorio;
    private String nome;
    private int capacidade; 

    public Laboratorio() {
    }

    public Laboratorio(int idLaboratorio, String nome) {
        this.idLaboratorio = idLaboratorio;
        this.nome = nome;
    }

    public int getIdLaboratorio() {
        return idLaboratorio;
    }

    public void setIdLaboratorio(int idLaboratorio) {
        this.idLaboratorio = idLaboratorio;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        this.capacidade = capacidade;
    }

    @Override
    public String toString() {
        return this.nome;
    }
}