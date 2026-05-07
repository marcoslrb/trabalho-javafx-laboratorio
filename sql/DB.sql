---
-- Estrutura de Banco de Dados: Gestão de Laboratórios
-- Projeto: trabalho-javafx-laboratorio
---

-- 1. Tabela PESQUISADOR
-- O campo 'suspenso' substitui a ideia de 'ativo', onde TRUE significa que o pesquisador está impedido.
CREATE TABLE pesquisador (
    matricula VARCHAR(8) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    telefone VARCHAR(11) UNIQUE,
    suspenso BOOLEAN NOT NULL DEFAULT FALSE
);

-- 2. Tabela LABORATORIO
-- 'funcional' indica se o laboratório pode receber reservas (muda para FALSE em manutenções).
CREATE TABLE laboratorio (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    area VARCHAR(50) NOT NULL,
    descricao VARCHAR(300),
    funcional BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. Tabela RESERVA
-- Armazena o vínculo entre pesquisador e laboratório com data e hora.
CREATE TABLE reserva (
    id SERIAL PRIMARY KEY,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NOT NULL,
    matricula_pesquisador VARCHAR(8) NOT NULL,
    id_laboratorio INT NOT NULL,
    
    -- Restrição: Impede deletar pesquisador/lab se houver reserva vinculada
    CONSTRAINT fk_reserva_pesquisador FOREIGN KEY (matricula_pesquisador) 
        REFERENCES pesquisador(matricula) ON DELETE RESTRICT,
    CONSTRAINT fk_reserva_laboratorio FOREIGN KEY (id_laboratorio) 
        REFERENCES laboratorio(id) ON DELETE RESTRICT,
        
    -- Verificação básica: data de fim não pode ser anterior ao início
    CONSTRAINT chk_datas CHECK (data_fim > data_inicio)
);

-- 4. Tabela PEDIDO_MANUTENCAO
-- Vinculado obrigatoriamente a uma reserva de origem conforme o diferencial do projeto.
CREATE TABLE pedido_manutencao (
    id SERIAL PRIMARY KEY,
    id_reserva INT NOT NULL,
    hora_pedido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descricao VARCHAR(500) NOT NULL,
    status_resolvido BOOLEAN NOT NULL DEFAULT FALSE,
    
    CONSTRAINT fk_manutencao_reserva FOREIGN KEY (id_reserva) 
        REFERENCES reserva(id) ON DELETE CASCADE
);

---
-- Fim do Script
---
