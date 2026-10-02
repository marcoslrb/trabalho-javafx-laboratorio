import logging
import os
from concurrent import futures

import grpc
import laboratorio_pb2
import laboratorio_pb2_grpc
import psycopg2


# Configuração da conexão com o banco de dados PostgreSQL
def get_db_connection():
    env_pass = os.getenv("DB_PASSWORD")
    candidate_passwords = [env_pass, "postgres", "admin"] if env_pass else ["postgres", "admin"]
    for pwd in candidate_passwords:
        try:
            dsn = f"host=127.0.0.1 port=5432 dbname=laboratorio user=postgres password={pwd}"
            return psycopg2.connect(dsn)
        except psycopg2.OperationalError:
            continue
    raise psycopg2.OperationalError("Não foi possível autenticar no PostgreSQL com as senhas padrão.")

GRPC_PORT = 50051

logger = logging.getLogger("laboratorio-server")

class LaboratorioServiceServicer(laboratorio_pb2_grpc.LaboratorioServiceServicer):
    def ConsultarPorId(self, request, context):
        try:
            conn = get_db_connection()
            try:
                cur = conn.cursor()
                # Busca apenas as colunas que existem no banco (nome e area)
                cur.execute(
                    "SELECT nome, area "
                    "FROM laboratorio WHERE id = %s",
                    (request.id,),
                )
                row = cur.fetchone()
                cur.close()
            finally:
                conn.close()

            if row is None:
                context.set_code(grpc.StatusCode.NOT_FOUND)
                context.set_details(f"Laboratório com id {request.id} não encontrado.")
                return laboratorio_pb2.ConsultarPorIdResponse()

            # Mapeia a resposta para os campos definidos no .proto
            return laboratorio_pb2.ConsultarPorIdResponse(
                nome=row[0],
                departamento=row[1], # Utiliza 'area' no lugar de departamento
                capacidade=0         # Retorna 0, pois a coluna não existe no banco
            )

        except psycopg2.Error as exc:
            logger.exception("Erro ao consultar o PostgreSQL")
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(f"Falha ao consultar o banco de dados: {exc}")
            return laboratorio_pb2.ConsultarPorIdResponse()


def serve():
    logging.basicConfig(level=logging.INFO)
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    
    laboratorio_pb2_grpc.add_LaboratorioServiceServicer_to_server(
        LaboratorioServiceServicer(), server
    )
    
    server.add_insecure_port(f"[::]:{GRPC_PORT}")
    server.start()
    print(f"Servidor gRPC pronto em [::]:{GRPC_PORT}")
    server.wait_for_termination()


if __name__ == "__main__":
    serve()