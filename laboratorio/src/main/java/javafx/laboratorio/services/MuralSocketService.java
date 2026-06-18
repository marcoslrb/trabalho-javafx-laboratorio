package javafx.laboratorio.services;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import sockets.thread.RespostaMural;

/**
 * Serviço responsável pela comunicação via Socket com o servidor do Mural de Avisos.
 * Envia o id do grupo e recebe o objeto RespostaMural serializado.
 */
public class MuralSocketService {

    private static final String ENDERECO = "127.0.0.1";
    private static final int PORTA = 12345;

    /**
     * Conecta ao servidor, envia o idGrupo e retorna a resposta deserializada.
     *
     * @param idGrupo número do grupo (1–10)
     * @return objeto RespostaMural recebido do servidor
     * @throws Exception em caso de erro de conexão ou desserialização
     */
    public RespostaMural consultarMural(int idGrupo) throws Exception {
        try (Socket socket = new Socket(ENDERECO, PORTA);
             ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream())) {

            // Envia o id do grupo como Integer
            saida.writeObject(Integer.valueOf(idGrupo));
            saida.flush();

            // Recebe e retorna o objeto RespostaMural
            return (RespostaMural) entrada.readObject();
        }
    }
}
