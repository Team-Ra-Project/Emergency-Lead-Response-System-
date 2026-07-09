package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import utils.JsonUtil;
import utils.SessionUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Returns the current logged-in user (for header/profile). */
@WebServlet("/api/auth/me")
public class MeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User u = SessionUtil.currentUser(req);
        if (u == null) { JsonUtil.error(resp,401,"Not logged in"); return; }
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("userId", u.getUserId());
        out.put("fullName", u.getFullName());
        out.put("email", u.getEmail());
        out.put("role", u.getRoleName());
        JsonUtil.ok(resp, out);
    }
}