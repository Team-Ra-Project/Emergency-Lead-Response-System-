package service;

import dao.ProfileDAO;
import model.User;
import utils.PasswordUtil;
import utils.ValidationUtil;

import java.sql.SQLException;

public class ProfileService {

    private final ProfileDAO dao = new ProfileDAO();

    public User get(int userId) throws SQLException { return dao.findById(userId); }

    public boolean updateProfile(User u) throws SQLException {
        if (u.getUserId() <= 0) throw new IllegalArgumentException("Not authenticated");
        if (!ValidationUtil.notEmpty(u.getFullName()))
            throw new IllegalArgumentException("Full name is required");
        if (u.getPhone() != null && !u.getPhone().isBlank() && !ValidationUtil.phone(u.getPhone()))
            throw new IllegalArgumentException("Invalid phone number");
        return dao.updateProfile(u);
    }

    public boolean updateAvatar(int userId, String avatarUrl) throws SQLException {
        if (!ValidationUtil.notEmpty(avatarUrl))
            throw new IllegalArgumentException("Avatar URL is required");
        return dao.updateAvatar(userId, avatarUrl);
    }

    /** Verify current password then set the new one. */
    public boolean changePassword(int userId, String current, String next) throws SQLException {
        if (!ValidationUtil.password(next))
            throw new IllegalArgumentException("New password must be at least 8 characters");
        String hash = dao.getPasswordHash(userId);
        if (hash == null) throw new IllegalArgumentException("User not found");
        if (!PasswordUtil.verify(current, hash))
            throw new IllegalArgumentException("Current password is incorrect");
        return dao.updatePassword(userId, PasswordUtil.hash(next));
    }
}
