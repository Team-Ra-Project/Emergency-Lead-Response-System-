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

@WebServlet("/api/auth/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
    private final AuthService auth = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String,String> b = RequestUtil.readJson(req);
        String email = b.get("email");
        if (!ValidationUtil.email(email)) { JsonUtil.error(resp,400,"Valid email required"); return; }

        try {
            String token = auth.createResetToken(email);
            // In production: send email via EmailUtil. For demo, return the reset link.
            String link = (token == null) ? null :
                "/elrs/pages/auth/reset-password.html?token=" + token;
            JsonUtil.ok(resp, Map.of(
                "message", "If that account exists, a reset link has been sent.",
                "devResetLink", link == null ? "" : link
            ));
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }
}