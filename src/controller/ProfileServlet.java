package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.ProfileService;
import utils.JsonUtil;
import utils.RequestUtil;
import utils.SessionUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Profile module for the logged-in user.
 *
 *   GET  /api/profile             -> current user's profile
 *   PUT  /api/profile             -> update editable fields
 *   PUT  /api/profile/photo       -> update avatar url
 *   PUT  /api/profile/password    -> change password
 */
@WebServlet(urlPatterns = {"/api/profile", "/api/profile/*"})
public class ProfileServlet extends HttpServlet {

    private final ProfileService svc = new ProfileService();

    private User me(HttpServletRequest req) { return SessionUtil.currentUser(req); }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User session = me(req);
            if (session == null) { JsonUtil.error(resp, 401, "Login required"); return; }
            User u = svc.get(session.getUserId());
            if (u == null) { JsonUtil.error(resp, 404, "Profile not found"); return; }
            JsonUtil.ok(resp, toMap(u));
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            User session = me(req);
            if (session == null) { JsonUtil.error(resp, 401, "Login required"); return; }
            int userId = session.getUserId();
            String path = req.getPathInfo();
            Map<String,String> b = RequestUtil.readJson(req);

            if (path != null && path.equalsIgnoreCase("/password")) {
                svc.changePassword(userId, b.get("currentPassword"), b.get("newPassword"));
                JsonUtil.ok(resp, "Password changed");
                return;
            }
            if (path != null && path.equalsIgnoreCase("/photo")) {
                String url = b.get("avatarUrl");
                svc.updateAvatar(userId, url);
                JsonUtil.ok(resp, "Photo updated");
                return;
            }

            User u = svc.get(userId);
            if (u == null) { JsonUtil.error(resp, 404, "Profile not found"); return; }
            if (b.get("fullName")    != null) u.setFullName(b.get("fullName"));
            if (b.get("phone")       != null) u.setPhone(b.get("phone"));
            if (b.get("department")  != null) u.setDepartment(b.get("department"));
            if (b.get("designation") != null) u.setDesignation(b.get("designation"));
            svc.updateProfile(u);

            // Keep the session copy fresh.
            session.setFullName(u.getFullName());
            session.setPhone(u.getPhone());
            JsonUtil.ok(resp, "Profile updated");
        } catch (IllegalArgumentException iae) {
            JsonUtil.error(resp, 400, iae.getMessage());
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    private Map<String,Object> toMap(User u) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("userId",       u.getUserId());
        m.put("fullName",     u.getFullName());
        m.put("email",        u.getEmail());
        m.put("phone",        u.getPhone());
        m.put("roleName",     u.getRoleName());
        m.put("status",       u.getStatus());
        m.put("avatarUrl",    u.getAvatarUrl());
        m.put("department",   u.getDepartment());
        m.put("designation",  u.getDesignation());
        m.put("employeeCode", u.getEmployeeCode());
        m.put("dateJoined",   u.getDateJoined() == null ? null : u.getDateJoined().toString());
        m.put("createdAt",    u.getCreatedAt() == null ? null : u.getCreatedAt().toString());
        return m;
    }
}
