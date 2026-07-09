package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple JDBC connection helper. For production, wrap with a pool (HikariCP).
 */
public class DBConnection {
    private static final String URL  = "jdbc:mysql://localhost:3306/elrs?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Tanvi9903@";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver missing", e);
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}