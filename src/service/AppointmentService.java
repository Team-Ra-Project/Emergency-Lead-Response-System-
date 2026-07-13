package service;

import dao.AppointmentDAO;
import model.Appointment;
import utils.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class AppointmentService {

    private final AppointmentDAO dao = new AppointmentDAO();
    private static final Set<String> STATUSES =
        Set.of("Booked", "Pending", "Rescheduled", "Completed", "Cancelled");

    public List<Appointment> listAll() throws SQLException { return dao.findAll(); }

    public List<Appointment> search(String q, String status, String date) throws SQLException {
        return dao.search(q, status, date);
    }

    public Appointment get(int id) throws SQLException { return dao.findById(id); }

    public int create(Appointment a) throws SQLException {
        validate(a);
        if (a.getStatus() == null || a.getStatus().isBlank()) a.setStatus("Booked");
        return dao.insert(a);
    }

    public boolean update(Appointment a) throws SQLException {
        if (a.getAppointmentId() <= 0) throw new IllegalArgumentException("appointmentId is required");
        validate(a);
        return dao.update(a);
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        if (status == null || !STATUSES.contains(status))
            throw new IllegalArgumentException("Invalid status: " + status);
        return dao.updateStatus(id, status);
    }

    public boolean delete(int id) throws SQLException { return dao.delete(id); }

    private void validate(Appointment a) {
        if (!ValidationUtil.notEmpty(a.getCustomerName()))
            throw new IllegalArgumentException("Customer name is required");
        if (a.getAppointmentDate() == null)
            throw new IllegalArgumentException("Appointment date is required");
        if (a.getStatus() != null && !a.getStatus().isBlank() && !STATUSES.contains(a.getStatus()))
            throw new IllegalArgumentException("Invalid status: " + a.getStatus());
    }
}
