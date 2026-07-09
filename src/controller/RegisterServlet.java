package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.AuthService;
import utils.JsonUtil;
import utils.RequestUtil;
import utils.ValidationUtil;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/auth/register")
public class RegisterServlet extends HttpServlet {
    private final AuthService auth = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String,String> b = RequestUtil.readJson(req);
        String name = b.get("fullName"), email = b.get("email"),
               phone = b.get("phone"), password = b.get("password"),
               role = b.getOrDefault("role", "CUSTOMER");

        if (!ValidationUtil.notEmpty(name))     { JsonUtil.error(resp,400,"Name required"); return; }
        if (!ValidationUtil.email(email))       { JsonUtil.error(resp,400,"Valid email required"); return; }
        if (!ValidationUtil.phone(phone))       { JsonUtil.error(resp,400,"Valid phone required"); return; }
        if (!ValidationUtil.password(password)) { JsonUtil.error(resp,400,"Password must be 8+ characters"); return; }

        try {
            User u = auth.register(name.trim(), email.trim().toLowerCase(),
                                   phone.trim(), password, role.toUpperCase());
            JsonUtil.ok(resp, Map.of("userId", u.getUserId(), "email", u.getEmail()));
        } catch (IllegalStateException e) {
            JsonUtil.error(resp, 409, e.getMessage());
        } catch (Exception e) {
            JsonUtil.error(resp, 500, "Registration failed: " + e.getMessage());
        }
    }
}