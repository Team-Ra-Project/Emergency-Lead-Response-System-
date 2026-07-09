package service;

import dao.RoleDAO;
import dao.UserDAO;
import model.User;
import utils.PasswordUtil;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AuthService {
    private final UserDAO userDao = new UserDAO();
    private final RoleDAO roleDao = new RoleDAO();

    public User login(String email, String rawPassword) throws SQLException {
        User u = userDao.findByEmail(email);
        if (u == null) return null;
        if (!"ACTIVE".equalsIgnoreCase(u.getStatus())) return null;
        return PasswordUtil.verify(rawPassword, u.getPasswordHash()) ? u : null;
    }

    public User register(String fullName, String email, String phone,
                         String rawPassword, String roleName) throws SQLException {
        if (userDao.findByEmail(email) != null)
            throw new IllegalStateException("Email already registered");
        Integer roleId = roleDao.findIdByName(roleName == null ? "CUSTOMER" : roleName);
        if (roleId == null) throw new IllegalStateException("Invalid role");

        User u = new User();
        u.setFullName(fullName); u.setEmail(email); u.setPhone(phone);
        u.setPasswordHash(PasswordUtil.hash(rawPassword));
        u.setRoleId(roleId); u.setStatus("ACTIVE");
        userDao.insert(u);
        return u;
    }

    public String createResetToken(String email) throws SQLException {
        User u = userDao.findByEmail(email);
        if (u == null) return null; // do not reveal, but caller can send generic response
        String token = randomToken(48);
        Timestamp expiry = new Timestamp(System.currentTimeMillis() + 60L*60L*1000L); // 1h
        userDao.setResetToken(email, token, expiry);
        return token;
    }

    public boolean resetPassword(String token, String newPassword) throws SQLException {
        User u = userDao.findByResetToken(token);
        if (u == null) return false;
        userDao.updatePassword(u.getUserId(), PasswordUtil.hash(newPassword));
        userDao.clearResetToken(u.getUserId());
        return true;
    }

    private String randomToken(int bytes) {
        byte[] b = new byte[bytes];
        new SecureRandom().nextBytes(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}