package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Appointment implements Serializable {
    private int appointmentId;
    private Integer customerId;
    private String customerName;
    private Integer staffId;
    private String staffName;
    private Integer serviceId;
    private String serviceName;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String duration;
    private String status;   // Booked | Pending | Rescheduled | Completed | Cancelled
    private String location;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int v) { appointmentId = v; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer v) { customerId = v; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String v) { customerName = v; }
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer v) { staffId = v; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String v) { staffName = v; }
    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer v) { serviceId = v; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String v) { serviceName = v; }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate v) { appointmentDate = v; }
    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime v) { appointmentTime = v; }
    public String getDuration() { return duration; }
    public void setDuration(String v) { duration = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public String getLocation() { return location; }
    public void setLocation(String v) { location = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { notes = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { updatedAt = v; }
}
