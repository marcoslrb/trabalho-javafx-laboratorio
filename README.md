# trabalho-javafx-laboratorio

## Entidades

- Pesquisador
- Laboratório

## Processos

- Reserva
- Pedido de Manutenção

## Regra de Negócio

- Reserva somente pode ser feita por um pesquisador cadastrado;
- Reserva somente pode ser feita se houver disponibilidade do laboratório (deve ser checado a data e horários do uso);
- Um mesmo pesquisador não pode fazer mais do que 5 reservas, na mesma semana, em um laboratório específico;

- O pedido de manutenção só deve ser registrado após a validação da matrícula de um pesquisador válido;
- Verifica se não há nenhum outro pedido de manutenção do laboratório pelo mesmo pesquisador - (verifica a tabela de pedido manutenção);
- O pedido de manutenção só deve ser registrado com até 5 dias após a data de uso do laboratório reservado (id do pesquisador + laboratório) - verifica a reserva;

## % Relatórios (Jasper Reports)

### Relatório 1: Extrato de Ocupação por Pesquisador

**Objetivo:**  
Consolidar todas as reservas que um pesquisador fez para fins de comprovação de horas de pesquisa.

**Campos:**  
Data Início, Data Fim, Nome do Laboratório e ID da Reserva.

**Diferencial (Requisitos):**  
Utiliza **WHERE** para filtrar por matrícula e **COUNT** para totalizar as reservas no período.

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

---

### Relatório 2: Log de Incidentes e Impacto em Reservas

**Objetivo:**  
Listar os problemas relatados e quem foi o responsável por identificá-los através de uma reserva.

**Campos:**  
Data do Pedido, Nome do Pesquisador, Nome do Laboratório, Descrição do Defeito.

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

## § Gráficos (JavaFX BarChart)

### Gráfico 1: Índice de Confiabilidade (Barras)

**O que mostra:**  
No eixo X, os nomes dos laboratórios. No eixo Y, a quantidade de pedidos de manutenção.

**Objetivo:**  
Identificar visualmente laboratórios com alto índice de falhas.

**SQL Base (Com WHERE e COUNT):**
```sql
SELECT l.nome, COUNT(pm.id) as total_falhas
FROM LABORATORIO l
INNER JOIN PEDIDO_MANUTENCAO pm ON l.id = pm.id_laboratorio
WHERE l.funcional = FALSE  -- Exibe apenas laboratórios com problemas ativos
GROUP BY l.nome;
```

---

## Estrutura do Banco de Dados

### PESQUISADOR
* matricula (PK, VARCHAR(8))
* nome (VARCHAR(100), NOT NULL)
* email (VARCHAR(50), NOT NULL, UNIQUE)
* cpf (VARCHAR(11), NOT NULL, UNIQUE)
* telefone (VARCHAR(11), UNIQUE, NULL)
* suspenso (BOOL(FALSE), NOT NULL)

### LABORATÓRIO
* id (PK, INT)
* nome (VARCHAR(100), NOT NULL)
* area (VARCHAR(50), NOT NULL)
* descricao (VARCHAR(300), NULL)
* funcional (BOOL, NOT NULL)

### RESERVA (REL.)
* id (PK, INT)
* data_inicio (DATETIME, NOT NULL)
* data_fim (DATETIME, NOT NULL)
* matricula_pesquisador (FK)
* id_laboratorio (FK)

### PEDIDO_MANUTENCAO (REL.)
* id (PK, INT)
* id_reserva (FK)
* id_laboratorio (FK)
* hora_pedido (DATETIME, NOT NULL)
* descricao (VARCHAR(500), NOT NULL)
* status_resolvido (BOOL, NOT NULL)

---

## Telas do Sistema

### Tela de Reserva
* Aplicação das três regras de negócio;
* Lista de reservas atuais;
* Seleção de laboratórios via ComboBox.

### Tela de Pedido de Manutenção
* **Validar Pesquisador:** Consulta na tabela PESQUISADOR.
* **Validar Reserva:** Verifica se existe reserva (matrícula + lab) nos últimos 5 dias.
* **Verificar Duplicidade:** Garante que não haja chamado aberto idêntico pendente.
* **Inserir Pedido:** Registro na tabela PEDIDO_MANUTENCAO.
* **Atualizar Status:** Altera `LABORATORIO.funcional` para `FALSE`.
