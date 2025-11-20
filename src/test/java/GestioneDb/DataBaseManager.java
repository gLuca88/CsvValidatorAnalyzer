package GestioneDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseManager {

    private static Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/configdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "gianluca";

    private DataBaseManager() {}

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false);   // <— IMPORTANTE
        }
        return connection;
    }

    public static void commit() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.commit();
        }
    }

    public static void rollback() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.rollback();
        }
    }

    public static void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
