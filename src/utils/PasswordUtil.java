package utils;

import org.mindrot.jbcrypt.BCrypt;

/** BCrypt password hashing. Requires jbcrypt-0.4.jar in WEB-INF/lib. */
public class PasswordUtil {
    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }
    public static boolean verify(String plain, String hash) {
        if (plain == null || hash == null) return false;
        try { return BCrypt.checkpw(plain, hash); }
        catch (Exception e) { return false; }
    }
}