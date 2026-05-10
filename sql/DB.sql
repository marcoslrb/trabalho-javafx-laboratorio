---
-- Estrutura de Banco de Dados: Gestão de Laboratórios
-- Projeto: trabalho-javafx-laboratorio
---

-- ===========================================================
-- 1. PESQUISADOR
-- ===========================================================
CREATE TABLE pesquisador (
    matricula VARCHAR(8) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    telefone VARCHAR(11) UNIQUE,
    suspenso BOOLEAN NOT NULL DEFAULT FALSE
);

-- ===========================================================
-- 2. LABORATORIO
-- 'funcional' muda para FALSE quando um pedido de manutenção é aberto.
-- ===========================================================
CREATE TABLE laboratorio (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    area VARCHAR(50) NOT NULL,
    descricao VARCHAR(300),
    funcional BOOLEAN NOT NULL DEFAULT TRUE
);

-- ===========================================================
-- 3. RESERVA
-- ===========================================================
CREATE TABLE reserva (
    id SERIAL PRIMARY KEY,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NOT NULL,
    matricula_pesquisador VARCHAR(8) NOT NULL,
    id_laboratorio INT NOT NULL,

    CONSTRAINT fk_reserva_pesquisador FOREIGN KEY (matricula_pesquisador)
        REFERENCES pesquisador(matricula) ON DELETE RESTRICT,
    CONSTRAINT fk_reserva_laboratorio FOREIGN KEY (id_laboratorio)
        REFERENCES laboratorio(id) ON DELETE RESTRICT,
    CONSTRAINT chk_datas CHECK (data_fim > data_inicio)
);

-- ===========================================================
-- 4. PEDIDO_MANUTENCAO
-- ===========================================================
CREATE TABLE pedido_manutencao (
    id SERIAL PRIMARY KEY,
    id_reserva INT NOT NULL,
    hora_pedido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descricao VARCHAR(500) NOT NULL,
    status_resolvido BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_manutencao_reserva FOREIGN KEY (id_reserva)
        REFERENCES reserva(id) ON DELETE CASCADE
);

-- ===========================================================
-- DADOS DE EXEMPLO (mínimo 2 registros por tabela)
-- ===========================================================

-- 2 pesquisadores ativos
INSERT INTO pesquisador (matricula, nome, email, cpf, telefone, suspenso) VALUES
    ('20241001', 'Ana Souza',  'ana.souza@univ.br',  '12345678901', '11999990001', FALSE),
    ('20241002', 'Bruno Lima', 'bruno.lima@univ.br', '98765432100', '11999990002', FALSE);

-- 2 laboratórios (ambos começam como funcionais)
INSERT INTO laboratorio (nome, area, descricao, funcional) VALUES
    ('Lab. Informática A', 'Computação', 'Laboratório com 30 computadores para aulas práticas.', TRUE),
    ('Lab. Redes B',       'Redes',      'Infraestrutura de rede para experimentos.',            TRUE);

-- 2 reservas — data_fim no passado recente para viabilizar pedidos de manutenção
-- Reserva 1: Ana no Lab A, terminou há 2 dias (dentro da janela de 5 dias)
-- Reserva 2: Bruno no Lab B, terminou há 1 dia (dentro da janela de 5 dias)
INSERT INTO reserva (data_inicio, data_fim, matricula_pesquisador, id_laboratorio) VALUES
    (NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', '20241001', 1),
    (NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day',  '20241002', 2);

-- 2 pedidos de manutenção
-- Pedido 1: Ana reporta problema no Lab A (pendente → lab marcado como não funcional)
-- Pedido 2: Bruno reporta problema no Lab B (já resolvido)
INSERT INTO pedido_manutencao (id_reserva, hora_pedido, descricao, status_resolvido) VALUES
    (1, NOW() - INTERVAL '1 day',  'Computador da posição 5 não liga. Possível problema na fonte.', FALSE),
    (2, NOW() - INTERVAL '12 hours', 'Cabo de rede danificado no rack principal. Sem conexão.', TRUE);

-- Atualiza laboratório do pedido pendente (RN4: funcional → FALSE)
UPDATE laboratorio SET funcional = FALSE WHERE id = 1;

-- ===========================================================
-- CONSULTAS SQL ESSENCIAIS (referência e documentação)
-- ===========================================================

-- Op.1 — RN1: valida pesquisador (existe e não está suspenso)
-- SELECT COUNT(*) FROM pesquisador WHERE matricula = ? AND suspenso = FALSE;

-- Op.2 — RN1: busca dados completos do pesquisador validado
-- SELECT * FROM pesquisador WHERE matricula = ?;

-- Op.3 — RN3: busca reserva válida nos últimos 5 dias
-- SELECT * FROM reserva
-- WHERE matricula_pesquisador = ? AND id_laboratorio = ?
--   AND data_fim >= NOW() - INTERVAL '5 days'
--   AND data_fim <= NOW()
-- ORDER BY data_fim DESC LIMIT 1;

-- Op.4 — RN2: verifica duplicidade (pedido pendente do mesmo pesquisador/lab)
-- SELECT COUNT(*) FROM pedido_manutencao pm
-- JOIN reserva r ON pm.id_reserva = r.id
-- WHERE r.matricula_pesquisador = ? AND r.id_laboratorio = ?
--   AND pm.status_resolvido = FALSE;

-- Op.5a — RN4 (ESCRITA 1/2): registra o pedido de manutenção
-- INSERT INTO pedido_manutencao (id_reserva, hora_pedido, descricao, status_resolvido)
-- VALUES (?, NOW(), ?, FALSE);

-- Op.5b — RN4 (ESCRITA 2/2): desativa o laboratório
-- UPDATE laboratorio SET funcional = FALSE WHERE id = ?;

-- Nota: Op.5a e Op.5b são executadas dentro de uma transação JDBC:
--   connection.setAutoCommit(false);
--   -- executa insert
--   -- executa update
--   connection.commit();   -- confirma ambas se tudo deu certo
--   -- ou connection.rollback() se qualquer uma falhar

-- Relatório 1: Extrato de Ocupação por Pesquisador
-- SELECT r.data_inicio, r.data_fim, l.nome AS nome_laboratorio, COUNT(r.id) OVER() AS total_registros
-- FROM reserva r JOIN laboratorio l ON r.id_laboratorio = l.id
-- WHERE r.matricula_pesquisador = $P{p_matricula};

-- Relatório 2: Log de Incidentes
-- SELECT pm.hora_pedido, p.nome AS pesquisador, l.nome AS laboratorio, pm.descricao
-- FROM pedido_manutencao pm
-- JOIN reserva r ON pm.id_reserva = r.id
-- JOIN pesquisador p ON r.matricula_pesquisador = p.matricula
-- JOIN laboratorio l ON r.id_laboratorio = l.id;

-- Gráfico: Índice de Confiabilidade (laboratórios x total de falhas)
-- SELECT l.nome, COUNT(pm.id) AS total_falhas
-- FROM laboratorio l
-- JOIN reserva r ON r.id_laboratorio = l.id
-- JOIN pedido_manutencao pm ON pm.id_reserva = r.id
-- WHERE pm.status_resolvido = FALSE
-- GROUP BY l.nome
-- ORDER BY total_falhas DESC;
