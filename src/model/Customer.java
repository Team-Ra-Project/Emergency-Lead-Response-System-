package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Customer implements Serializable {
    private int customerId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private String customerType; // Individual | Family | Corporate | Hospital | Government | NGO | Other
    private String status;       // Active | Inactive
    private String notes;
    private LocalDateTime createdAt;

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int v) { customerId = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { fullName = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = v; }
    public String getCity() { return city; }
    public void setCity(String v) { city = v; }
    public String getState() { return state; }
    public void setState(String v) { state = v; }
    public String getPincode() { return pincode; }
    public void setPincode(String v) { pincode = v; }
    public String getCustomerType() { return customerType; }
    public void setCustomerType(String v) { customerType = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public String getNotes() { return notes; }
    public void setNotes(String v) { notes = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}
