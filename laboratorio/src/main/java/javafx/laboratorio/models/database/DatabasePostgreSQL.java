package javafx.laboratorio.models.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabasePostgreSQL implements Database {

    private Connection connection;

    @Override
    public Connection conectar() {
        try {
            Class.forName("org.postgresql.Driver");
            String envPass = System.getenv("DB_PASSWORD");
            String[] candidatePasswords = (envPass != null && !envPass.isBlank()) 
                    ? new String[]{envPass, "postgres", "admin"} 
                    : new String[]{"postgres", "admin"};

            SQLException lastEx = null;
            for (String pass : candidatePasswords) {
                try {
                    this.connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/laboratorio", "postgres", pass);
                    return this.connection;
                } catch (SQLException sqle) {
                    lastEx = sqle;
                }
            }
            if (lastEx != null) {
                Logger.getLogger(DatabasePostgreSQL.class.getName()).log(Level.SEVERE, null, lastEx);
            }
            return null;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatabasePostgreSQL.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public void desconectar(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabasePostgreSQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}