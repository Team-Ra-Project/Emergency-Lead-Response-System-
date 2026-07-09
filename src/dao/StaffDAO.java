package dao;

import model.Staff;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    private static final String SELECT =
        "SELECT s.staff_id, s.user_id, u.full_name, u.email, u.phone, " +
        "       s.designation, s.specialization, s.availability, s.joined_on " +
        "FROM staff s JOIN users u ON s.user_id = u.user_id";

    private Staff map(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffId(rs.getInt("staff_id"));
        s.setUserId(rs.getInt("user_id"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setDesignation(rs.getString("designation"));
        s.setSpecialization(rs.getString("specialization"));
        s.setAvailability(rs.getString("availability"));
        Date d = rs.getDate("joined_on"); if (d != null) s.setJoinedOn(d.toLocalDate());
        return s;
    }

    public List<Staff> findAll() throws SQLException {
        List<Staff> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " ORDER BY s.staff_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Staff findById(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE s.staff_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public int insert(int userId, String designation, String specialization,
                      String availability, Date joinedOn) throws SQLException {
        String sql = "INSERT INTO staff(user_id,designation,specialization,availability,joined_on) VALUES(?,?,?,?,?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setString(2, designation);
            ps.setString(3, specialization);
            ps.setString(4, availability == null ? "AVAILABLE" : availability);
            ps.setDate(5, joinedOn);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getInt(1); }
        }
        return -1;
    }

    public boolean update(int staffId, String designation, String specialization, String availability) throws SQLException {
        String sql = "UPDATE staff SET designation=?, specialization=?, availability=? WHERE staff_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, designation); ps.setString(2, specialization);
            ps.setString(3, availability); ps.setInt(4, staffId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int staffId) throws SQLException {
        // Cascade removes staff row; keep user row.
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM staff WHERE staff_id=?")) {
            ps.setInt(1, staffId); return ps.executeUpdate() > 0;
        }
    }

    /** Returns [staffId, name, leadCount, completedCount]. */
    public List<Object[]> performance() throws SQLException {
        String sql =
          "SELECT s.staff_id, u.full_name, " +
          "  COALESCE(SUM(CASE WHEN l.lead_id IS NOT NULL THEN 1 ELSE 0 END),0) AS assigned, " +
          "  COALESCE(SUM(CASE WHEN l.status='COMPLETED' THEN 1 ELSE 0 END),0) AS completed " +
          "FROM staff s JOIN users u ON s.user_id=u.user_id " +
          "LEFT JOIN leads l ON l.assigned_staff=s.staff_id " +
          "GROUP BY s.staff_id, u.full_name ORDER BY completed DESC";
        List<Object[]> out = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(new Object[]{
                rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4)
            });
        }
        return out;
    }
}