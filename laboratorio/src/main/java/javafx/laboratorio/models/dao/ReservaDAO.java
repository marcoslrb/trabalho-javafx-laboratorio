package javafx.laboratorio.models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.laboratorio.models.domain.Laboratorio;
import javafx.laboratorio.models.domain.Pesquisador;
import javafx.laboratorio.models.domain.Reserva;

public class ReservaDAO {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // =====================================================================
    // INTERAÇÃO 6: INSERT (Com transação)
    // Grava uma nova reserva de forma segura com commit/rollback.
    // =====================================================================
    public boolean inserir(Reserva reserva) {
        String sql = "INSERT INTO reserva (matricula_pesquisador, id_laboratorio, data_inicio, data_fim) VALUES (?, ?, ?, ?)";
        
        try {
            connection.setAutoCommit(false); // Trava o auto-save
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, reserva.getPesquisador().getMatricula());
            stmt.setInt(2, reserva.getLaboratorio().getId()); 
            
            stmt.setTimestamp(3, Timestamp.valueOf(reserva.getDataHoraInicio()));
            stmt.setTimestamp(4, Timestamp.valueOf(reserva.getDataHoraFim()));
            
            stmt.execute();
            connection.commit(); // Confirma a gravação
            return true;
            
        } catch (SQLException ex) {
            try { connection.rollback(); } catch (SQLException rollbackEx) { }
            System.err.println("Erro ao tentar salvar a reserva: " + ex.getMessage());
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { }
        }
    }

    // =====================================================================
    // INTERAÇÃO EXTRA: UPDATE (Com transação)
    // Atualiza uma reserva existente (usado no botão Alterar).
    // =====================================================================
    public boolean alterar(Reserva reserva) {
        String sql = "UPDATE reserva SET matricula_pesquisador=?, id_laboratorio=?, data_inicio=?, data_fim=? WHERE id=?";
        
        try {
            connection.setAutoCommit(false);
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, reserva.getPesquisador().getMatricula());
            stmt.setInt(2, reserva.getLaboratorio().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(reserva.getDataHoraInicio()));
            stmt.setTimestamp(4, Timestamp.valueOf(reserva.getDataHoraFim()));
            stmt.setInt(5, reserva.getIdReserva());
            
            stmt.execute();
            connection.commit();
            return true;
            
        } catch (SQLException ex) {
            try { connection.rollback(); } catch (SQLException rollbackEx) { }
            System.err.println("Erro ao alterar a reserva: " + ex.getMessage());
            return false;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { }
        }
    }

    // =====================================================================
    // INTERAÇÃO EXTRA: DELETE
    // Remove a reserva selecionada na tabela.
    // =====================================================================
    public boolean remover(Reserva reserva) {
        String sql = "DELETE FROM reserva WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, reserva.getIdReserva());
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            System.err.println("Erro ao remover a reserva (pode estar vinculada a uma manutenção): " + ex.getMessage());
            return false;
        }
    }

    // =====================================================================
    // INTERAÇÃO 1: SELECT principal (Listar)
    // Traz a lista completa com JOIN para popular a TableView na tela.
    // =====================================================================
    public List<Reserva> listar() {
        String sql = "SELECT r.id AS id_reserva, r.data_inicio, r.data_fim, " +
                     "p.matricula, p.nome AS nome_pesquisador, " +
                     "l.id AS id_laboratorio, l.nome AS nome_laboratorio " +
                     "FROM reserva r " +
                     "INNER JOIN pesquisador p ON r.matricula_pesquisador = p.matricula " +
                     "INNER JOIN laboratorio l ON r.id_laboratorio = l.id";
                     
        List<Reserva> retorno = new ArrayList<>();
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultado = stmt.executeQuery();
            
            while (resultado.next()) {
                // Monta o Pesquisador
                Pesquisador pesquisador = new Pesquisador();
                pesquisador.setMatricula(resultado.getString("matricula"));
                pesquisador.setNome(resultado.getString("nome_pesquisador"));
                
                // Monta o Laboratório
                Laboratorio laboratorio = new Laboratorio();
                laboratorio.setId(resultado.getInt("id_laboratorio")); 
                laboratorio.setNome(resultado.getString("nome_laboratorio"));
                
                // Junta tudo na Reserva
                Reserva reserva = new Reserva();
                reserva.setIdReserva(resultado.getInt("id_reserva"));
                reserva.setPesquisador(pesquisador);
                reserva.setLaboratorio(laboratorio);
                
                // Conversão de Timestamp (banco) para LocalDateTime (Java)
                if (resultado.getTimestamp("data_inicio") != null) {
                    reserva.setDataHoraInicio(resultado.getTimestamp("data_inicio").toLocalDateTime());
                }
                if (resultado.getTimestamp("data_fim") != null) {
                    reserva.setDataHoraFim(resultado.getTimestamp("data_fim").toLocalDateTime());
                }
                
                retorno.add(reserva);
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao listar as reservas: " + ex.getMessage());
        }
        return retorno;
    }

    // =======================================================================
    // REGRAS DE NEGÓCIO DA DISCIPLINA
    // =======================================================================

    /**
     * INTERAÇÃO 4 (Regra 1): Checa se o laboratório já está ocupado.
     * O "AND id != ?" serve para ignorar a própria reserva caso estejamos a fazer um UPDATE.
     */
    public boolean isLaboratorioOcupado(int idLaboratorio, LocalDateTime inicio, LocalDateTime fim, int idReserva) {
        String sql = "SELECT COUNT(*) AS total FROM reserva WHERE id_laboratorio = ? AND data_inicio < ? AND data_fim > ? AND id != ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idLaboratorio);
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            stmt.setTimestamp(3, Timestamp.valueOf(inicio));
            stmt.setInt(4, idReserva); 
            
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                return resultado.getInt("total") > 0;
            }
        } catch (SQLException ex) {
            System.err.println("Falha na busca de disponibilidade: " + ex.getMessage());
        }
        return true; 
    }

    /**
     * INTERAÇÃO 5 (Regra 2): Limite de 5 reservas na mesma semana.
     * O uso do CAST(? AS TIMESTAMP) resolve o erro de sintaxe do PostgreSQL.
     */
    public int contarReservasNaSemana(String matricula, int idLaboratorio, LocalDateTime dataPretendida, int idReserva) {
        String sql = "SELECT COUNT(*) AS total FROM reserva "
                   + "WHERE matricula_pesquisador = ? AND id_laboratorio = ? AND id != ? "
                   + "AND EXTRACT(WEEK FROM data_inicio) = EXTRACT(WEEK FROM CAST(? AS TIMESTAMP)) "
                   + "AND EXTRACT(YEAR FROM data_inicio) = EXTRACT(YEAR FROM CAST(? AS TIMESTAMP))";
                   
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, matricula);
            stmt.setInt(2, idLaboratorio);
            stmt.setInt(3, idReserva); 
            
            Timestamp dataBusca = Timestamp.valueOf(dataPretendida);
            stmt.setTimestamp(4, dataBusca); 
            stmt.setTimestamp(5, dataBusca); 
            
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                return resultado.getInt("total");
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao validar o limite da semana: " + ex.getMessage());
        }
        return 99; // Bloqueia se der erro no banco
    }
    
    public java.util.Map<String, Integer> obterDadosGraficoReservas() {
    String sql = "SELECT l.nome, COUNT(r.id) AS total_reservas " +
                 "FROM reserva r JOIN laboratorio l ON r.id_laboratorio = l.id " +
                 "GROUP BY l.nome ORDER BY total_reservas DESC";
    java.util.Map<String, Integer> retorno = new java.util.LinkedHashMap<>();
    try {
        java.sql.PreparedStatement stmt = connection.prepareStatement(sql);
        java.sql.ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            retorno.put(rs.getString("nome"), rs.getInt("total_reservas"));
        }
    } catch (java.sql.SQLException ex) {
        System.err.println("Erro gráfico reservas: " + ex.getMessage());
    }
    return retorno;
    }
}