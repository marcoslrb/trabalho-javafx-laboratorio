package javafx.laboratorio.models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.laboratorio.models.domain.Laboratorio;

/**
 * DAO (Data Access Object) para a entidade Laboratorio.
 * Segue o mesmo padrão do PesquisadorDAO já existente no projeto:
 * a conexão é injetada de fora (via setConnection).
 */
public class LaboratorioDAO {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // =========================================================
    // INSERIR um novo laboratório
    // =========================================================
    /**
     * Insere um laboratório no banco. O 'id' é gerado automaticamente (SERIAL).
     * @return true se inseriu com sucesso, false caso contrário.
     */
    public boolean inserir(Laboratorio laboratorio) {
        String sql = "INSERT INTO laboratorio (nome, area, descricao, funcional) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, laboratorio.getNome());
            stmt.setString(2, laboratorio.getArea());
            stmt.setString(3, laboratorio.getDescricao());
            stmt.setBoolean(4, laboratorio.isFuncional());
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            System.err.println("Erro ao inserir laboratório: " + ex.getMessage());
            return false;
        }
    }

    // =========================================================
    // LISTAR todos os laboratórios
    // =========================================================
    /**
     * Retorna a lista completa de laboratórios do banco.
     */
    public List<Laboratorio> listar() {
        String sql = "SELECT * FROM laboratorio ORDER BY nome";
        List<Laboratorio> retorno = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultado = stmt.executeQuery();
            while (resultado.next()) {
                Laboratorio lab = new Laboratorio();
                lab.setId(resultado.getInt("id"));
                lab.setNome(resultado.getString("nome"));
                lab.setArea(resultado.getString("area"));
                lab.setDescricao(resultado.getString("descricao"));
                lab.setFuncional(resultado.getBoolean("funcional"));
                retorno.add(lab);
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao listar laboratórios: " + ex.getMessage());
        }
        return retorno;
    }

    // =========================================================
    // BUSCAR laboratório por ID
    // =========================================================
    /**
     * Busca um laboratório específico pelo seu ID.
     * @return o Laboratorio encontrado, ou null se não existir.
     */
    public Laboratorio buscarPorId(int id) {
        String sql = "SELECT * FROM laboratorio WHERE id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                Laboratorio lab = new Laboratorio();
                lab.setId(resultado.getInt("id"));
                lab.setNome(resultado.getString("nome"));
                lab.setArea(resultado.getString("area"));
                lab.setDescricao(resultado.getString("descricao"));
                lab.setFuncional(resultado.getBoolean("funcional"));
                return lab;
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao buscar laboratório por ID: " + ex.getMessage());
        }
        return null;
    }

    // =========================================================
    // ALTERAR um laboratório existente
    // =========================================================
    /**
     * Atualiza os dados de um laboratório. A chave de busca é o 'id'.
     * @return true se atualizou ao menos 1 linha, false caso contrário.
     */
    public boolean alterar(Laboratorio laboratorio) {
        String sql = "UPDATE laboratorio SET nome=?, area=?, descricao=?, funcional=? WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, laboratorio.getNome());
            stmt.setString(2, laboratorio.getArea());
            stmt.setString(3, laboratorio.getDescricao());
            stmt.setBoolean(4, laboratorio.isFuncional());
            stmt.setInt(5, laboratorio.getId());
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException ex) {
            System.err.println("Erro ao alterar laboratório: " + ex.getMessage());
            return false;
        }
    }

    // =========================================================
    // REMOVER um laboratório
    // =========================================================
    /**
     * Remove um laboratório pelo seu ID.
     * Atenção: falhará se houver RESERVAS vinculadas (restrição ON DELETE RESTRICT no banco).
     * @return true se removeu, false caso contrário.
     */
    public boolean remover(Laboratorio laboratorio) {
        String sql = "DELETE FROM laboratorio WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, laboratorio.getId());
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            System.err.println("Erro ao remover laboratório: " + ex.getMessage());
            return false;
        }
    }

    // =========================================================
    // MARCAR LABORATÓRIO COMO NÃO FUNCIONAL (para Pedido de Manutenção)
    // =========================================================
    /**
     * Regra de negócio: após um pedido de manutenção ser registrado,
     * o laboratório deve ser marcado como não funcional (funcional = FALSE).
     * Este método encapsula essa atualização específica.
     *
     * SQL equivalente: UPDATE laboratorio SET funcional = FALSE WHERE id = ?
     *
     * @param idLaboratorio o ID do laboratório a ser marcado como não funcional.
     * @return true se atualizou com sucesso.
     */
    public boolean marcarComoNaoFuncional(int idLaboratorio) {
        String sql = "UPDATE laboratorio SET funcional = FALSE WHERE id = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idLaboratorio);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException ex) {
            System.err.println("Erro ao marcar laboratório como não funcional: " + ex.getMessage());
            return false;
        }
    }
}
