package javafx.laboratorio.models.database;

public class DatabaseFactory {
    public static Database getDatabase(String nome){
        if(nome.equals("postgresql")){
            return new DatabasePostgreSQL();
        }
        // Se no futuro quiser usar MySQL, basta adicionar o 'else if' aqui
        return null;
    }
}