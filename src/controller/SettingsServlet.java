package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.SettingsService;
import utils.JsonUtil;
import utils.RequestUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Settings module (business details, working hours, WhatsApp/SMTP/SMS, logo, theme).
 * Stored as flexible key/value pairs.
 *
 *   GET  /api/settings   -> all settings as a flat object
 *   PUT  /api/settings   -> upsert any subset of keys sent in the JSON body
 */
@WebServlet(urlPatterns = {"/api/settings", "/api/settings/*"})
public class SettingsServlet extends HttpServlet {

    private final SettingsService svc = new SettingsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String,String> all = svc.getAll();
            JsonUtil.ok(resp, new LinkedHashMap<Object,Object>(all));
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String,String> body = RequestUtil.readJson(req);
            if (body.isEmpty()) { JsonUtil.error(resp, 400, "No settings provided"); return; }
            svc.saveAll(body);
            JsonUtil.ok(resp, "Settings saved");
        } catch (IllegalArgumentException iae) {
            JsonUtil.error(resp, 400, iae.getMessage());
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    // Allow POST as an alias for saving (some forms submit via POST).
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doPut(req, resp);
    }
}
