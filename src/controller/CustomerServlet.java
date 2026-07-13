package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Customer;
import service.CustomerService;
import utils.JsonUtil;
import utils.RequestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for the Customer Management module.
 *
 *   GET    /api/customers            -> list (?q=&type=&status=)
 *   GET    /api/customers/{id}       -> single
 *   POST   /api/customers            -> create
 *   PUT    /api/customers/{id}       -> update
 *   DELETE /api/customers/{id}       -> delete
 */
@WebServlet(urlPatterns = {"/api/customers", "/api/customers/*"})
public class CustomerServlet extends HttpServlet {

    private final CustomerService svc = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = RequestUtil.pathId(req);
            if (id != null) {
                Customer c = svc.get(id);
                if (c == null) { JsonUtil.error(resp, 404, "Customer not found"); return; }
                JsonUtil.ok(resp, toMap(c));
                return;
            }
            String q = req.getParameter("q");
            String type = req.getParameter("type");
            String status = req.getParameter("status");
            List<Customer> rows = (q == null && type == null && status == null)
                    ? svc.listAll() : svc.search(q, type, status);
            List<Map<String,Object>> out = new ArrayList<>();
            for (Customer c : rows) out.add(toMap(c));
            JsonUtil.ok(resp, out);
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Customer c = fromBody(new Customer(), RequestUtil.readJson(req));
            int id = svc.create(c);
            Map<String,Object> out = new LinkedHashMap<>();
            out.put("customerId", id);
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
            if (id == null) { JsonUtil.error(resp, 400, "Customer id required"); return; }
            Customer existing = svc.get(id);
            if (existing == null) { JsonUtil.error(resp, 404, "Customer not found"); return; }
            Customer merged = fromBody(existing, RequestUtil.readJson(req));
            merged.setCustomerId(id);
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
            if (id == null) { JsonUtil.error(resp, 400, "Customer id required"); return; }
            svc.delete(id);
            JsonUtil.ok(resp, "Deleted");
        } catch (Exception e) {
            JsonUtil.error(resp, 500, e.getMessage());
        }
    }

    /* ---------- helpers ---------- */

    private Customer fromBody(Customer c, Map<String,String> b) {
        if (b.get("fullName")     != null) c.setFullName(b.get("fullName"));
        if (b.get("customerName") != null) c.setFullName(b.get("customerName"));
        if (b.get("phone")        != null) c.setPhone(b.get("phone"));
        if (b.get("email")        != null) c.setEmail(b.get("email"));
        if (b.get("address")      != null) c.setAddress(b.get("address"));
        if (b.get("city")         != null) c.setCity(b.get("city"));
        if (b.get("state")        != null) c.setState(b.get("state"));
        if (b.get("pincode")      != null) c.setPincode(b.get("pincode"));
        if (b.get("customerType") != null) c.setCustomerType(b.get("customerType"));
        if (b.get("type")         != null) c.setCustomerType(b.get("type"));
        if (b.get("status")       != null) c.setStatus(b.get("status"));
        if (b.get("notes")        != null) c.setNotes(b.get("notes"));
        return c;
    }

    private Map<String,Object> toMap(Customer c) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("customerId",   c.getCustomerId());
        m.put("fullName",     c.getFullName());
        m.put("phone",        c.getPhone());
        m.put("email",        c.getEmail());
        m.put("address",      c.getAddress());
        m.put("city",         c.getCity());
        m.put("state",        c.getState());
        m.put("pincode",      c.getPincode());
        m.put("customerType", c.getCustomerType());
        m.put("status",       c.getStatus());
        m.put("notes",        c.getNotes());
        m.put("createdAt",    c.getCreatedAt() == null ? null : c.getCreatedAt().toString());
        return m;
    }
}
