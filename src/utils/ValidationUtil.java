package utils;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL =
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\-\\s]{7,20}$");

    public static boolean notEmpty(String s) { return s != null && !s.trim().isEmpty(); }
    public static boolean email(String s)    { return notEmpty(s) && EMAIL.matcher(s).matches(); }
    public static boolean phone(String s)    { return notEmpty(s) && PHONE.matcher(s).matches(); }
    public static boolean password(String s) { return notEmpty(s) && s.length() >= 8; }

    /** Simple HTML-escape to help mitigate XSS when echoing user data. */
    public static String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }
}