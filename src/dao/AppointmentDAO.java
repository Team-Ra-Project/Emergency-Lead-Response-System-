package dao;

import model.Appointment;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    private static final String SELECT =
        "SELECT appointment_id, customer_id, customer_name, staff_id, staff_name, service_id, service_name, " +
        "       appointment_date, appointment_time, duration, status, location, notes, created_at, updated_at " +
        "FROM appointments";

    private Appointment map(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        int cid = rs.getInt("customer_id"); a.setCustomerId(rs.wasNull() ? null : cid);
        a.setCustomerName(rs.getString("customer_name"));
        int sid = rs.getInt("staff_id");    a.setStaffId(rs.wasNull() ? null : sid);
        a.setStaffName(rs.getString("staff_name"));
        int svc = rs.getInt("service_id");  a.setServiceId(rs.wasNull() ? null : svc);
        a.setServiceName(rs.getString("service_name"));
        Date d = rs.getDate("appointment_date"); if (d != null) a.setAppointmentDate(d.toLocalDate());
        Time t = rs.getTime("appointment_time"); if (t != null) a.setAppointmentTime(t.toLocalTime());
        a.setDuration(rs.getString("duration"));
        a.setStatus(rs.getString("status"));
        a.setLocation(rs.getString("location"));
        a.setNotes(rs.getString("notes"));
        Timestamp c = rs.getTimestamp("created_at"); if (c != null) a.setCreatedAt(c.toLocalDateTime());
        Timestamp u = rs.getTimestamp("updated_at"); if (u != null) a.setUpdatedAt(u.toLocalDateTime());
        return a;
    }

    public Appointment findById(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE appointment_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public List<Appointment> findAll() throws SQLException {
        List<Appointment> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(SELECT + " ORDER BY appointment_date DESC, appointment_time DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Search text + optional status + optional exact date (yyyy-MM-dd). */
    public List<Appointment> search(String q, String status, String date) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT).append(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" AND (customer_name LIKE ? OR staff_name LIKE ? OR service_name LIKE ? OR location LIKE ?)");
            String like = "%" + q.trim() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }
        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("All")) {
            sql.append(" AND status=?"); params.add(status);
        }
        if (date != null && !date.isBlank()) {
            sql.append(" AND appointment_date=?"); params.add(Date.valueOf(date));
        }
        sql.append(" ORDER BY appointment_date DESC, appointment_time DESC");

        List<Appointment> list = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public int insert(Appointment a) throws SQLException {
        String sql = "INSERT INTO appointments(customer_id,customer_name,staff_id,staff_name,service_id,service_name," +
                     "appointment_date,appointment_time,duration,status,location,notes) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (a.getCustomerId() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, a.getCustomerId());
            ps.setString(2, a.getCustomerName());
            if (a.getStaffId() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, a.getStaffId());
            ps.setString(4, a.getStaffName());
            if (a.getServiceId() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, a.getServiceId());
            ps.setString(6, a.getServiceName());
            ps.setDate(7, a.getAppointmentDate() == null ? null : Date.valueOf(a.getAppointmentDate()));
            ps.setTime(8, a.getAppointmentTime() == null ? null : Time.valueOf(a.getAppointmentTime()));
            ps.setString(9, a.getDuration());
            ps.setString(10, a.getStatus() == null ? "Booked" : a.getStatus());
            ps.setString(11, a.getLocation());
            ps.setString(12, a.getNotes());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) { int id = keys.getInt(1); a.setAppointmentId(id); return id; }
            }
        }
        return -1;
    }

    public boolean update(Appointment a) throws SQLException {
        String sql = "UPDATE appointments SET customer_id=?,customer_name=?,staff_id=?,staff_name=?,service_id=?," +
                     "service_name=?,appointment_date=?,appointment_time=?,duration=?,status=?,location=?,notes=? " +
                     "WHERE appointment_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (a.getCustomerId() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, a.getCustomerId());
            ps.setString(2, a.getCustomerName());
            if (a.getStaffId() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, a.getStaffId());
            ps.setString(4, a.getStaffName());
            if (a.getServiceId() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, a.getServiceId());
            ps.setString(6, a.getServiceName());
            ps.setDate(7, a.getAppointmentDate() == null ? null : Date.valueOf(a.getAppointmentDate()));
            ps.setTime(8, a.getAppointmentTime() == null ? null : Time.valueOf(a.getAppointmentTime()));
            ps.setString(9, a.getDuration());
            ps.setString(10, a.getStatus());
            ps.setString(11, a.getLocation());
            ps.setString(12, a.getNotes());
            ps.setInt(13, a.getAppointmentId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE appointments SET status=? WHERE appointment_id=?")) {
            ps.setString(1, status); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM appointments WHERE appointment_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
