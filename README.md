````md
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

## % Relatórios

### Relatório 1: Extrato de Ocupação por Pesquisador

**Objetivo:**  
Consolidar todas as reservas que um pesquisador fez para fins de comprovação de horas de pesquisa.

**Campos:**  
Data Início, Data Fim, Nome do Laboratório e Tempo Total (em horas).

**Filtro:**  
Por Matrícula do Pesquisador.

---

### Relatório 2: Log de Incidentes e Impacto em Reservas

**Objetivo:**  
Listar os problemas relatados e quem foi o responsável por identificá-los através de uma reserva.

**Campos:**  
Data do Pedido, Nome do Pesquisador, Nome do Laboratório, Descrição do Defeito e ID da Reserva de origem.

**Diferencial:**  
Este relatório prova que sua regra de "Pedido vinculado à Reserva" está funcionando na prática.

---

## § Gráficos

### Gráfico 1: Índice de Confiabilidade dos Laboratórios (Barras)

**O que mostra:**  
No eixo X, os Laboratórios. No eixo Y, a quantidade de Pedidos de Manutenção realizados.

**Objetivo:**  
Identificar visualmente qual laboratório é o "campeão" de problemas. Um laboratório com muitas barras altas pode indicar equipamentos velhos que precisam de troca definitiva.

**SQL Base:**  
```sql
SELECT id_laboratorio, COUNT(*) FROM PEDIDO_MANUTENCAO GROUP BY id_laboratorio;
```

---

### Gráfico 2: Proporção de Status de Manutenção (Pizza/Donut)

**O que mostra:**
A divisão entre pedidos com status_resolvido = TRUE vs status_resolvido = FALSE.

**Objetivo:**
Mostrar a eficiência da equipe de infraestrutura. Se a fatia de "Não Resolvido" estiver muito grande, o laboratório está ficando parado por muito tempo.

**SQL Base:**

```sql
SELECT status_resolvido, COUNT(*) FROM PEDIDO_MANUTENCAO GROUP BY status_resolvido;
```

---

## PESQUISADOR

* matricula (PK, VARCHAR(8))
* nome (VARCHAR(100), NOT NULL)
* email (VARCHAR(50), NOT NULL, UNIQUE)
* cpf (VARCHAR(11), NOT NULL, UNIQUE)
* telefone (VARCHAR(11), UNIQUE, NULL)

## LABORATÓRIO

* id (PK, INT)
* nome (VARCHAR(100), NOT NULL)
* area (VARCHAR(50), NOT NULL)
* descricao (VARCHAR(300), NULL)
* funcional (BOOL, NOT NULL)

## RESERVA (REL.)

* id (PK, INT)
* data_inicio (DATETIME, NOT NULL)
* data_fim (DATETIME, NOT NULL)
* matricula_pesquisador (FK)
* id_laboratorio (FK)

## PEDIDO_MANUTENCAO (REL.)

* id (PK, INT)
* id_reserva (FK)
* hora_pedido (DATETIME, NOT NULL)
* descricao (VARCHAR(500), NOT NULL)
* status_resolvido (BOOL, NOT NULL)

---

## Tela de Reserva

* As três regras de negócio + lista de reservas já feitas + labs disponíveis em um combobox;

## Tela de Pedido de Manutenção

* Validar Pesquisador: SELECT na tabela PESQUISADOR para conferir a matrícula.
* Validar Reserva: SELECT na tabela RESERVA filtrando por matricula + id_laboratorio + data dentro dos 5 dias.
* Verificar Duplicidade: SELECT na tabela PEDIDO_MANUTENCAO para garantir que esse pesquisador já não abriu um chamado idêntico que ainda não foi resolvido.
* Inserir Pedido: INSERT na tabela PEDIDO_MANUTENCAO.
* Atualizar Status do Lab: UPDATE na tabela LABORATORIO definindo funcional = FALSE (já que houve um relato de problema).
