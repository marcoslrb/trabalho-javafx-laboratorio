package javafx.laboratorio.services;

import java.sql.Connection;
import java.util.List;
import javafx.laboratorio.models.dao.LaboratorioDAO;
import javafx.laboratorio.models.database.Database;
import javafx.laboratorio.models.database.DatabaseFactory;
import javafx.laboratorio.models.domain.Laboratorio;

/**
 * Service da camada de negócio para Laboratório.
 *
 * Encapsula as regras e validações relacionadas ao laboratório,
 * mantendo os Controllers enxutos.
 *
 * Validações implementadas:
 *   - Nome não pode ser vazio e deve ter no máximo 100 caracteres
 *   - Área não pode ser vazia e deve ter no máximo 50 caracteres
 *   - Descrição é opcional, mas se informada, máximo 300 caracteres
 */
public class LaboratorioService {

    private final LaboratorioDAO laboratorioDAO = new LaboratorioDAO();

    // =========================================================
    // LISTAR todos os laboratórios
    // =========================================================
    public List<Laboratorio> listarTodos() {
        Database database = DatabaseFactory.getDatabase("postgresql");
        Connection connection = database.conectar();
        if (connection == null) {
            throw new RuntimeException("Não foi possível conectar ao banco de dados. Verifique as configurações em DatabasePostgreSQL.java.");
        }
        laboratorioDAO.setConnection(connection);
        try {
            return laboratorioDAO.listar();
        } finally {
            database.desconectar(connection);
        }
    }

    // =========================================================
    // INSERIR laboratório com validação
    // =========================================================
    /**
     * Valida e insere um laboratório.
     */
    public String inserir(Laboratorio laboratorio) {
        String erro = validar(laboratorio);
        if (erro != null) return erro;

        Database database = DatabaseFactory.getDatabase("postgresql");
        Connection connection = database.conectar();
        if (connection == null) {
            return "ERRO: Não foi possível conectar ao banco de dados.";
        }
        laboratorioDAO.setConnection(connection);
        try {
            boolean ok = laboratorioDAO.inserir(laboratorio);
            return ok ? "SUCESSO" : "ERRO: Falha ao inserir no banco de dados.";
        } finally {
            database.desconectar(connection);
        }
    }

    // =========================================================
    // ALTERAR laboratório com validação
    // =========================================================
    public String alterar(Laboratorio laboratorio) {
        String erro = validar(laboratorio);
        if (erro != null) return erro;

        Database database = DatabaseFactory.getDatabase("postgresql");
        Connection connection = database.conectar();
        if (connection == null) {
            return "ERRO: Não foi possível conectar ao banco de dados.";
        }
        laboratorioDAO.setConnection(connection);
        try {
            boolean ok = laboratorioDAO.alterar(laboratorio);
            return ok ? "SUCESSO" : "ERRO: Nenhum registro atualizado.";
        } finally {
            database.desconectar(connection);
        }
    }

    // =========================================================
    // REMOVER laboratório
    // =========================================================
    public boolean remover(Laboratorio laboratorio) {
        Database database = DatabaseFactory.getDatabase("postgresql");
        Connection connection = database.conectar();
        if (connection == null) {
            throw new RuntimeException("Não foi possível conectar ao banco de dados. Verifique as configurações em DatabasePostgreSQL.java.");
        }
        laboratorioDAO.setConnection(connection);
        try {
            return laboratorioDAO.remover(laboratorio);
        } finally {
            database.desconectar(connection);
        }
    }

    // =========================================================
    // VALIDAÇÕES de campos
    // =========================================================
    /**
     * Valida os campos do laboratório.
     * @return null se tudo estiver OK, ou a mensagem de erro caso contrário.
     */
    private String validar(Laboratorio lab) {
        if (lab.getNome() == null || lab.getNome().trim().isEmpty()) {
            return "ERRO: O nome do laboratório é obrigatório.";
        }
        if (lab.getNome().length() > 100) {
            return "ERRO: O nome deve ter no máximo 100 caracteres.";
        }
        if (lab.getArea() == null || lab.getArea().trim().isEmpty()) {
            return "ERRO: A área do laboratório é obrigatória.";
        }
        if (lab.getArea().length() > 50) {
            return "ERRO: A área deve ter no máximo 50 caracteres.";
        }
        if (lab.getDescricao() != null && lab.getDescricao().length() > 300) {
            return "ERRO: A descrição deve ter no máximo 300 caracteres.";
        }
        return null; // sem erros
    }
}
