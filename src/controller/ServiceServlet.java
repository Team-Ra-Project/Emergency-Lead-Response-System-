package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Service;
import service.ServiceService;
import utils.JsonUtil;
import utils.RequestUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for the Services module.
 *
 *   GET    /api/services            -> list (?q=&category=&status=)
 *   GET    /api/services/{id}       -> single
 *   POST   /api/services            -> create
 *   PUT    /api/services/{id}       -> update
 *   DELETE /api/services/{id}       -> delete
 */
@WebServlet(urlPatterns = {"/api/services", "/api/services/*"})
public class ServiceServlet extends HttpServlet {

    private final ServiceService svc = new ServiceService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = RequestUtil.pathId(req);
            if (id != null) {
                Service s = svc.get(id);
                if (s == null) { JsonUtil.error(resp, 404, "Service not found"); return; }
                JsonUtil.ok(resp, toMap(s));
                return;
            }
            String q = req.getParameter("q");
            String category = req.getParameter("category");
            String status = req.getParameter("status");
            List<Service> rows = (q == null && category == null && status == null)
                    ? svc.listAll() : svc.search(q, category, status);
            List<Map<String,Object>> out = new ArrayList<>();
            for (Service s : rows) out.add(toMap(s));
            JsonUtil.ok(resp, out);
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Service s = fromBody(new Service(), RequestUtil.readJson(req));
            int id = svc.create(s);
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("serviceId", id);
            JsonUtil.write(resp, 201, Map.of("success", true, "data", out));
        } catch (IllegalArgumentException iae) {
            JsonUtil.error(resp, 400, iae.getMessage());
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = RequestUtil.pathId(req);
            if (id == null) { JsonUtil.error(resp, 400, "Service id required"); return; }
            Service existing = svc.get(id);
            if (existing == null) { JsonUtil.error(resp, 404, "Service not found"); return; }
            Service merged = fromBody(existing, RequestUtil.readJson(req));
            merged.setServiceId(id);
            svc.update(merged);
            JsonUtil.ok(resp, "Updated");
        } catch (IllegalArgumentException iae) {
            JsonUtil.error(resp, 400, iae.getMessage());
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = RequestUtil.pathId(req);
            if (id == null) { JsonUtil.error(resp, 400, "Service id required"); return; }
            svc.delete(id);
            JsonUtil.ok(resp, "Deleted");
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    /* ---------- helpers ---------- */

    private Service fromBody(Service s, Map<String,String> b) {
        if (b.get("name")        != null) s.setName(b.get("name"));
        if (b.get("serviceName") != null) s.setName(b.get("serviceName"));
        if (b.get("category")    != null) s.setCategory(b.get("category"));
        if (b.get("description") != null) s.setDescription(b.get("description"));
        if (b.get("price")       != null) s.setPrice(parseDecimal(b.get("price")));
        if (b.get("duration")    != null) s.setDuration(b.get("duration"));
        if (b.get("status")      != null) s.setStatus(b.get("status"));
        return s;
    }

    private BigDecimal parseDecimal(String v) {
        if (v == null || v.isBlank()) return null;
        try { return new BigDecimal(v.replaceAll("[^0-9.]", "")); }
        catch (NumberFormatException e) { return null; }
    }

    private Map<String,Object> toMap(Service s) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("serviceId",   s.getServiceId());
        m.put("name",        s.getName());
        m.put("category",    s.getCategory());
        m.put("description", s.getDescription());
        m.put("price",       s.getPrice() == null ? null : s.getPrice().toPlainString());
        m.put("duration",    s.getDuration());
        m.put("status",      s.getStatus());
        m.put("createdAt",   s.getCreatedAt() == null ? null : s.getCreatedAt().toString());
        m.put("updatedAt",   s.getUpdatedAt() == null ? null : s.getUpdatedAt().toString());
        return m;
    }
}
