package service;

import dao.ServiceDAO;
import model.Service;
import utils.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class ServiceService {

    private final ServiceDAO dao = new ServiceDAO();
    private static final Set<String> STATUSES =
        Set.of("Active", "Inactive", "Temporarily Unavailable");

    public List<Service> listAll() throws SQLException { return dao.findAll(); }

    public List<Service> search(String q, String category, String status) throws SQLException {
        return dao.search(q, category, status);
    }

    public Service get(int id) throws SQLException { return dao.findById(id); }

    public int create(Service s) throws SQLException {
        validate(s);
        if (s.getStatus() == null || s.getStatus().isBlank()) s.setStatus("Active");
        return dao.insert(s);
    }

    public boolean update(Service s) throws SQLException {
        if (s.getServiceId() <= 0) throw new IllegalArgumentException("serviceId is required");
        validate(s);
        return dao.update(s);
    }

    public boolean delete(int id) throws SQLException { return dao.delete(id); }

    private void validate(Service s) {
        if (!ValidationUtil.notEmpty(s.getName())) throw new IllegalArgumentException("Service name is required");
        if (s.getPrice() != null && s.getPrice().signum() < 0)
            throw new IllegalArgumentException("Price cannot be negative");
        if (s.getStatus() != null && !s.getStatus().isBlank() && !STATUSES.contains(s.getStatus()))
            throw new IllegalArgumentException("Invalid status: " + s.getStatus());
    }
}
