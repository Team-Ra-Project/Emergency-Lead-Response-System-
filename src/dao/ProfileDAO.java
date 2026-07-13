package dao;

import model.User;
import utils.DBConnection;

import java.sql.*;

/**
 * Read/write access to the logged-in user's own profile.
 * Uses the extended profile columns added to the users table
 * (avatar_url, department, designation, employee_code, date_joined).
 */
public class ProfileDAO {

    private static final String SELECT =
        "SELECT u.user_id,u.full_name,u.email,u.phone,u.password_hash,u.role_id,r.role_name,u.status," +
        "       u.created_at,u.avatar_url,u.department,u.designation,u.employee_code,u.date_joined " +
        "FROM users u JOIN roles r ON u.role_id=r.role_id WHERE u.user_id=?";

    public User findById(int userId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setRoleId(rs.getInt("role_id"));
                u.setRoleName(rs.getString("role_name"));
                u.setStatus(rs.getString("status"));
                Timestamp t = rs.getTimestamp("created_at");
                if (t != null) u.setCreatedAt(t.toLocalDateTime());
                u.setAvatarUrl(rs.getString("avatar_url"));
                u.setDepartment(rs.getString("department"));
                u.setDesignation(rs.getString("designation"));
                u.setEmployeeCode(rs.getString("employee_code"));
                Date d = rs.getDate("date_joined");
                if (d != null) u.setDateJoined(d.toLocalDate());
                return u;
            }
        }
    }

    /** Update editable profile fields (not password, not role). */
    public boolean updateProfile(User u) throws SQLException {
        String sql = "UPDATE users SET full_name=?, phone=?, department=?, designation=? WHERE user_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getPhone());
            ps.setString(3, u.getDepartment());
            ps.setString(4, u.getDesignation());
            ps.setInt(5, u.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateAvatar(int userId, String avatarUrl) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET avatar_url=? WHERE user_id=?")) {
            ps.setString(1, avatarUrl); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public String getPasswordHash(int userId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT password_hash FROM users WHERE user_id=?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : null; }
        }
    }

    public boolean updatePassword(int userId, String hash) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")) {
            ps.setString(1, hash); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }
}
