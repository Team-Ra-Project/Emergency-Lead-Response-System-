package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Lead implements Serializable {
    private int leadId;
    private int customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Integer serviceId;
    private String serviceName;
    private String priority;   // LOW | MEDIUM | HIGH | EMERGENCY
    private String status;     // NEW | CONTACTED | QUALIFIED | QUOTE_SENT | BOOKED | COMPLETED | LOST
    private Integer assignedStaff;
    private String assignedStaffName;
    private String notes;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public int getLeadId() { return leadId; }
    public void setLeadId(int v) { leadId = v; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int v) { customerId = v; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String v) { customerName = v; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String v) { customerPhone = v; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String v) { customerEmail = v; }
    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer v) { serviceId = v; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String v) { serviceName = v; }
    public String getPriority() { return priority; }
    public void setPriority(String v) { priority = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public Integer getAssignedStaff() { return assignedStaff; }
    public void setAssignedStaff(Integer v) { assignedStaff = v; }
    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String v) { assignedStaffName = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { notes = v; }
    public String getSource() { return source; }
    public void setSource(String v) { source = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { updatedAt = v; }
}
