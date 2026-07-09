package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.AuthService;
import utils.JsonUtil;
import utils.RequestUtil;
import utils.ValidationUtil;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/auth/reset-password")
public class ResetPasswordServlet extends HttpServlet {
    private final AuthService auth = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String,String> b = RequestUtil.readJson(req);
        String token = b.get("token"), pw = b.get("password");
        if (!ValidationUtil.notEmpty(token))  { JsonUtil.error(resp,400,"Token required"); return; }
        if (!ValidationUtil.password(pw))     { JsonUtil.error(resp,400,"Password must be 8+ characters"); return; }
        try {
            boolean ok = auth.resetPassword(token, pw);
            if (!ok) { JsonUtil.error(resp, 400, "Invalid or expired token"); return; }
            JsonUtil.ok(resp, "Password updated");
        } catch (Exception e) { JsonUtil.error(resp, 500, e.getMessage()); }
    }
}