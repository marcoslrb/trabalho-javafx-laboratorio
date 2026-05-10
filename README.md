# trabalho-javafx-laboratorio

Projeto acadêmico desenvolvido em **JavaFX** com arquitetura **MVC**, persistência com **DAO**, conexão com banco via **Factory** e banco de dados **PostgreSQL**.

O sistema foi pensado para o gerenciamento de uso de laboratórios de pesquisa, incluindo cadastros, reservas, pedidos de manutenção, relatório e gráfico.

---

## Tecnologias utilizadas

- Java
- JavaFX
- FXML
- JDBC
- PostgreSQL

---

## Arquitetura do projeto

O projeto segue os padrões solicitados no trabalho:

- **MVC (Model-View-Controller)** para organização da aplicação
- **DAO (Data Access Object)** para acesso ao banco de dados
- **Factory** para criação da conexão com o banco

### Organização em camadas

- `models/domain` → classes de domínio
- `models/dao` → acesso a dados
- `controllers` → controladores JavaFX
- `views` → telas FXML
- `services` → regras de negócio
- `database` → conexão com o banco

---

## Entidades

- **Pesquisador**
- **Laboratório**

## Processos

- **Reserva**
- **Pedido de Manutenção**

--

## Regras de negócio

### Reserva

- Reserva somente pode ser feita por um pesquisador cadastrado
- Reserva somente pode ser feita se houver disponibilidade do laboratório
- Um mesmo pesquisador não pode fazer mais do que 5 reservas, na mesma semana, em um laboratório específico
- **A data e hora de término da reserva deve ser obrigatoriamente posterior à data e hora de início.**

### Pedido de Manutenção

- O pedido só pode ser registrado após validação da matrícula de um pesquisador válido
- O sistema verifica se existe reserva do pesquisador para o laboratório nos últimos 5 dias
- O sistema verifica se já existe pedido de manutenção pendente do mesmo pesquisador para o mesmo laboratório
- Ao registrar um pedido com sucesso, o laboratório é atualizado para `funcional = false` **através de uma transação atômica (tudo ou nada)**.

### Cadastros e Integridade de Dados

- **Unicidade:** O CPF e o E-mail do pesquisador devem ser únicos no sistema.
- **Estado Padrão:** Todo laboratório cadastrado inicia com o status `funcional` como verdadeiro por padrão.
- **Regras de Exclusão (Integridade Referencial):**
  - Não é possível excluir um **Pesquisador** se ele possuir reservas associadas.
  - Não é possível excluir um **Laboratório** se ele possuir reservas associadas.
  - Se uma **Reserva** for excluída, o **Pedido de Manutenção** associado a ela é excluído automaticamente (em cascata).

### Validações de Interface (Tempo Real)

Para melhorar a experiência do usuário e prevenir erros, todos os formulários possuem validações visuais e de bloqueio de digitação:
- **TextFormatter:** Impede fisicamente a digitação de dados inválidos (ex: letras no CPF/Telefone) e bloqueia quando o limite de caracteres é atingido.
- **PromptText e Tooltip:** Fornecem dicas visuais de preenchimento (ex: `HH:mm` para horas, limites máximos de tamanho) antes do usuário digitar.

---

---

## Operações de banco no registro do pedido

Durante o processo de inserção do pedido de manutenção, o sistema realiza as seguintes operações no banco:

1. validar se o pesquisador existe e está ativo
2. buscar os dados do pesquisador
3. verificar se existe reserva válida nos últimos 5 dias
4. verificar se já existe pedido pendente para o mesmo pesquisador e laboratório
5. inserir o pedido de manutenção
6. atualizar o status do laboratório para não funcional

---

## Script SQL

O arquivo `sql/DB.sql` contém:

- criação das tabelas do sistema
- relacionamentos entre as tabelas
- inserção de pelo menos 2 registros por tabela
- consultas SQL utilizadas como apoio no desenvolvimento

---

## % Relatórios (Jasper Reports)

### Relatório 1: Extrato de Ocupação por Pesquisador

**Objetivo:** consolidar as reservas de um pesquisador para comprovação de horas de pesquisa.

**Campos:**

- Data Início
- Data Fim
- Nome do Laboratório
- ID da Reserva

**SQL Base:**

```sql
SELECT 
    r.data_inicio, 
    r.data_fim, 
    l.nome AS nome_laboratorio,
    COUNT(r.id) OVER() AS total_registros
FROM RESERVA r
INNER JOIN LABORATORIO l ON r.id_laboratorio = l.id
WHERE r.matricula_pesquisador = $P{p_matricula};
```

### Relatório 2: Log de Incidentes e Impacto em Reservas

**Objetivo:** listar os problemas relatados e quem identificou o problema através de uma reserva.

**Campos:**

- Data do Pedido
- Nome do Pesquisador
- Nome do Laboratório
- Descrição do Defeito

**SQL Base:**

```sql
SELECT 
    pm.hora_pedido, 
    p.nome AS pesquisador, 
    l.nome AS laboratorio, 
    pm.descricao
FROM PEDIDO_MANUTENCAO pm
JOIN RESERVA r ON pm.id_reserva = r.id
JOIN PESQUISADOR p ON r.matricula_pesquisador = p.matricula
JOIN LABORATORIO l ON r.id_laboratorio = l.id;
```

---

## Gráfico

### Índice de Confiabilidade dos Laboratórios

Gráfico de barras em JavaFX para exibir a quantidade de pedidos de manutenção por laboratório.

**Objetivo:** identificar laboratórios com maior incidência de falhas.

**SQL Base:**

```sql
SELECT l.nome, COUNT(pm.id) AS total_falhas
FROM LABORATORIO l
INNER JOIN PEDIDO_MANUTENCAO pm ON l.id = pm.id_laboratorio
WHERE l.funcional = FALSE
GROUP BY l.nome;
```

---

## Estrutura do banco de dados

### PESQUISADOR

- `matricula` (PK, VARCHAR(8))
- `nome` (VARCHAR(100), NOT NULL)
- `email` (VARCHAR(50), NOT NULL, UNIQUE)
- `cpf` (VARCHAR(11), NOT NULL, UNIQUE)
- `telefone` (VARCHAR(11), UNIQUE, NULL)
- `suspenso` (BOOL DEFAULT FALSE, NOT NULL)

### LABORATORIO

- `id` (PK, INT)
- `nome` (VARCHAR(100), NOT NULL)
- `area` (VARCHAR(50), NOT NULL)
- `descricao` (VARCHAR(300), NULL)
- `funcional` (BOOL, NOT NULL)

### RESERVA

- `id` (PK, INT)
- `data_inicio` (TIMESTAMP, NOT NULL)
- `data_fim` (TIMESTAMP, NOT NULL)
- `matricula_pesquisador` (FK)
- `id_laboratorio` (FK)

### PEDIDO_MANUTENCAO

- `id` (PK, INT)
- `id_reserva` (FK)
- `id_laboratorio` (FK)
- `hora_pedido` (TIMESTAMP, NOT NULL)
- `descricao` (VARCHAR(500), NOT NULL)
- `status_resolvido` (BOOL, NOT NULL)

---

## Telas do Sistema

### Tela de Reserva

- Aplicação das três regras de negócio;

- Lista de reservas atuais;
- Seleção de laboratórios via ComboBox.

### Tela de Pedido de Manutenção

- **Validar Pesquisador:** Consulta na tabela PESQUISADOR.

- **Validar Reserva:** Verifica se existe reserva (matrícula + lab) nos últimos 5 dias.
- **Verificar Duplicidade:** Garante que não haja chamado aberto idêntico pendente.
- **Inserir Pedido:** Registro na tabela PEDIDO_MANUTENCAO.
- **Atualizar Status:** Altera `LABORATORIO.funcional` para `FALSE`.
