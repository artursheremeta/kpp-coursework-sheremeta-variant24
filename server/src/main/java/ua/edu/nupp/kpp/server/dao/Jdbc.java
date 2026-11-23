package ua.edu.nupp.kpp.server.dao;

import java.sql.*;
import java.util.*;

/** SQL operations are centralized here, never exposed to the JavaFX client. */
public final class Jdbc {
    private Jdbc() { }
    @FunctionalInterface public interface Bind { void apply(PreparedStatement ps) throws SQLException; }
    @FunctionalInterface public interface Read<T> { T apply(ResultSet rs) throws SQLException; }

    public static <T> List<T> list(String sql, Bind bind, Read<T> read) throws SQLException {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind.apply(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> found = new ArrayList<>();
                while (rs.next()) found.add(read.apply(rs));
                return found;
            }
        }
    }
    public static <T> T one(String sql, Bind bind, Read<T> read) throws SQLException {
        List<T> values = list(sql, bind, read);
        return values.isEmpty() ? null : values.getFirst();
    }
    public static int execute(String sql, Bind bind) throws SQLException {
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind.apply(ps);
            return ps.executeUpdate();
        }
    }
    public static long insert(String sql, Bind bind) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind.apply(ps);
            if (ps.executeUpdate() != 1) throw new SQLException("Не вдалося додати запис");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new SQLException("База не повернула ID нового запису");
            }
        }
    }
}
