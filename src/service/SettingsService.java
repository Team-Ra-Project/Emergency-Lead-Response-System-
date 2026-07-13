package service;

import dao.SettingsDAO;

import java.sql.SQLException;
import java.util.Map;

public class SettingsService {

    private final SettingsDAO dao = new SettingsDAO();

    public Map<String,String> getAll() throws SQLException { return dao.findAll(); }

    public String get(String key) throws SQLException { return dao.get(key); }

    public void saveAll(Map<String,String> values) throws SQLException {
        if (values == null || values.isEmpty()) return;
        dao.upsertAll(values);
    }

    public void save(String key, String value) throws SQLException {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Setting key is required");
        dao.upsert(key, value);
    }
}
