package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Appointment;
import service.AppointmentService;
import utils.JsonUtil;
import utils.RequestUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for the Appointment module.
 *
 *   GET    /api/appointments             -> list (?q=&status=&date=)
 *   GET    /api/appointments/{id}        -> single
 *   POST   /api/appointments             -> create
 *   PUT    /api/appointments/{id}        -> update
 *   PUT    /api/appointments/{id}/status -> quick status change
 *   DELETE /api/appointments/{id}        -> delete
 */
@WebServlet(urlPatterns = {"/api/appointments", "/api/appointments/*"})
public class AppointmentServlet extends HttpServlet {

    private final AppointmentService svc = new AppointmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = RequestUtil.pathId(req);
            if (id != null) {
                Appointment a = svc.get(id);
                if (a == null) { JsonUtil.error(resp, 404, "Appointment not found"); return; }
                JsonUtil.ok(resp, toMap(a));
                return;
            }
            String q = req.getParameter("q");
            String status = req.getParameter("status");
            String date = req.getParameter("date");
            List<Appointment> rows = (q == null && status == null && date == null)
                    ? svc.listAll() : svc.search(q, status, date);
            List<Map<String,Object>> out = new ArrayList<>();
            for (Appointment a : rows) out.add(toMap(a));
            JsonUtil.ok(resp, out);
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Appointment a = fromBody(new Appointment(), RequestUtil.readJson(req));
            int id = svc.create(a);
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("appointmentId", id);
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
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) { JsonUtil.error(resp, 400, "Appointment id required"); return; }
            String[] parts = path.substring(1).split("/");
            int id;
            try { id = Integer.parseInt(parts[0]); }
            catch (NumberFormatException nfe) { JsonUtil.error(resp, 400, "Invalid appointment id"); return; }

            Map<String,String> b = RequestUtil.readJson(req);

            if (parts.length == 2 && "status".equalsIgnoreCase(parts[1])) {
                String s = b.get("status");
                if (s == null || s.isBlank()) { JsonUtil.error(resp, 400, "status is required"); return; }
                svc.updateStatus(id, s);
                JsonUtil.ok(resp, "Status updated");
                return;
            }

            Appointment existing = svc.get(id);
            if (existing == null) { JsonUtil.error(resp, 404, "Appointment not found"); return; }
            Appointment merged = fromBody(existing, b);
            merged.setAppointmentId(id);
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
            if (id == null) { JsonUtil.error(resp, 400, "Appointment id required"); return; }
            svc.delete(id);
            JsonUtil.ok(resp, "Deleted");
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    /* ---------- helpers ---------- */

    private Appointment fromBody(Appointment a, Map<String,String> b) {
        if (b.get("customerId")   != null) a.setCustomerId(parseIntOrNull(b.get("customerId")));
        if (b.get("customerName") != null) a.setCustomerName(b.get("customerName"));
        if (b.get("staffId")      != null) a.setStaffId(parseIntOrNull(b.get("staffId")));
        if (b.get("staffName")    != null) a.setStaffName(b.get("staffName"));
        if (b.get("assignedStaff")!= null) a.setStaffName(b.get("assignedStaff"));
        if (b.get("serviceId")    != null) a.setServiceId(parseIntOrNull(b.get("serviceId")));
        if (b.get("serviceName")  != null) a.setServiceName(b.get("serviceName"));
        if (b.get("appointmentDate") != null) a.setAppointmentDate(parseDate(b.get("appointmentDate")));
        if (b.get("date")            != null) a.setAppointmentDate(parseDate(b.get("date")));
        if (b.get("appointmentTime") != null) a.setAppointmentTime(parseTime(b.get("appointmentTime")));
        if (b.get("time")            != null) a.setAppointmentTime(parseTime(b.get("time")));
        if (b.get("duration")     != null) a.setDuration(b.get("duration"));
        if (b.get("status")       != null) a.setStatus(b.get("status"));
        if (b.get("location")     != null) a.setLocation(b.get("location"));
        if (b.get("notes")           != null) a.setNotes(b.get("notes"));
        if (b.get("appointmentNote") != null) a.setNotes(b.get("appointmentNote"));
        return a;
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank() || "null".equalsIgnoreCase(s)) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim()); } catch (Exception e) { return null; }
    }

    private LocalTime parseTime(String s) {
        if (s == null || s.isBlank()) return null;
        String v = s.trim();
        try { return LocalTime.parse(v.length() == 5 ? v : v.substring(0, 5)); }
        catch (Exception e) { return null; }
    }

    private Map<String,Object> toMap(Appointment a) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("appointmentId", a.getAppointmentId());
        m.put("customerId",    a.getCustomerId());
        m.put("customerName",  a.getCustomerName());
        m.put("staffId",       a.getStaffId());
        m.put("staffName",     a.getStaffName());
        m.put("serviceId",     a.getServiceId());
        m.put("serviceName",   a.getServiceName());
        m.put("appointmentDate", a.getAppointmentDate() == null ? null : a.getAppointmentDate().toString());
        m.put("appointmentTime", a.getAppointmentTime() == null ? null : a.getAppointmentTime().toString());
        m.put("duration",      a.getDuration());
        m.put("status",        a.getStatus());
        m.put("location",      a.getLocation());
        m.put("notes",         a.getNotes());
        m.put("createdAt",     a.getCreatedAt() == null ? null : a.getCreatedAt().toString());
        m.put("updatedAt",     a.getUpdatedAt() == null ? null : a.getUpdatedAt().toString());
        return m;
    }
}
