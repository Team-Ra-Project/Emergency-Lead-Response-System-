package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.AuthService;
import utils.JsonUtil;
import utils.RequestUtil;
import utils.SessionUtil;
import utils.ValidationUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/auth/login")
public class LoginServlet extends HttpServlet {
    private final AuthService auth = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String,String> body = RequestUtil.readJson(req);
        String email = body.get("email"), password = body.get("password");

        if (!ValidationUtil.email(email) || !ValidationUtil.password(password)) {
            JsonUtil.error(resp, 400, "Invalid email or password format"); return;
        }
        try {
            User u = auth.login(email, password);
            if (u == null) { JsonUtil.error(resp, 401, "Invalid credentials"); return; }
            SessionUtil.login(req, u);
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("userId", u.getUserId());
            out.put("fullName", u.getFullName());
            out.put("email", u.getEmail());
            out.put("role", u.getRoleName());
            JsonUtil.ok(resp, out);
        } catch (Exception e) {
            JsonUtil.error(resp, 500, "Login failed: " + e.getMessage());
        }
    }
}