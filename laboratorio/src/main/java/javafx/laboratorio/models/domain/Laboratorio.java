package javafx.laboratorio.models.domain;

import java.io.Serializable;

/**
 * Entidade que representa um Laboratório no sistema.
 * Mapeada para a tabela 'laboratorio' no banco de dados.
 */
public class Laboratorio implements Serializable {

    private int id;               // PK, SERIAL (auto-gerado)
    private String nome;          // VARCHAR(100), NOT NULL
    private String area;          // VARCHAR(50), NOT NULL
    private String descricao;     // VARCHAR(300), NULL (opcional)
    private boolean funcional;    // BOOLEAN, NOT NULL (true = em uso normal, false = em manutenção)

    // Construtor padrão 
    public Laboratorio() {
    }

    // Construtor completo (sem id, pois é auto-gerado pelo banco)
    public Laboratorio(String nome, String area, String descricao, boolean funcional) {
        this.nome = nome;
        this.area = area;
        this.descricao = descricao;
        this.funcional = funcional;
    }

    // Construtor completo (com id, usado ao carregar do banco)
    public Laboratorio(int id, String nome, String area, String descricao, boolean funcional) {
        this.id = id;
        this.nome = nome;
        this.area = area;
        this.descricao = descricao;
        this.funcional = funcional;
    }

    // --- Getters e Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isFuncional() {
        return funcional;
    }

    public void setFuncional(boolean funcional) {
        this.funcional = funcional;
    }

    /**
     * toString() retorna apenas o nome do laboratório.
     * Isso é essencial para que o ComboBox exiba o nome corretamente.
     */
    @Override
    public String toString() {
        return this.nome;
    }
}