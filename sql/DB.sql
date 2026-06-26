---
-- ===========================================================
-- PROJETO: GESTÃO DE LABORATÓRIOS (JavaFX)
-- SCRIPT COMPLETO DE PREPARAÇÃO PARA APRESENTAÇÃO
-- ===========================================================
---

-- ===========================================================
-- ZERANDO O BANCO (Garante uma execução limpa antes de apresentar)
-- ===========================================================
DROP TABLE IF EXISTS pedido_manutencao CASCADE;
DROP TABLE IF EXISTS reserva CASCADE;
DROP TABLE IF EXISTS laboratorio CASCADE;
DROP TABLE IF EXISTS pesquisador CASCADE;

-- ===========================================================
-- 1. ESTRUTURA DE TABELAS (DDL)
-- ===========================================================

CREATE TABLE pesquisador (
    matricula VARCHAR(8) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    telefone VARCHAR(11) UNIQUE,
    suspenso BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE laboratorio (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    area VARCHAR(50) NOT NULL,
    descricao VARCHAR(300),
    funcional BOOLEAN NOT NULL DEFAULT TRUE
);

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
-- 2. CARGA DE DADOS RICA (Para Gráficos e Relatórios)
-- ===========================================================

-- PESQUISADORES
INSERT INTO pesquisador (matricula, nome, email, cpf, telefone, suspenso) VALUES
    ('20241001', 'Ana Souza',    'ana@univ.br',    '11111111111', '11900000001', FALSE),
    ('20241002', 'Bruno Lima',   'bruno@univ.br',  '22222222222', '11900000002', FALSE),
    ('20241003', 'Carlos Silva', 'carlos@univ.br', '33333333333', '11900000003', FALSE),
    ('20241004', 'Diana Rocha',  'diana@univ.br',  '44444444444', '11900000004', FALSE);

-- LABORATÓRIOS
INSERT INTO laboratorio (id, nome, area, descricao, funcional) VALUES
    (1, 'Lab. Informática A', 'Computação',   '30 computadores modernos.', TRUE),
    (2, 'Lab. Redes B',       'Redes',        'Rack principal e switches Cisco.', TRUE),
    (3, 'Lab. Química C',     'Química',      'Bancadas com exaustores industriais.', TRUE),
    (4, 'Lab. Maker D',       'Prototipagem', 'Impressoras 3D e corte a laser.', TRUE);

SELECT setval('laboratorio_id_seq', (SELECT MAX(id) FROM laboratorio));

-- RESERVAS (Mistura de histórico para relatórios + bases para a apresentação)
INSERT INTO reserva (id, data_inicio, data_fim, matricula_pesquisador, id_laboratorio) VALUES
    -- Histórico antigo (Popula o Extrato de Ocupação e Gráfico de Ocupação)
    (1, NOW() - INTERVAL '20 days', NOW() - INTERVAL '19 days', '20241001', 1),
    (2, NOW() - INTERVAL '18 days', NOW() - INTERVAL '17 days', '20241002', 2),
    (3, NOW() - INTERVAL '15 days', NOW() - INTERVAL '14 days', '20241004', 3),
    (4, NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days',  '20241001', 4),

    -- Reservas Recentes (Permitem abertura de chamado de manutenção válido)
    (5, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', '20241001', 1), -- Ana no Lab A
    (6, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', '20241002', 2), -- Bruno no Lab B
    (7, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 day', '20241004', 1), -- Diana no Lab A

    -- [ARMADILHA] REGRA 1: Choque de Horários (Tente reservar o Lab B neste horário com outro pesquisador)
    (8, NOW() + INTERVAL '1 day', NOW() + INTERVAL '1 day 2 hours', '20241004', 2),

    -- [ARMADILHA] REGRA 2: Máx 5 por semana (5 reservas do Carlos no Lab A. Tente fazer a 6ª)
    (9, NOW() - INTERVAL '1 hour', NOW(), '20241003', 1),
    (10, NOW() - INTERVAL '3 hours', NOW() - INTERVAL '2 hours', '20241003', 1),
    (11, NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 hours', '20241003', 1),
    (12, NOW() - INTERVAL '7 hours', NOW() - INTERVAL '6 hours', '20241003', 1),
    (13, NOW() - INTERVAL '9 hours', NOW() - INTERVAL '8 hours', '20241003', 1),

    -- [ARMADILHA] REGRA 3: Manutenção de máx 5 dias atrás (Reserva velha do Carlos. Tente abrir chamado nela)
    (14, NOW() - INTERVAL '15 days', NOW() - INTERVAL '14 days', '20241003', 3);

SELECT setval('reserva_id_seq', (SELECT MAX(id) FROM reserva));

-- MANUTENÇÕES (Log de Incidentes + Índice de Confiabilidade)
INSERT INTO pedido_manutencao (id_reserva, hora_pedido, descricao, status_resolvido) VALUES
    -- Incidentes Antigos Resolvidos (Enchem o relatório de Log, não afetam pendentes)
    (1, NOW() - INTERVAL '19 days', 'Tela azul no PC do professor.', TRUE),
    (2, NOW() - INTERVAL '17 days', 'Cabo de fibra ótica rompido.', TRUE),
    (3, NOW() - INTERVAL '14 days', 'Exaustor central travado.', TRUE),

    -- Incidentes Pendentes (Formam o gráfico de confiabilidade e acionam a REGRA 4)
    (5, NOW() - INTERVAL '12 hours', 'Computador da posição 5 não liga (Ana).', FALSE),
    (6, NOW() - INTERVAL '10 hours', 'Switch rack 2 sem energia (Bruno).', FALSE),
    (7, NOW() - INTERVAL '5 hours',  'Ar condicionado falhou de novo (Diana).', FALSE);

-- Desativa os laboratórios que possuem manutenções abertas
UPDATE laboratorio SET funcional = FALSE WHERE id IN (1, 2);


-- ===========================================================
-- 3. CONSULTAS PARA O JAVA (Seu DAO deve executar estas strings)
-- ===========================================================

/*
--- GRÁFICO 1: ÍNDICE DE CONFIABILIDADE (Falhas Pendentes por Lab) ---
-- O uso do LEFT JOIN garante que Labs sem problemas (como o Maker D) apareçam com valor 0.
SELECT 
    l.nome, 
    COUNT(pm.id) AS falhas_pendentes
FROM laboratorio l
LEFT JOIN reserva r ON r.id_laboratorio = l.id
LEFT JOIN pedido_manutencao pm ON pm.id_reserva = r.id AND pm.status_resolvido = FALSE
GROUP BY l.nome
ORDER BY falhas_pendentes DESC, l.nome ASC;

--- GRÁFICO 2: OCUPAÇÃO GERAL DOS LABORATÓRIOS ---
SELECT 
    l.nome AS nome_laboratorio, 
    COUNT(r.id) AS total_reservas
FROM laboratorio l
LEFT JOIN reserva r ON r.id_laboratorio = l.id
GROUP BY l.nome
ORDER BY total_reservas DESC;

--- RELATÓRIO 1: EXTRATO DE OCUPAÇÃO POR PESQUISADOR ---
-- Passe a matrícula no lugar de '?'
SELECT 
    r.data_inicio, 
    r.data_fim, 
    l.nome AS nome_laboratorio
FROM reserva r 
JOIN laboratorio l ON r.id_laboratorio = l.id
WHERE r.matricula_pesquisador = ? 
ORDER BY r.data_inicio DESC;

--- RELATÓRIO 2: LOG DE INCIDENTES ---
SELECT 
    pm.hora_pedido, 
    p.nome AS pesquisador, 
    l.nome AS laboratorio, 
    pm.descricao,
    CASE WHEN pm.status_resolvido THEN 'Resolvido' ELSE 'Pendente' END AS status
FROM pedido_manutencao pm
JOIN reserva r ON pm.id_reserva = r.id
JOIN pesquisador p ON r.matricula_pesquisador = p.matricula
JOIN laboratorio l ON r.id_laboratorio = l.id
ORDER BY pm.hora_pedido DESC;


-- ===========================================================
-- AS 4 REGRAS DE NEGÓCIO (Validações ANTES do Insert no Java)
-- ===========================================================

-- REGRA 1 (Choque de Horários): Antes de agendar, garanta que retorne 0.
-- ? = id do lab, ? = data fim desejada, ? = data inicio desejada
SELECT COUNT(*) FROM reserva 
WHERE id_laboratorio = ? 
  AND data_inicio < ? AND data_fim > ?;

-- REGRA 2 (Máx 5 por semana): Garanta que retorne < 5
SELECT COUNT(*) FROM reserva 
WHERE matricula_pesquisador = ? 
  AND id_laboratorio = ? 
  AND data_inicio >= NOW() - INTERVAL '7 days'; 

-- REGRA 3 (Manutenção com reserva nos últimos 5 dias): Deve retornar 1 registro
SELECT * FROM reserva 
WHERE matricula_pesquisador = ? AND id_laboratorio = ? 
  AND data_fim >= NOW() - INTERVAL '5 days' AND data_fim <= NOW() 
ORDER BY data_fim DESC LIMIT 1;

-- REGRA 4 (Um pedido pendente por pesquisador no sistema): Garanta que retorne 0
SELECT COUNT(*) FROM pedido_manutencao pm
JOIN reserva r ON pm.id_reserva = r.id
WHERE r.matricula_pesquisador = ? AND pm.status_resolvido = FALSE;
*/
