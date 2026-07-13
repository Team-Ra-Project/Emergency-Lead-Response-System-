package service;

import dao.CustomerDAO;
import model.Customer;
import utils.ValidationUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class CustomerService {

    private final CustomerDAO dao = new CustomerDAO();
    private static final Set<String> STATUSES = Set.of("Active", "Inactive");

    public List<Customer> listAll() throws SQLException { return dao.findAll(); }

    public List<Customer> search(String q, String type, String status) throws SQLException {
        return dao.search(q, type, status);
    }

    public Customer get(int id) throws SQLException { return dao.findById(id); }

    public int create(Customer c) throws SQLException {
        validate(c);
        if (c.getStatus() == null || c.getStatus().isBlank()) c.setStatus("Active");
        return dao.insert(c);
    }

    public boolean update(Customer c) throws SQLException {
        if (c.getCustomerId() <= 0) throw new IllegalArgumentException("customerId is required");
        validate(c);
        return dao.update(c);
    }

    public boolean delete(int id) throws SQLException { return dao.delete(id); }

    private void validate(Customer c) {
        if (!ValidationUtil.notEmpty(c.getFullName())) throw new IllegalArgumentException("Customer name is required");
        if (!ValidationUtil.phone(c.getPhone()))       throw new IllegalArgumentException("Valid phone is required");
        if (c.getEmail() != null && !c.getEmail().isBlank() && !ValidationUtil.email(c.getEmail()))
            throw new IllegalArgumentException("Invalid email address");
        if (c.getStatus() != null && !c.getStatus().isBlank() && !STATUSES.contains(c.getStatus()))
            throw new IllegalArgumentException("Invalid status: " + c.getStatus());
    }
}
