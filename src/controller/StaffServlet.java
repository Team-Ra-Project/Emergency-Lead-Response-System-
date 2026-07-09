package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Staff;
import service.StaffService;
import utils.JsonUtil;
import utils.RequestUtil;
import utils.ValidationUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/staff", "/api/staff/*"})
public class StaffServlet extends HttpServlet {
    private final StaffService svc = new StaffService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // /api/staff/performance
            String p = req.getPathInfo();
            if ("/performance".equals(p)) {
                List<Map<String,Object>> out = new ArrayList<>();
                for (Object[] r : svc.performance()) {
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("staffId", r[0]); m.put("name", r[1]);
                    m.put("assigned", r[2]); m.put("completed", r[3]);
                    out.add(m);
                }
                JsonUtil.ok(resp, out); return;
            }
            Integer id = RequestUtil.pathId(req);
            if (id != null) {
                Staff s = svc.get(id);
                if (s == null) { JsonUtil.error(resp,404,"Not found"); return; }
                JsonUtil.ok(resp, toMap(s)); return;
            }
            List<Map<String,Object>> out = new ArrayList<>();
            for (Staff s : svc.listAll()) out.add(toMap(s));
            JsonUtil.ok(resp, out);
        } catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String,String> b = RequestUtil.readJson(req);
            String name = b.get("fullName"), email = b.get("email"),
                   phone = b.get("phone"), pw = b.getOrDefault("password","Staff@123"),
                   desig = b.get("designation"), spec = b.get("specialization"),
                   avail = b.getOrDefault("availability","AVAILABLE");

            if (!ValidationUtil.notEmpty(name)) { JsonUtil.error(resp,400,"Name required"); return; }
            if (!ValidationUtil.email(email))   { JsonUtil.error(resp,400,"Valid email required"); return; }
            if (!ValidationUtil.phone(phone))   { JsonUtil.error(resp,400,"Valid phone required"); return; }

            int id = svc.add(name.trim(), email.trim().toLowerCase(), phone.trim(),
                             pw, desig, spec, avail);
            JsonUtil.ok(resp, Map.of("staffId", id));
        } catch (IllegalStateException e) { JsonUtil.error(resp,409,e.getMessage()); }
          catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }

    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = RequestUtil.pathId(req);
            if (id == null) { JsonUtil.error(resp,400,"Staff id required"); return; }
            Map<String,String> b = RequestUtil.readJson(req);
            svc.update(id, b.get("designation"), b.get("specialization"), b.get("availability"));
            JsonUtil.ok(resp, "Updated");
        } catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }

    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = RequestUtil.pathId(req);
            if (id == null) { JsonUtil.error(resp,400,"Staff id required"); return; }
            svc.delete(id);
            JsonUtil.ok(resp, "Deleted");
        } catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }

    private Map<String,Object> toMap(Staff s) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("staffId", s.getStaffId());
        m.put("userId", s.getUserId());
        m.put("fullName", s.getFullName());
        m.put("email", s.getEmail());
        m.put("phone", s.getPhone());
        m.put("designation", s.getDesignation());
        m.put("specialization", s.getSpecialization());
        m.put("availability", s.getAvailability());
        m.put("joinedOn", s.getJoinedOn() == null ? "" : s.getJoinedOn().toString());
        return m;
    }
}