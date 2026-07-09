package dao;

import model.Notification;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    public int insert(Notification n) throws SQLException {
        String sql = "INSERT INTO notifications(user_id,channel,title,message) VALUES(?,?,?,?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, n.getUserId());
            ps.setString(2, n.getChannel() == null ? "BROWSER" : n.getChannel());
            ps.setString(3, n.getTitle());
            ps.setString(4, n.getMessage());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) return k.getInt(1); }
        }
        return -1;
    }

    public List<Notification> findByUser(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT * FROM notifications WHERE user_id=? ORDER BY created_at DESC LIMIT 50")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationId(rs.getInt("notification_id"));
                    n.setUserId(rs.getInt("user_id"));
                    n.setChannel(rs.getString("channel"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getBoolean("is_read"));
                    Timestamp t = rs.getTimestamp("created_at");
                    if (t != null) n.setCreatedAt(t.toLocalDateTime());
                    list.add(n);
                }
            }
        }
        return list;
    }

    public boolean markRead(int id, int userId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE notifications SET is_read=1 WHERE notification_id=? AND user_id=?")) {
            ps.setInt(1, id); ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int unreadCount(int userId) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read=0")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }
}