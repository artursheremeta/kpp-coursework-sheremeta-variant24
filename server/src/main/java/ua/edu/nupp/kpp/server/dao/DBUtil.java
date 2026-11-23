
package ua.edu.nupp.kpp.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Підключення до бази даних MySQL. */
public final class DBUtil {

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC-драйвер не знайдено", e);
        }

        String url = env(
            "MUSEUM_DB_URL",
            "jdbc:mysql://localhost:3306/museum_exhibits?useUnicode=true&characterEncoding=utf8"
        );

        String user = env("MUSEUM_DB_USER", "root");
        String pass = env("MUSEUM_DB_PASSWORD", "");

        return DriverManager.getConnection(url, user, pass);
    }

    private static String env(String name, String fallback) {
        String v = System.getenv(name);
        return (v == null) ? fallback : v;
    }
}

