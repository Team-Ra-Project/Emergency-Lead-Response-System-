package utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import model.User;

public class SessionUtil {
    public static final String USER_KEY = "AUTH_USER";

    public static User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute(USER_KEY);
    }
    public static void login(HttpServletRequest req, User u) {
        HttpSession s = req.getSession(true);
        s.setAttribute(USER_KEY, u);
        s.setMaxInactiveInterval(60 * 60); // 1 hour
    }
    public static void logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
    }
    public static boolean hasRole(HttpServletRequest req, String... roles) {
        User u = currentUser(req);
        if (u == null) return false;
        for (String r : roles) if (r.equalsIgnoreCase(u.getRoleName())) return true;
        return false;
    }
}