package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.JsonUtil;
import utils.SessionUtil;

import java.io.IOException;

/**
 * RBAC: /api/users, /api/roles, /api/staff, /api/reports are ADMIN /
 * BUSINESS_OWNER only. STAFF users must not read these endpoints — that
 * matches the frontend sidebar which already hides Staff & Reports from
 * non-admin roles.
 */
@WebFilter(urlPatterns = {"/api/users/*", "/api/roles/*", "/api/staff/*", "/api/reports/*"})
public class AuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse w = (HttpServletResponse) res;

        if (SessionUtil.hasRole(r, "ADMIN", "BUSINESS_OWNER")) {
            chain.doFilter(req, res); return;
        }
        JsonUtil.error(w, HttpServletResponse.SC_FORBIDDEN, "Access denied");
    }
}