package dao;

import model.Lead;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LeadDAO {

    private static final String SELECT =
        "SELECT l.lead_id, l.customer_id, c.full_name AS customer_name, c.phone AS customer_phone, c.email AS customer_email, " +
        "       l.service_id, s.name AS service_name, " +
        "       l.priority, l.status, " +
        "       l.assigned_staff, u.full_name AS staff_name, " +
        "       l.notes, l.source, l.created_at, l.updated_at " +
        "FROM leads l " +
        "JOIN customers c        ON l.customer_id     = c.customer_id " +
        "LEFT JOIN services s    ON l.service_id      = s.service_id " +
        "LEFT JOIN staff st      ON l.assigned_staff  = st.staff_id " +
        "LEFT JOIN users u       ON st.user_id        = u.user_id";

    private Lead map(ResultSet rs) throws SQLException {
        Lead l = new Lead();
        l.setLeadId(rs.getInt("lead_id"));
        l.setCustomerId(rs.getInt("customer_id"));
        l.setCustomerName(rs.getString("customer_name"));
        l.setCustomerPhone(rs.getString("customer_phone"));
        l.setCustomerEmail(rs.getString("customer_email"));
        int sid = rs.getInt("service_id");
        l.setServiceId(rs.wasNull() ? null : sid);
        l.setServiceName(rs.getString("service_name"));
        l.setPriority(rs.getString("priority"));
        l.setStatus(rs.getString("status"));
        int as = rs.getInt("assigned_staff");
        l.setAssignedStaff(rs.wasNull() ? null : as);
        l.setAssignedStaffName(rs.getString("staff_name"));
        l.setNotes(rs.getString("notes"));
        l.setSource(rs.getString("source"));
        Timestamp c = rs.getTimestamp("created_at");
        if (c != null) l.setCreatedAt(c.toLocalDateTime());
        Timestamp u = rs.getTimestamp("updated_at");
        if (u != null) l.setUpdatedAt(u.toLocalDateTime());
        return l;
    }

    public Lead findById(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE l.lead_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Lead> findAll() throws SQLException {
        List<Lead> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " ORDER BY l.lead_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Search + filter. Any parameter may be null / empty / "All". */
    public List<Lead> search(String q, String status, String priority) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (q != null && !q.isBlank()) {
            sql.append(" AND (c.full_name LIKE ? OR c.phone LIKE ? OR c.email LIKE ?)");
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All")) {
            sql.append(" AND l.status=?");
            params.add(status);
        }
        if (priority != null && !priority.isBlank() && !priority.equalsIgnoreCase("All")) {
            sql.append(" AND l.priority=?");
            params.add(priority);
        }
        sql.append(" ORDER BY l.lead_id DESC");

        List<Lead> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int insert(Lead l) throws SQLException {
        String sql = "INSERT INTO leads(customer_id,service_id,priority,status,assigned_staff,notes,source) " +
                     "VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, l.getCustomerId());
            if (l.getServiceId() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, l.getServiceId());
            ps.setString(3, l.getPriority() == null ? "MEDIUM" : l.getPriority());
            ps.setString(4, l.getStatus()   == null ? "NEW"    : l.getStatus());
            if (l.getAssignedStaff() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, l.getAssignedStaff());
            ps.setString(6, l.getNotes());
            ps.setString(7, l.getSource());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { int id = keys.getInt(1); l.setLeadId(id); return id; }
            }
        }
        return -1;
    }

    public boolean update(Lead l) throws SQLException {
        String sql = "UPDATE leads SET service_id=?,priority=?,status=?,assigned_staff=?,notes=?,source=? WHERE lead_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (l.getServiceId() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, l.getServiceId());
            ps.setString(2, l.getPriority());
            ps.setString(3, l.getStatus());
            if (l.getAssignedStaff() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, l.getAssignedStaff());
            ps.setString(5, l.getNotes());
            ps.setString(6, l.getSource());
            ps.setInt(7, l.getLeadId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int leadId, String status) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE leads SET status=? WHERE lead_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, leadId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean assignStaff(int leadId, Integer staffId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE leads SET assigned_staff=? WHERE lead_id=?")) {
            if (staffId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, staffId);
            ps.setInt(2, leadId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM leads WHERE lead_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
