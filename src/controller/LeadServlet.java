package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Lead;
import service.LeadService;
import utils.JsonUtil;
import utils.RequestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for the Lead Management module.
 *
 *   GET    /api/leads                       -> list (supports ?q=&status=&priority=)
 *   GET    /api/leads/{id}                  -> single lead
 *   POST   /api/leads                       -> create
 *   PUT    /api/leads/{id}                  -> full update
 *   PUT    /api/leads/{id}/status           -> quick status change
 *   PUT    /api/leads/{id}/assign           -> assign staff
 *   DELETE /api/leads/{id}                  -> delete
 */
@WebServlet(urlPatterns = {"/api/leads", "/api/leads/*"})
public class LeadServlet extends HttpServlet {

    private final LeadService svc = new LeadService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = RequestUtil.pathId(req);
            if (id != null) {
                Lead l = svc.get(id);
                if (l == null) { JsonUtil.error(resp, 404, "Lead not found"); return; }
                JsonUtil.ok(resp, toMap(l));
                return;
            }
            String q        = req.getParameter("q");
            String status   = req.getParameter("status");
            String priority = req.getParameter("priority");

            List<Lead> rows = (q == null && status == null && priority == null)
                    ? svc.listAll()
                    : svc.search(q, status, priority);

            List<Map<String,Object>> out = new ArrayList<>();
            for (Lead l : rows) out.add(toMap(l));
            JsonUtil.ok(resp, out);
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Map<String,String> b = RequestUtil.readJson(req);
            Lead l = fromBody(new Lead(), b);
            // If no existing customer was chosen, create one from the inline form fields.
            if (l.getCustomerId() <= 0) {
                String name = firstNonBlank(b.get("customerName"), b.get("name"));
                if (name == null) { JsonUtil.error(resp, 400, "customerId or customer name is required"); return; }
                model.Customer c = new model.Customer();
                c.setFullName(name);
                c.setPhone(firstNonBlank(b.get("mobileNumber"), b.get("phone")));
                c.setEmail(b.get("email"));
                c.setAddress(b.get("address"));
                int cid = new dao.CustomerDAO().insert(c);
                l.setCustomerId(cid);
            }
            // Normalise UI labels ("In Progress"/"High") to backend enums.
            if (l.getStatus() != null)   l.setStatus(l.getStatus().trim().toUpperCase().replace(' ', '_'));
            if (l.getPriority() != null) l.setPriority(mapPriority(l.getPriority()));
            int id = svc.create(l);
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("leadId", id);
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
            String path = req.getPathInfo(); // e.g. /12  or /12/status  or /12/assign
            if (path == null || path.equals("/")) { JsonUtil.error(resp, 400, "Lead id required"); return; }
            String[] parts = path.substring(1).split("/");
            int id;
            try { id = Integer.parseInt(parts[0]); }
            catch (NumberFormatException nfe) { JsonUtil.error(resp, 400, "Invalid lead id"); return; }

            Map<String,String> b = RequestUtil.readJson(req);

            if (parts.length == 2 && "status".equalsIgnoreCase(parts[1])) {
                String s = b.get("status");
                if (s == null || s.isBlank()) { JsonUtil.error(resp, 400, "status is required"); return; }
                svc.updateStatus(id, s);
                JsonUtil.ok(resp, "Status updated");
                return;
            }
            if (parts.length == 2 && "assign".equalsIgnoreCase(parts[1])) {
                Integer staffId = parseIntOrNull(b.get("staffId"));
                svc.assign(id, staffId);
                JsonUtil.ok(resp, "Assignment updated");
                return;
            }

            Lead existing = svc.get(id);
            if (existing == null) { JsonUtil.error(resp, 404, "Lead not found"); return; }
            Lead merged = fromBody(existing, b);
            merged.setLeadId(id);
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
            if (id == null) { JsonUtil.error(resp, 400, "Lead id required"); return; }
            svc.delete(id);
            JsonUtil.ok(resp, "Deleted");
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    /* ---------- helpers ---------- */

    private Lead fromBody(Lead l, Map<String,String> b) {
        if (b.get("customerId")     != null) l.setCustomerId(Integer.parseInt(b.get("customerId")));
        if (b.get("serviceId")      != null) l.setServiceId(parseIntOrNull(b.get("serviceId")));
        if (b.get("priority")       != null) l.setPriority(b.get("priority"));
        if (b.get("status")         != null) l.setStatus(b.get("status"));
        if (b.get("assignedStaff")  != null) l.setAssignedStaff(parseIntOrNull(b.get("assignedStaff")));
        if (b.get("notes")          != null) l.setNotes(b.get("notes"));
        if (b.get("source")         != null) l.setSource(b.get("source"));
        if (b.get("leadSource")     != null) l.setSource(b.get("leadSource"));
        if (b.get("description")    != null) l.setNotes(b.get("description"));
        return l;
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank() || "null".equalsIgnoreCase(s)) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Map<String,Object> toMap(Lead l) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("leadId",         l.getLeadId());
        m.put("customerId",     l.getCustomerId());
        m.put("customerName",   l.getCustomerName());
        m.put("customerPhone",  l.getCustomerPhone());
        m.put("customerEmail",  l.getCustomerEmail());
        m.put("serviceId",      l.getServiceId());
        m.put("serviceName",    l.getServiceName());
        m.put("priority",       l.getPriority());
        m.put("status",         l.getStatus());
        m.put("assignedStaff",  l.getAssignedStaff());
        m.put("assignedStaffName", l.getAssignedStaffName());
        m.put("notes",          l.getNotes());
        m.put("source",         l.getSource());
        m.put("createdAt",      l.getCreatedAt() == null ? null : l.getCreatedAt().toString());
        m.put("updatedAt",      l.getUpdatedAt() == null ? null : l.getUpdatedAt().toString());
        return m;
    }

    private String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v.trim();
        return null;
    }

    private String mapPriority(String p) {
        String v = p.trim().toUpperCase();
        if (v.equals("CRITICAL")) return "EMERGENCY";
        return v;
    }
}
