package controller;

import dao.RoleDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Role;
import utils.JsonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/roles")
public class RoleServlet extends HttpServlet {
    private final RoleDAO dao = new RoleDAO();
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Map<String,Object>> out = new ArrayList<>();
            for (Role r : dao.findAll()) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("roleId", r.getRoleId());
                m.put("roleName", r.getRoleName());
                m.put("description", r.getDescription());
                out.add(m);
            }
            JsonUtil.ok(resp, out);
        } catch (Exception e) { JsonUtil.error(resp,500,e.getMessage()); }
    }
}