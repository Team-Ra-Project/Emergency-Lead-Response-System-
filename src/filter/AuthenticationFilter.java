package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.JsonUtil;
import utils.SessionUtil;

import java.io.IOException;
import java.util.Set;

/**
 * Blocks unauthenticated access to /api/* except public endpoints
 * (login, register, forgot/reset password).
 */
@WebFilter(urlPatterns = {"/api/*"})
public class AuthenticationFilter implements Filter {

    private static final Set<String> PUBLIC = Set.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/forgot-password",
        "/api/auth/reset-password"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;

        String path = r.getRequestURI().substring(r.getContextPath().length());
        if (PUBLIC.contains(path)) { chain.doFilter(req, res); return; }

        if (SessionUtil.currentUser(r) == null) {
            JsonUtil.error(w, HttpServletResponse.SC_UNAUTHORIZED, "Login required");
            return;
        }
        chain.doFilter(req, res);
    }
}