package dao;

import model.User;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final String SELECT =
        "SELECT u.user_id,u.full_name,u.email,u.phone,u.password_hash,u.role_id,r.role_name,u.status,u.created_at " +
        "FROM users u JOIN roles r ON u.role_id=r.role_id";

    private User map(ResultSet rs) throws SQLException {
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
        return u;
    }

    public User findByEmail(String email) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE u.email=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public User findById(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE u.user_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " ORDER BY u.user_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public int insert(User u) throws SQLException {
        String sql = "INSERT INTO users(full_name,email,phone,password_hash,role_id,status) VALUES(?,?,?,?,?,?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPhone());
            ps.setString(4, u.getPasswordHash());
            ps.setInt(5, u.getRoleId());
            ps.setString(6, u.getStatus() == null ? "ACTIVE" : u.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { int id = keys.getInt(1); u.setUserId(id); return id; }
            }
        }
        return -1;
    }

    public boolean update(User u) throws SQLException {
        String sql = "UPDATE users SET full_name=?,phone=?,role_id=?,status=? WHERE user_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getFullName());
            ps.setString(2, u.getPhone());
            ps.setInt(3, u.getRoleId());
            ps.setString(4, u.getStatus());
            ps.setInt(5, u.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int userId, String hash) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")) {
            ps.setString(1, hash); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean setResetToken(String email, String token, Timestamp expiry) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE users SET reset_token=?, reset_expiry=? WHERE email=?")) {
            ps.setString(1, token); ps.setTimestamp(2, expiry); ps.setString(3, email);
            return ps.executeUpdate() > 0;
        }
    }

    public User findByResetToken(String token) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 SELECT + " WHERE u.reset_token=? AND u.reset_expiry > NOW()")) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public void clearResetToken(int userId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE users SET reset_token=NULL, reset_expiry=NULL WHERE user_id=?")) {
            ps.setInt(1, userId); ps.executeUpdate();
        }
    }
}