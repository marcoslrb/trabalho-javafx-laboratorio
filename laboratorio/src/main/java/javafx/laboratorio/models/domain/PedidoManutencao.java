package javafx.laboratorio.models.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidade que representa um Pedido de Manutenção.
 * Mapeada para a tabela 'pedido_manutencao' no banco de dados.
 *
 * Um PedidoManutencao está vinculado a uma Reserva (e através dela, a um Pesquisador e a um Laboratorio).
 */
public class PedidoManutencao implements Serializable {

    private int id;                      // PK, SERIAL (auto-gerado)
    private Reserva reserva;             // FK -> reserva.id (objeto completo)
    private Laboratorio laboratorio;     // FK -> laboratorio.id (objeto completo, para facilitar a tela)
    private LocalDateTime horaPedido;    // TIMESTAMP, NOT NULL
    private String descricao;            // VARCHAR(500), NOT NULL
    private boolean statusResolvido;     // BOOLEAN, NOT NULL (false = pendente, true = resolvido)

    // Construtor padrão
    public PedidoManutencao() {
    }

    // Construtor para inserção (sem id, sem horaPedido - o banco gera automaticamente)
    public PedidoManutencao(Reserva reserva, Laboratorio laboratorio, String descricao) {
        this.reserva = reserva;
        this.laboratorio = laboratorio;
        this.descricao = descricao;
        this.statusResolvido = false; // Sempre começa como não resolvido
    }

    // Construtor completo (usado ao carregar do banco)
    public PedidoManutencao(int id, Reserva reserva, Laboratorio laboratorio,
                             LocalDateTime horaPedido, String descricao, boolean statusResolvido) {
        this.id = id;
        this.reserva = reserva;
        this.laboratorio = laboratorio;
        this.horaPedido = horaPedido;
        this.descricao = descricao;
        this.statusResolvido = statusResolvido;
    }

    // --- Getters e Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Laboratorio getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(Laboratorio laboratorio) {
        this.laboratorio = laboratorio;
    }

    public LocalDateTime getHoraPedido() {
        return horaPedido;
    }

    public void setHoraPedido(LocalDateTime horaPedido) {
        this.horaPedido = horaPedido;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isStatusResolvido() {
        return statusResolvido;
    }

    public void setStatusResolvido(boolean statusResolvido) {
        this.statusResolvido = statusResolvido;
    }

    @Override
    public String toString() {
        return "Pedido #" + id + " - Lab: " + (laboratorio != null ? laboratorio.getNome() : "?")
                + " | " + (statusResolvido ? "Resolvido" : "Pendente");
    }
}
