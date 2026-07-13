package service;

import dao.LeadDAO;
import model.Lead;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class LeadService {

    private final LeadDAO dao = new LeadDAO();

    private static final Set<String> PRIORITIES = Set.of("LOW","MEDIUM","HIGH","EMERGENCY");
    private static final Set<String> STATUSES   = Set.of("NEW","CONTACTED","QUALIFIED","QUOTE_SENT","BOOKED","COMPLETED","LOST");

    public List<Lead> listAll() throws SQLException { return dao.findAll(); }

    public List<Lead> search(String q, String status, String priority) throws SQLException {
        return dao.search(q, status, priority);
    }

    public Lead get(int id) throws SQLException { return dao.findById(id); }

    public int create(Lead l) throws SQLException {
        if (l.getCustomerId() <= 0) throw new IllegalArgumentException("customerId is required");
        normalize(l);
        return dao.insert(l);
    }

    public boolean update(Lead l) throws SQLException {
        if (l.getLeadId() <= 0) throw new IllegalArgumentException("leadId is required");
        normalize(l);
        return dao.update(l);
    }

    public boolean updateStatus(int leadId, String status) throws SQLException {
        String s = status == null ? "" : status.toUpperCase();
        if (!STATUSES.contains(s)) throw new IllegalArgumentException("Invalid status: " + status);
        return dao.updateStatus(leadId, s);
    }

    public boolean assign(int leadId, Integer staffId) throws SQLException {
        return dao.assignStaff(leadId, staffId);
    }

    public boolean delete(int id) throws SQLException { return dao.delete(id); }

    private void normalize(Lead l) {
        if (l.getPriority() != null) l.setPriority(l.getPriority().toUpperCase());
        if (l.getStatus()   != null) l.setStatus(l.getStatus().toUpperCase());
        if (l.getPriority() != null && !PRIORITIES.contains(l.getPriority()))
            throw new IllegalArgumentException("Invalid priority: " + l.getPriority());
        if (l.getStatus() != null && !STATUSES.contains(l.getStatus()))
            throw new IllegalArgumentException("Invalid status: " + l.getStatus());
    }
}
