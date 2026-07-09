package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import service.UserService;
import utils.JsonUtil;
import utils.RequestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/users", "/api/users/*"})
public class UserServlet extends HttpServlet {
    private final UserService svc = new UserService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = RequestUtil.pathId(req);
            if (id != null) {
                User u = svc.get(id);
                if (u == null) { JsonUtil.error(resp,404,"Not found"); return; }
                JsonUtil.ok(resp, toMap(u));
                return;
            }
            List<Map<String,Object>> out = new ArrayList<>();
            for (User u : svc.listAll()) out.add(toMap(u));
            JsonUtil.ok(resp, out);
        } catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }

    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = RequestUtil.pathId(req);
            if (id == null) { JsonUtil.error(resp,400,"User id required"); return; }
            Map<String,String> b = RequestUtil.readJson(req);
            User u = svc.get(id);
            if (u == null) { JsonUtil.error(resp,404,"Not found"); return; }
            if (b.get("fullName") != null) u.setFullName(b.get("fullName"));
            if (b.get("phone") != null)    u.setPhone(b.get("phone"));
            if (b.get("roleId") != null)   u.setRoleId(Integer.parseInt(b.get("roleId")));
            if (b.get("status") != null)   u.setStatus(b.get("status"));
            svc.update(u);
            JsonUtil.ok(resp, "Updated");
        } catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }

    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer id = RequestUtil.pathId(req);
            if (id == null) { JsonUtil.error(resp,400,"User id required"); return; }
            svc.delete(id);
            JsonUtil.ok(resp, "Deleted");
        } catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }

    private Map<String,Object> toMap(User u) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("userId", u.getUserId());
        m.put("fullName", u.getFullName());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("role", u.getRoleName());
        m.put("status", u.getStatus());
        return m;
    }
}