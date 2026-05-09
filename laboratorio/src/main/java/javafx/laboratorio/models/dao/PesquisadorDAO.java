package javafx.laboratorio.models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.laboratorio.models.domain.Pesquisador;

public class PesquisadorDAO {

    // 1. O professor usa um atributo de conexão e getters/setters
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // 2. Método de Inserir sem abrir/fechar a conexão internamente
    public boolean inserir(Pesquisador pesquisador) {
        String sql = "INSERT INTO pesquisador (matricula, nome, email, cpf, telefone, suspenso) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, pesquisador.getMatricula());
            stmt.setString(2, pesquisador.getNome());
            stmt.setString(3, pesquisador.getEmail());
            stmt.setString(4, pesquisador.getCpf());
            stmt.setString(5, pesquisador.getTelefone());
            stmt.setBoolean(6, pesquisador.isSuspenso());
            
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            System.err.println("Erro ao inserir pesquisador: " + ex.getMessage());
            return false;
        }
    }

    // 3. Método Listar no padrão do professor
    public List<Pesquisador> listar() {
        String sql = "SELECT * FROM pesquisador";
        List<Pesquisador> retorno = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultado = stmt.executeQuery();
            while (resultado.next()) {
                Pesquisador p = new Pesquisador();
                p.setMatricula(resultado.getString("matricula"));
                p.setNome(resultado.getString("nome"));
                p.setEmail(resultado.getString("email"));
                p.setCpf(resultado.getString("cpf"));
                p.setTelefone(resultado.getString("telefone"));
                p.setSuspenso(resultado.getBoolean("suspenso"));
                retorno.add(p);
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao listar pesquisadores: " + ex.getMessage());
        }
        return retorno;
    }
    
    public boolean remover(Pesquisador pesquisador) {
    String sql = "DELETE FROM pesquisador WHERE matricula=?";
    try {
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, pesquisador.getMatricula());
        stmt.execute();
        return true;
    } catch (SQLException ex) {
        System.err.println("Erro ao remover pesquisador: " + ex.getMessage());
        return false;
    }
    }
    // Este código deve estar dentro de PesquisadorDAO.java
    public boolean alterar(Pesquisador pesquisador) {
        String sql = "UPDATE pesquisador SET nome=?, email=?, cpf=?, telefone=?, suspenso=? WHERE matricula=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, pesquisador.getNome());
            stmt.setString(2, pesquisador.getEmail());
            stmt.setString(3, pesquisador.getCpf());
            stmt.setString(4, pesquisador.getTelefone());
            stmt.setBoolean(5, pesquisador.isSuspenso());
            stmt.setString(6, pesquisador.getMatricula()); 

            int linhasAfetadas = stmt.executeUpdate(); 
            return linhasAfetadas > 0;
        } catch (SQLException ex) {
            System.err.println("Erro no SQL ao alterar pesquisador: " + ex.getMessage());
            return false;
        }
    }
}