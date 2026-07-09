package dao;

import model.Role;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoleDAO {
    public List<Role> findAll() throws SQLException {
        List<Role> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM roles ORDER BY role_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Role r = new Role();
                r.setRoleId(rs.getInt("role_id"));
                r.setRoleName(rs.getString("role_name"));
                r.setDescription(rs.getString("description"));
                list.add(r);
            }
        }
        return list;
    }

    public Integer findIdByName(String name) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT role_id FROM roles WHERE role_name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : null; }
        }
    }
}