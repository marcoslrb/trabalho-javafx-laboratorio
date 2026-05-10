package javafx.laboratorio.models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.laboratorio.models.domain.Pesquisador;

/**
 * DAO mínimo para validação de Pesquisador.
 * Conforme o escopo do trabalho, aqui só precisamos buscar/validar
 * se um pesquisador existe e se não está suspenso.
 * O CRUD completo de Pesquisador já existe em PesquisadorDAO.
 */
public class PesquisadorValidacaoDAO {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // =========================================================
    // BUSCAR PESQUISADOR POR MATRÍCULA (para validação)
    // =========================================================
    /**
     * Busca um pesquisador pela matrícula.
     * Retorna o objeto Pesquisador se encontrado, ou null se não existir.
     */
    public Pesquisador buscarPorMatricula(String matricula) {
        String sql = "SELECT * FROM pesquisador WHERE matricula = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, matricula);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Pesquisador p = new Pesquisador();
                p.setMatricula(rs.getString("matricula"));
                p.setNome(rs.getString("nome"));
                p.setEmail(rs.getString("email"));
                p.setCpf(rs.getString("cpf"));
                p.setTelefone(rs.getString("telefone"));
                p.setSuspenso(rs.getBoolean("suspenso"));
                return p;
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao buscar pesquisador: " + ex.getMessage());
        }
        return null;
    }

    /**
     * Verifica rapidamente se um pesquisador existe E está ativo (não suspenso).
     */
    public boolean isPesquisadorValido(String matricula) {
        String sql = "SELECT COUNT(*) FROM pesquisador WHERE matricula = ? AND suspenso = FALSE";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, matricula);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao validar pesquisador: " + ex.getMessage());
        }
        return false;
    }
}
