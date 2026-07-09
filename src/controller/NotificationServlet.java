package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Notification;
import model.User;
import service.NotificationService;
import utils.JsonUtil;
import utils.RequestUtil;
import utils.SessionUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/notifications", "/api/notifications/*"})
public class NotificationServlet extends HttpServlet {
    private final NotificationService svc = new NotificationService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u = SessionUtil.currentUser(req);
        try {
            if ("/unread-count".equals(req.getPathInfo())) {
                JsonUtil.ok(resp, Map.of("count", svc.unread(u.getUserId()))); return;
            }
            List<Map<String,Object>> out = new ArrayList<>();
            for (Notification n : svc.forUser(u.getUserId())) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("id", n.getNotificationId());
                m.put("channel", n.getChannel());
                m.put("title", n.getTitle());
                m.put("message", n.getMessage());
                m.put("read", n.isRead());
                m.put("createdAt", n.getCreatedAt() == null ? "" : n.getCreatedAt().toString());
                out.add(m);
            }
            JsonUtil.ok(resp, out);
        } catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u = SessionUtil.currentUser(req);
        try {
            Map<String,String> b = RequestUtil.readJson(req);
            int userId = b.get("userId") != null ? Integer.parseInt(b.get("userId")) : u.getUserId();
            int id = svc.notify(userId, b.getOrDefault("channel","BROWSER"),
                b.get("title"), b.get("message"));
            JsonUtil.ok(resp, Map.of("id", id));
        } catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }

    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u = SessionUtil.currentUser(req);
        try {
            Integer id = RequestUtil.pathId(req);
            if (id == null) { JsonUtil.error(resp,400,"Notification id required"); return; }
            svc.markRead(id, u.getUserId());
            JsonUtil.ok(resp, "Marked read");
        } catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }
}