package dao;

import model.Customer;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private static final String SELECT =
        "SELECT customer_id, full_name, phone, email, address, city, state, pincode, " +
        "       customer_type, status, notes, created_at FROM customers";

    private Customer map(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setFullName(rs.getString("full_name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        c.setState(rs.getString("state"));
        c.setPincode(rs.getString("pincode"));
        c.setCustomerType(rs.getString("customer_type"));
        c.setStatus(rs.getString("status"));
        c.setNotes(rs.getString("notes"));
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null) c.setCreatedAt(t.toLocalDateTime());
        return c;
    }

    public Customer findById(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE customer_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " ORDER BY customer_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Search by name/phone/email + optional type/status filter. */
    public List<Customer> search(String q, String type, String status) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" AND (full_name LIKE ? OR phone LIKE ? OR email LIKE ?)");
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like); params.add(like);
        }
        if (type != null && !type.isBlank() && !type.equalsIgnoreCase("All") && !type.equalsIgnoreCase("All Customers")) {
            sql.append(" AND customer_type=?"); params.add(type);
        }
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All")) {
            sql.append(" AND status=?"); params.add(status);
        }
        sql.append(" ORDER BY customer_id DESC");

        List<Customer> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public int insert(Customer x) throws SQLException {
        String sql = "INSERT INTO customers(full_name,phone,email,address,city,state,pincode,customer_type,status,notes) " +
                     "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, x.getFullName());
            ps.setString(2, x.getPhone());
            ps.setString(3, x.getEmail());
            ps.setString(4, x.getAddress());
            ps.setString(5, x.getCity());
            ps.setString(6, x.getState());
            ps.setString(7, x.getPincode());
            ps.setString(8, x.getCustomerType() == null ? "Individual" : x.getCustomerType());
            ps.setString(9, x.getStatus() == null ? "Active" : x.getStatus());
            ps.setString(10, x.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { int id = keys.getInt(1); x.setCustomerId(id); return id; }
            }
        }
        return -1;
    }

    public boolean update(Customer x) throws SQLException {
        String sql = "UPDATE customers SET full_name=?,phone=?,email=?,address=?,city=?,state=?,pincode=?," +
                     "customer_type=?,status=?,notes=? WHERE customer_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, x.getFullName());
            ps.setString(2, x.getPhone());
            ps.setString(3, x.getEmail());
            ps.setString(4, x.getAddress());
            ps.setString(5, x.getCity());
            ps.setString(6, x.getState());
            ps.setString(7, x.getPincode());
            ps.setString(8, x.getCustomerType());
            ps.setString(9, x.getStatus());
            ps.setString(10, x.getNotes());
            ps.setInt(11, x.getCustomerId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM customers WHERE customer_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
