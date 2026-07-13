package dao;

import utils.DBConnection;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Key/value store backing the Settings module. */
public class SettingsDAO {

    public Map<String,String> findAll() throws SQLException {
        Map<String,String> out = new LinkedHashMap<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT setting_key, setting_value FROM settings ORDER BY setting_key");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.put(rs.getString("setting_key"), rs.getString("setting_value"));
        }
        return out;
    }

    public String get(String key) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT setting_value FROM settings WHERE setting_key=?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : null; }
        }
    }

    /** Insert or update a single key (MySQL upsert). */
    public void upsert(String key, String value) throws SQLException {
        String sql = "INSERT INTO settings(setting_key, setting_value) VALUES(?,?) " +
                     "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value), updated_at=CURRENT_TIMESTAMP";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key); ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    /** Bulk upsert inside a single transaction. */
    public void upsertAll(Map<String,String> values) throws SQLException {
        String sql = "INSERT INTO settings(setting_key, setting_value) VALUES(?,?) " +
                     "ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value), updated_at=CURRENT_TIMESTAMP";
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (Map.Entry<String,String> e : values.entrySet()) {
                    ps.setString(1, e.getKey());
                    ps.setString(2, e.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
}
