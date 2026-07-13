package dao;

import model.Service;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    private static final String SELECT =
        "SELECT service_id, name, category, description, price, duration, status, created_at, updated_at FROM services";

    private Service map(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setServiceId(rs.getInt("service_id"));
        s.setName(rs.getString("name"));
        s.setCategory(rs.getString("category"));
        s.setDescription(rs.getString("description"));
        BigDecimal p = rs.getBigDecimal("price");
        s.setPrice(p);
        s.setDuration(rs.getString("duration"));
        s.setStatus(rs.getString("status"));
        Timestamp c = rs.getTimestamp("created_at");
        if (c != null) s.setCreatedAt(c.toLocalDateTime());
        Timestamp u = rs.getTimestamp("updated_at");
        if (u != null) s.setUpdatedAt(u.toLocalDateTime());
        return s;
    }

    public Service findById(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE service_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public List<Service> findAll() throws SQLException {
        List<Service> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " ORDER BY service_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Service> search(String q, String category, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" AND (name LIKE ? OR category LIKE ? OR description LIKE ?)");
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("All")) {
            sql.append(" AND category=?"); params.add(category);
        }
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All")) {
            sql.append(" AND status=?"); params.add(status);
        }
        sql.append(" ORDER BY service_id DESC");

        List<Service> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public int insert(Service s) throws SQLException {
        String sql = "INSERT INTO services(name,category,description,price,duration,status) VALUES(?,?,?,?,?,?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getCategory());
            ps.setString(3, s.getDescription());
            if (s.getPrice() == null) ps.setNull(4, Types.DECIMAL); else ps.setBigDecimal(4, s.getPrice());
            ps.setString(5, s.getDuration());
            ps.setString(6, s.getStatus() == null ? "Active" : s.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { int id = keys.getInt(1); s.setServiceId(id); return id; }
            }
        }
        return -1;
    }

    public boolean update(Service s) throws SQLException {
        String sql = "UPDATE services SET name=?,category=?,description=?,price=?,duration=?,status=? WHERE service_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getCategory());
            ps.setString(3, s.getDescription());
            if (s.getPrice() == null) ps.setNull(4, Types.DECIMAL); else ps.setBigDecimal(4, s.getPrice());
            ps.setString(5, s.getDuration());
            ps.setString(6, s.getStatus());
            ps.setInt(7, s.getServiceId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM services WHERE service_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
