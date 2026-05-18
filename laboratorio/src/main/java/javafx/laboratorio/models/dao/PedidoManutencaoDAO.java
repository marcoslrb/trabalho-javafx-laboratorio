package javafx.laboratorio.models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.laboratorio.models.domain.Laboratorio;
import javafx.laboratorio.models.domain.PedidoManutencao;
import javafx.laboratorio.models.domain.Pesquisador;
import javafx.laboratorio.models.domain.Reserva;

/**
 * DAO para a entidade PedidoManutencao.
 * Contém: inserção, listagem, busca, atualização (alterar), remoção e atualização de status.
 */
public class PedidoManutencaoDAO {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // INSERIR um novo Pedido de Manutenção
    /**
     * Insere o pedido de manutenção no banco.
     * A hora_pedido é gerada automaticamente pelo banco (DEFAULT CURRENT_TIMESTAMP).
     * O status_resolvido começa sempre como FALSE.
     */
    public boolean inserir(PedidoManutencao pedido) {
        // Nota: não é passado hora_pedido nem status_resolvido pois têm DEFAULT no banco
        String sql = "INSERT INTO pedido_manutencao (id_reserva, hora_pedido, descricao, status_resolvido) "
                   + "VALUES (?, NOW(), ?, FALSE)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, pedido.getReserva().getIdReserva());
            stmt.setString(2, pedido.getDescricao());
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            System.err.println("Erro ao inserir pedido de manutenção: " + ex.getMessage());
            return false;
        }
    }

    // LISTAR todos os Pedidos de Manutenção (com JOINs)
    /**
     * Retorna todos os pedidos de manutenção, com os dados da reserva,
     * pesquisador e laboratório associados.
     */
    public List<PedidoManutencao> listar() {
        String sql = "SELECT pm.id, pm.hora_pedido, pm.descricao, pm.status_resolvido, "
                   + "r.id AS id_reserva, r.data_inicio, r.data_fim, r.matricula_pesquisador, "
                   + "p.nome AS nome_pesquisador, "
                   + "l.id AS id_lab, l.nome AS nome_lab, l.area, l.descricao AS desc_lab, l.funcional "
                   + "FROM pedido_manutencao pm "
                   + "JOIN reserva r ON pm.id_reserva = r.id "
                   + "JOIN pesquisador p ON r.matricula_pesquisador = p.matricula "
                   + "JOIN laboratorio l ON r.id_laboratorio = l.id "
                   + "ORDER BY pm.hora_pedido DESC";
        List<PedidoManutencao> retorno = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                retorno.add(mapearResultSet(rs));
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao listar pedidos de manutenção: " + ex.getMessage());
        }
        return retorno;
    }

    //  VERIFICAR DUPLICIDADE (Regra de Negócio 2)
    /**
     * Verifica se já existe um pedido de manutenção PENDENTE (status_resolvido = FALSE)
     * para o mesmo pesquisador no mesmo laboratório.
     *
     * Regra: um pesquisador não pode abrir dois chamados pendentes para o mesmo laboratório.
     */
    public boolean existePedidoPendente(String matriculaPesquisador, int idLaboratorio) {
        String sql = "SELECT COUNT(*) FROM pedido_manutencao pm "
                   + "JOIN reserva r ON pm.id_reserva = r.id "
                   + "WHERE r.matricula_pesquisador = ? "
                   + "  AND r.id_laboratorio = ? "
                   + "  AND pm.status_resolvido = FALSE";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, matriculaPesquisador);
            stmt.setInt(2, idLaboratorio);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao verificar pedido pendente: " + ex.getMessage());
        }
        return false;
    }

    // BUSCAR reserva válida (Regra de Negócio 3)
    /**
     * Busca uma reserva nos últimos 5 dias para o pesquisador + laboratório informados.
     *
     * Regra: o pedido só pode ser aberto se o pesquisador usou o laboratório
     *        (teve uma reserva) nos últimos 5 dias.
     */
    public Reserva buscarReservaValida(String matriculaPesquisador, int idLaboratorio) {
        String sql = "SELECT r.id, r.data_inicio, r.data_fim, r.matricula_pesquisador, r.id_laboratorio, "
                   + "p.matricula, p.nome AS nome_pesquisador, p.email, p.cpf, p.telefone, p.suspenso, "
                   + "l.id AS id_lab, l.nome AS nome_lab, l.area, l.descricao AS desc_lab, l.funcional "
                   + "FROM reserva r "
                   + "JOIN pesquisador p ON r.matricula_pesquisador = p.matricula "
                   + "JOIN laboratorio l ON r.id_laboratorio = l.id "
                   + "WHERE r.matricula_pesquisador = ? "
                   + "  AND r.id_laboratorio = ? "
                   + "  AND r.data_fim >= NOW() - INTERVAL '5 days' "
                   + "  AND r.data_fim <= NOW() "
                   + "ORDER BY r.data_fim DESC "
                   + "LIMIT 1";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, matriculaPesquisador);
            stmt.setInt(2, idLaboratorio);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Monta o objeto Pesquisador
                Pesquisador p = new Pesquisador();
                p.setMatricula(rs.getString("matricula_pesquisador"));
                p.setNome(rs.getString("nome_pesquisador"));
                p.setEmail(rs.getString("email"));
                p.setCpf(rs.getString("cpf"));
                p.setTelefone(rs.getString("telefone"));
                p.setSuspenso(rs.getBoolean("suspenso"));

                // Monta o objeto Laboratorio
                Laboratorio lab = new Laboratorio();
                lab.setId(rs.getInt("id_lab"));
                lab.setNome(rs.getString("nome_lab"));
                lab.setArea(rs.getString("area"));
                lab.setDescricao(rs.getString("desc_lab"));
                lab.setFuncional(rs.getBoolean("funcional"));

                // Monta a Reserva
                Reserva reserva = new Reserva();
                reserva.setIdReserva(rs.getInt("id"));
                reserva.setPesquisador(p);
                reserva.setLaboratorio(lab);
                Timestamp inicio = rs.getTimestamp("data_inicio");
                Timestamp fim = rs.getTimestamp("data_fim");
                if (inicio != null) reserva.setDataHoraInicio(inicio.toLocalDateTime());
                if (fim != null) reserva.setDataHoraFim(fim.toLocalDateTime());

                return reserva;
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao buscar reserva válida: " + ex.getMessage());
        }
        return null; // null = não existe reserva nos últimos 5 dias
    }

    // BUSCAR POR ID
    /**
     * Busca um pedido de manutenção pelo seu ID.
     */
    public PedidoManutencao buscarPorId(int idPedido) {
        String sql = "SELECT pm.id, pm.hora_pedido, pm.descricao, pm.status_resolvido, "
                   + "r.id AS id_reserva, r.data_inicio, r.data_fim, r.matricula_pesquisador, "
                   + "p.nome AS nome_pesquisador, "
                   + "l.id AS id_lab, l.nome AS nome_lab, l.area, l.descricao AS desc_lab, l.funcional "
                   + "FROM pedido_manutencao pm "
                   + "JOIN reserva r ON pm.id_reserva = r.id "
                   + "JOIN pesquisador p ON r.matricula_pesquisador = p.matricula "
                   + "JOIN laboratorio l ON r.id_laboratorio = l.id "
                   + "WHERE pm.id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idPedido);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapearResultSet(rs);
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao buscar pedido por ID: " + ex.getMessage());
        }
        return null;
    }

    // ATUALIZAR status do pedido para resolvido (Regra de Negócio 4)
    /**
     * Marca um pedido de manutenção como resolvido (status_resolvido = TRUE).
     * Só atualiza se o pedido ainda estiver pendente (status_resolvido = FALSE).
     */
    public boolean marcarComoResolvido(int idPedido) {
        String sql = "UPDATE pedido_manutencao SET status_resolvido = TRUE WHERE id = ? AND status_resolvido = FALSE";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idPedido);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException ex) {
            System.err.println("Erro ao atualizar status do pedido: " + ex.getMessage());
            return false;
        }
    }

    // ALTERAR descrição e/ou status de um Pedido de Manutenção
    /**
     * Edita a descrição e o status_resolvido de um pedido existente.
     */
    public boolean alterar(PedidoManutencao pedido) {
        String sql = "UPDATE pedido_manutencao SET descricao = ?, status_resolvido = ? WHERE id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, pedido.getDescricao());
            stmt.setBoolean(2, pedido.isStatusResolvido());
            stmt.setInt(3, pedido.getId());
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException ex) {
            System.err.println("Erro ao alterar pedido de manutenção: " + ex.getMessage());
            return false;
        }
    }

    // REMOVER um Pedido de Manutenção
    /**
     * Remove um pedido de manutenção pelo seu ID.
     */
    public boolean remover(int idPedido) {
        String sql = "DELETE FROM pedido_manutencao WHERE id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idPedido);
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            System.err.println("Erro ao remover pedido de manutenção: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Busca a quantidade de pedidos de manutenção pendentes por laboratório.
     * Retorna um Map onde a chave é o nome do laboratório e o valor é o total de falhas.
     */
    public Map<String, Integer> obterDadosGraficoConfiabilidade() {
        String sql = "SELECT l.nome, COUNT(pm.id) AS total_falhas "
                   + "FROM laboratorio l "
                   + "JOIN reserva r ON r.id_laboratorio = l.id "
                   + "JOIN pedido_manutencao pm ON pm.id_reserva = r.id "
                   + "WHERE pm.status_resolvido = FALSE "
                   + "GROUP BY l.nome "
                   + "ORDER BY total_falhas DESC";
        Map<String, Integer> retorno = new LinkedHashMap<>();
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                retorno.put(rs.getString("nome"), rs.getInt("total_falhas"));
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao obter dados para gráfico: " + ex.getMessage());
        }
        return retorno;
    }

    // MÉTODO PRIVADO AUXILIAR: mapear ResultSet -> PedidoManutencao
    /**
     * Converte uma linha do ResultSet em um objeto PedidoManutencao completo.
     * Usado internamente para evitar repetição de código.
     */
    private PedidoManutencao mapearResultSet(ResultSet rs) throws SQLException {
        // Monta Pesquisador
        Pesquisador p = new Pesquisador();
        p.setMatricula(rs.getString("matricula_pesquisador"));
        p.setNome(rs.getString("nome_pesquisador"));

        // Monta Laboratorio
        Laboratorio lab = new Laboratorio();
        lab.setId(rs.getInt("id_lab"));
        lab.setNome(rs.getString("nome_lab"));
        lab.setArea(rs.getString("area"));
        lab.setDescricao(rs.getString("desc_lab"));
        lab.setFuncional(rs.getBoolean("funcional"));

        // Monta Reserva
        Reserva reserva = new Reserva();
        reserva.setIdReserva(rs.getInt("id_reserva"));
        reserva.setPesquisador(p);
        reserva.setLaboratorio(lab);
        Timestamp inicio = rs.getTimestamp("data_inicio");
        Timestamp fim = rs.getTimestamp("data_fim");
        if (inicio != null) reserva.setDataHoraInicio(inicio.toLocalDateTime());
        if (fim != null) reserva.setDataHoraFim(fim.toLocalDateTime());

        // Monta PedidoManutencao
        PedidoManutencao pedido = new PedidoManutencao();
        pedido.setId(rs.getInt("id"));
        pedido.setReserva(reserva);
        pedido.setLaboratorio(lab);
        Timestamp horaPedido = rs.getTimestamp("hora_pedido");
        if (horaPedido != null) pedido.setHoraPedido(horaPedido.toLocalDateTime());
        pedido.setDescricao(rs.getString("descricao"));
        pedido.setStatusResolvido(rs.getBoolean("status_resolvido"));

        return pedido;
    }
}
