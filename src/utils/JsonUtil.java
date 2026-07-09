package utils;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON writer — avoids pulling in Gson/Jackson.
 * Supports: Map, List, String, Number, Boolean, null.
 */
public class JsonUtil {

    public static void write(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        PrintWriter out = resp.getWriter();
        out.write(toJson(body));
        out.flush();
    }

    public static void ok(HttpServletResponse resp, Object body) throws IOException {
        write(resp, 200, Map.of("success", true, "data", body == null ? "" : body));
    }
    public static void error(HttpServletResponse resp, int status, String msg) throws IOException {
        write(resp, status, Map.of("success", false, "message", msg));
    }

    public static String toJson(Object o) {
        StringBuilder sb = new StringBuilder();
        append(sb, o);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void append(StringBuilder sb, Object o) {
        if (o == null)               { sb.append("null"); return; }
        if (o instanceof Boolean)    { sb.append(o); return; }
        if (o instanceof Number)     { sb.append(o); return; }
        if (o instanceof Map)        { appendMap(sb, (Map<String,Object>) o); return; }
        if (o instanceof List)       { appendList(sb, (List<Object>) o); return; }
        appendStr(sb, o.toString());
    }
    private static void appendMap(StringBuilder sb, Map<String,Object> m) {
        sb.append('{'); boolean first = true;
        for (Map.Entry<String,Object> e : m.entrySet()) {
            if (!first) sb.append(','); first = false;
            appendStr(sb, e.getKey()); sb.append(':'); append(sb, e.getValue());
        }
        sb.append('}');
    }
    private static void appendList(StringBuilder sb, List<Object> l) {
        sb.append('['); boolean first = true;
        for (Object v : l) {
            if (!first) sb.append(','); first = false;
            append(sb, v);
        }
        sb.append(']');
    }
    private static void appendStr(StringBuilder sb, String s) {
        sb.append('"');
        for (int i=0;i<s.length();i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int)c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }
}