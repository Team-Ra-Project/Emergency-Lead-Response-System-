package service;

import dao.NotificationDAO;
import model.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {
    private final NotificationDAO dao = new NotificationDAO();
    private final NotificationDispatcher dispatcher = new NotificationDispatcher();

    public int notify(int userId, String channel, String title, String message) throws SQLException {
        String status = dispatcher.dispatch(userId, channel, title, message);
        Notification n = new Notification();
        n.setUserId(userId); n.setChannel(channel); n.setTitle(title);
        // Persist delivery status alongside the message so the in-app bell
        // shows what actually happened on the transport side.
        n.setMessage(message + "\n[" + status + "]");
        return dao.insert(n);
    }
    public List<Notification> forUser(int userId) throws SQLException { return dao.findByUser(userId); }
    public boolean markRead(int id, int userId) throws SQLException { return dao.markRead(id, userId); }
    public int unread(int userId) throws SQLException { return dao.unreadCount(userId); }
}