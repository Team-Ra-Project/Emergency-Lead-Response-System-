package utils;

import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Tiny JSON body parser (flat key/value strings) — sufficient for our forms.
 * For nested payloads, upgrade to Jackson/Gson.
 */
public class RequestUtil {

    public static Map<String,String> readJson(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = req.getReader()) {
            String l; while ((l = r.readLine()) != null) sb.append(l);
        }
        return parseFlat(sb.toString());
    }

    static Map<String,String> parseFlat(String s) {
        Map<String,String> out = new HashMap<>();
        if (s == null) return out;
        s = s.trim();
        if (s.isEmpty() || !s.startsWith("{")) return out;
        s = s.substring(1, s.endsWith("}") ? s.length() - 1 : s.length());

        int i = 0, n = s.length();
        while (i < n) {
            while (i < n && Character.isWhitespace(s.charAt(i))) i++;
            if (i >= n) break;
            if (s.charAt(i) != '"') { i++; continue; }
            i++;
            StringBuilder k = new StringBuilder();
            while (i < n && s.charAt(i) != '"') {
                if (s.charAt(i) == '\\' && i+1 < n) { k.append(unesc(s.charAt(++i))); }
                else k.append(s.charAt(i));
                i++;
            }
            i++; // closing "
            while (i < n && s.charAt(i) != ':') i++;
            i++;
            while (i < n && Character.isWhitespace(s.charAt(i))) i++;
            StringBuilder v = new StringBuilder();
            if (i < n && s.charAt(i) == '"') {
                i++;
                while (i < n && s.charAt(i) != '"') {
                    if (s.charAt(i) == '\\' && i+1 < n) v.append(unesc(s.charAt(++i)));
                    else v.append(s.charAt(i));
                    i++;
                }
                i++;
            } else {
                while (i < n && s.charAt(i) != ',' && s.charAt(i) != '}') { v.append(s.charAt(i)); i++; }
            }
            out.put(k.toString(), v.toString().trim());
            while (i < n && s.charAt(i) != ',') i++;
            i++;
        }
        return out;
    }

    private static char unesc(char c) {
        switch (c) {
            case 'n': return '\n';
            case 't': return '\t';
            case 'r': return '\r';
            case '"': return '"';
            case '\\': return '\\';
            case '/': return '/';
            default: return c;
        }
    }

    public static int intParam(HttpServletRequest r, String name, int def) {
        try { return Integer.parseInt(r.getParameter(name)); } catch (Exception e) { return def; }
    }

    /** Extract trailing id from a URL like /api/staff/12  -> 12 */
    public static Integer pathId(HttpServletRequest r) {
        String p = r.getPathInfo();
        if (p == null || p.equals("/")) return null;
        try { return Integer.parseInt(p.substring(1)); } catch (Exception e) { return null; }
    }
}