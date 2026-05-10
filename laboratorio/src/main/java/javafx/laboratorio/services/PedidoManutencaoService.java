package javafx.laboratorio.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javafx.laboratorio.models.dao.LaboratorioDAO;
import javafx.laboratorio.models.dao.PedidoManutencaoDAO;
import javafx.laboratorio.models.dao.PesquisadorValidacaoDAO;
import javafx.laboratorio.models.database.Database;
import javafx.laboratorio.models.database.DatabaseFactory;
import javafx.laboratorio.models.domain.PedidoManutencao;
import javafx.laboratorio.models.domain.Pesquisador;
import javafx.laboratorio.models.domain.Reserva;

/**
 * Service da camada de negócio para Pedido de Manutenção.
 *
 * Regras implementadas:
 *   RN1 - Pesquisador deve existir e não estar suspenso
 *   RN2 - Não pode haver pedido pendente do mesmo pesquisador no mesmo laboratório
 *   RN3 - Só pode abrir pedido se usou o laboratório nos últimos 5 dias (reserva)
 *   RN4 - Ao inserir pedido com sucesso, marcar laboratório como não funcional
 *          (INSERT + UPDATE executados dentro de uma única transação JDBC)
 */
public class PedidoManutencaoService {

    private final PesquisadorValidacaoDAO pesquisadorValidacaoDAO = new PesquisadorValidacaoDAO();
    private final PedidoManutencaoDAO pedidoManutencaoDAO = new PedidoManutencaoDAO();
    private final LaboratorioDAO laboratorioDAO = new LaboratorioDAO();

    // =========================================================
    // REGISTRAR — método principal com transação JDBC real
    // =========================================================
    /**
     * Registra um novo pedido de manutenção executando as 4 regras de negócio.
     *
     * As operações de banco realizadas neste método (em ordem):
     *   Op.1 — SELECT COUNT(*) em PESQUISADOR (validar matrícula e suspenso)        [RN1]
     *   Op.2 — SELECT * em PESQUISADOR (buscar dados completos do pesquisador)       [RN1]
     *   Op.3 — SELECT com JOIN em RESERVA (buscar reserva válida nos últimos 5 dias) [RN3]
     *   Op.4 — SELECT COUNT(*) com JOIN em PEDIDO_MANUTENCAO (verificar duplicidade) [RN2]
     *   Op.5a — INSERT INTO pedido_manutencao (registrar o pedido)                  [RN4 — ESCRITA]
     *   Op.5b — UPDATE laboratorio SET funcional = FALSE (desativar laboratório)     [RN4 — ESCRITA]
     *
     * As operações 5a e 5b são agrupadas em uma TRANSAÇÃO JDBC:
     *   - connection.setAutoCommit(false) antes das escritas
     *   - connection.commit() apenas se AMBAS tiverem êxito
     *   - connection.rollback() se qualquer uma falhar, garantindo atomicidade
     *
     * @return String de resultado: começa com "SUCESSO" ou "ERRO_RN*" / "ERRO"
     */
    public String registrarPedido(String matricula, int idLaboratorio, String descricao) {

        Database database = DatabaseFactory.getDatabase("postgresql");
        Connection connection = database.conectar();
        if (connection == null) {
            throw new RuntimeException("Não foi possível conectar ao banco de dados. Verifique as configurações em DatabasePostgreSQL.java.");
        }

        pesquisadorValidacaoDAO.setConnection(connection);
        pedidoManutencaoDAO.setConnection(connection);
        laboratorioDAO.setConnection(connection);

        try {
            // --------------------------------------------------
            // Op.1 — RN1: Validar pesquisador (conta e verifica suspenso)
            // SQL: SELECT COUNT(*) FROM pesquisador WHERE matricula=? AND suspenso=FALSE
            // --------------------------------------------------
            if (!pesquisadorValidacaoDAO.isPesquisadorValido(matricula)) {
                return "ERRO_RN1: Matrícula '" + matricula + "' não encontrada ou pesquisador suspenso.";
            }

            // --------------------------------------------------
            // Op.2 — RN1: Buscar dados completos do pesquisador
            // SQL: SELECT * FROM pesquisador WHERE matricula = ?
            // --------------------------------------------------
            Pesquisador pesquisador = pesquisadorValidacaoDAO.buscarPorMatricula(matricula);

            // --------------------------------------------------
            // Op.3 — RN3: Buscar reserva válida nos últimos 5 dias
            // SQL: SELECT ... FROM reserva JOIN ... WHERE matricula=? AND id_lab=?
            //      AND data_fim >= NOW() - INTERVAL '5 days' AND data_fim <= NOW()
            // --------------------------------------------------
            Reserva reservaValida = pedidoManutencaoDAO.buscarReservaValida(matricula, idLaboratorio);
            if (reservaValida == null) {
                return "ERRO_RN3: Não foi encontrada nenhuma reserva deste pesquisador "
                     + "neste laboratório nos últimos 5 dias. "
                     + "O pedido só pode ser aberto até 5 dias após o uso.";
            }

            // --------------------------------------------------
            // Op.4 — RN2: Verificar duplicidade de pedido pendente
            // SQL: SELECT COUNT(*) FROM pedido_manutencao pm JOIN reserva r ...
            //      WHERE matricula=? AND id_lab=? AND status_resolvido=FALSE
            // --------------------------------------------------
            if (pedidoManutencaoDAO.existePedidoPendente(matricula, idLaboratorio)) {
                return "ERRO_RN2: Já existe um pedido PENDENTE deste pesquisador "
                     + "para este laboratório. Aguarde a resolução antes de abrir um novo.";
            }

            // --------------------------------------------------
            // Op.5a + Op.5b — RN4: INSERT + UPDATE dentro de TRANSAÇÃO
            // --------------------------------------------------
            PedidoManutencao pedido = new PedidoManutencao();
            pedido.setReserva(reservaValida);
            pedido.setLaboratorio(reservaValida.getLaboratorio());
            pedido.setDescricao(descricao);
            pedido.setStatusResolvido(false);

            // Inicia a transação: desativa o auto-commit
            connection.setAutoCommit(false);

            try {
                // Op.5a — INSERT INTO pedido_manutencao (id_reserva, hora_pedido, descricao, status_resolvido)
                boolean inserido = pedidoManutencaoDAO.inserir(pedido);
                if (!inserido) {
                    connection.rollback();   // desfaz qualquer efeito parcial
                    return "ERRO: Falha ao inserir o pedido de manutenção. Transação cancelada.";
                }

                // Op.5b — UPDATE laboratorio SET funcional = FALSE WHERE id = ?
                boolean marcado = laboratorioDAO.marcarComoNaoFuncional(idLaboratorio);
                if (!marcado) {
                    connection.rollback();   // garante que o INSERT também seja desfeito
                    return "ERRO: Falha ao atualizar o laboratório. Transação cancelada (pedido NÃO registrado).";
                }

                // Ambas as operações deram certo: confirma a transação
                connection.commit();
                return "SUCESSO: Pedido registrado e laboratório marcado como não funcional.";

            } catch (SQLException ex) {
                // Erro inesperado durante a transação: desfaz tudo
                try { connection.rollback(); } catch (SQLException ignore) {}
                System.err.println("Erro na transação de registro: " + ex.getMessage());
                return "ERRO: Exceção durante a transação — " + ex.getMessage();

            } finally {
                // Restaura o auto-commit independente do resultado
                try { connection.setAutoCommit(true); } catch (SQLException ignore) {}
            }

        } catch (SQLException ex) {
            System.err.println("Erro ao iniciar transação: " + ex.getMessage());
            return "ERRO: Não foi possível iniciar a transação — " + ex.getMessage();

        } finally {
            database.desconectar(connection);
        }
    }

    // =========================================================
    // LISTAR todos os pedidos
    // =========================================================
    public List<PedidoManutencao> listarTodos() {
        Database database = DatabaseFactory.getDatabase("postgresql");
        Connection connection = database.conectar();
        if (connection == null) {
            throw new RuntimeException("Não foi possível conectar ao banco de dados. Verifique as configurações em DatabasePostgreSQL.java.");
        }
        pedidoManutencaoDAO.setConnection(connection);
        try {
            return pedidoManutencaoDAO.listar();
        } finally {
            database.desconectar(connection);
        }
    }

    // =========================================================
    // ALTERAR descrição e status de um pedido (edição simples)
    // =========================================================
    /**
     * Edita a descrição e o status_resolvido de um pedido existente.
     * Validações: descrição não pode ser vazia nem ultrapassar 500 caracteres.
     *
     * @return mensagem de resultado ("SUCESSO" ou "ERRO: ...")
     */
    public String alterar(PedidoManutencao pedido) {
        if (pedido.getDescricao() == null || pedido.getDescricao().trim().isEmpty()) {
            return "ERRO: A descrição não pode ser vazia.";
        }
        if (pedido.getDescricao().length() > 500) {
            return "ERRO: A descrição deve ter no máximo 500 caracteres.";
        }

        Database database = DatabaseFactory.getDatabase("postgresql");
        Connection connection = database.conectar();
        if (connection == null) {
            return "ERRO: Não foi possível conectar ao banco de dados.";
        }
        pedidoManutencaoDAO.setConnection(connection);
        try {
            PedidoManutencao existente = pedidoManutencaoDAO.buscarPorId(pedido.getId());
            if (existente == null) {
                return "ERRO: Pedido não encontrado.";
            }

            boolean ok = pedidoManutencaoDAO.alterar(pedido);
            return ok ? "SUCESSO" : "ERRO: Nenhum registro atualizado.";
        } finally {
            database.desconectar(connection);
        }
    }

    // =========================================================
    // REMOVER um pedido
    // =========================================================
    public String remover(int idPedido) {
        Database database = DatabaseFactory.getDatabase("postgresql");
        Connection connection = database.conectar();
        if (connection == null) {
            return "ERRO: Não foi possível conectar ao banco de dados.";
        }
        pedidoManutencaoDAO.setConnection(connection);
        try {
            PedidoManutencao existente = pedidoManutencaoDAO.buscarPorId(idPedido);
            if (existente == null) {
                return "ERRO: Pedido não encontrado.";
            }

            boolean ok = pedidoManutencaoDAO.remover(idPedido);
            return ok ? "SUCESSO" : "ERRO: Não foi possível remover o pedido.";
        } finally {
            database.desconectar(connection);
        }
    }

    // =========================================================
    // MARCAR como resolvido (atalho rápido — botão na tabela)
    // =========================================================
    public String marcarComoResolvido(int idPedido) {
        Database database = DatabaseFactory.getDatabase("postgresql");
        Connection connection = database.conectar();
        if (connection == null) {
            return "ERRO: Não foi possível conectar ao banco de dados.";
        }
        pedidoManutencaoDAO.setConnection(connection);
        try {
            PedidoManutencao pedido = pedidoManutencaoDAO.buscarPorId(idPedido);
            if (pedido == null) {
                return "ERRO: Pedido não encontrado.";
            }
            if (pedido.isStatusResolvido()) {
                return "ERRO: O pedido já está resolvido.";
            }
 
            return pedidoManutencaoDAO.marcarComoResolvido(idPedido) ? "SUCESSO" : "ERRO: Não foi possível atualizar o pedido.";
        } finally {
            database.desconectar(connection);
        }
    }

    // =========================================================
    // VALIDAR pesquisador (feedback imediato na tela)
    // =========================================================
    public Pesquisador validarEBuscarPesquisador(String matricula) {
        Database database = DatabaseFactory.getDatabase("postgresql");
        Connection connection = database.conectar();
        pesquisadorValidacaoDAO.setConnection(connection);
        try {
            if (pesquisadorValidacaoDAO.isPesquisadorValido(matricula)) {
                return pesquisadorValidacaoDAO.buscarPorMatricula(matricula);
            }
            return null;
        } finally {
            database.desconectar(connection);
        }
    }

    // =========================================================
    // BUSCAR reserva válida (pré-visualização na tela — Passo 2)
    // =========================================================
    public Reserva buscarReservaValida(String matricula, int idLaboratorio) {
        Database database = DatabaseFactory.getDatabase("postgresql");
        Connection connection = database.conectar();
        pedidoManutencaoDAO.setConnection(connection);
        try {
            return pedidoManutencaoDAO.buscarReservaValida(matricula, idLaboratorio);
        } finally {
            database.desconectar(connection);
        }
    }
}
