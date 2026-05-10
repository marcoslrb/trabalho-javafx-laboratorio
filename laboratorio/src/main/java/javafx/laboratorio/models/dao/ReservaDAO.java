package javafx.laboratorio.models.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.laboratorio.models.domain.Laboratorio;
import javafx.laboratorio.models.domain.Pesquisador;
import javafx.laboratorio.models.domain.Reserva;

public class ReservaDAO {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public boolean inserir(Reserva reserva) {
        String sql = "INSERT INTO reserva (matricula_pesquisador, id_laboratorio, data_inicio, data_fim) VALUES (?, ?, ?, ?)";
        
        try {
            connection.setAutoCommit(false);
            
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, reserva.getPesquisador().getMatricula());
            stmt.setInt(2, reserva.getLaboratorio().getId());
            
            stmt.setTimestamp(3, Timestamp.valueOf(reserva.getDataHoraInicio()));
            stmt.setTimestamp(4, Timestamp.valueOf(reserva.getDataHoraFim()));
            
            stmt.execute();
            
            connection.commit();
            return true;
            
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Erro crítico ao tentar fazer rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Erro ao tentar registrar a reserva: " + ex.getMessage());
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Erro ao restaurar auto-commit: " + e.getMessage());
            }
        }
    }

    public boolean isLaboratorioOcupado(int idLaboratorio, LocalDateTime inicio, LocalDateTime fim) {
        String sql = "SELECT COUNT(*) AS total FROM reserva WHERE id_laboratorio = ? AND data_inicio < ? AND data_fim > ?";
        
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, idLaboratorio);
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            stmt.setTimestamp(3, Timestamp.valueOf(inicio));
            
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                return resultado.getInt("total") > 0;
            }
        } catch (SQLException ex) {
            System.err.println("Falha na consulta de disponibilidade: " + ex.getMessage());
        }
        return true; 
    }

    public int contarReservasNaSemana(String matricula, int idLaboratorio, LocalDateTime dataPretendida) {

        String sql = "SELECT COUNT(*) AS total FROM reserva "
                   + "WHERE matricula_pesquisador = ? AND id_laboratorio = ? "
                   + "AND EXTRACT(WEEK FROM data_inicio) = EXTRACT(WEEK FROM TIMESTAMP ?) "
                   + "AND EXTRACT(YEAR FROM data_inicio) = EXTRACT(YEAR FROM TIMESTAMP ?)";
                   
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, matricula);
            stmt.setInt(2, idLaboratorio);
            
            Timestamp dataBusca = Timestamp.valueOf(dataPretendida);
            stmt.setTimestamp(3, dataBusca); 
            stmt.setTimestamp(4, dataBusca);
            
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                return resultado.getInt("total");
            }
        } catch (SQLException ex) {
            System.err.println("Erro ao validar o limite de reservas semanais: " + ex.getMessage());
        }
        return 99; 
    }
}