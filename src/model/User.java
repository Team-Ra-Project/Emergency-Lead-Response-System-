package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class User implements Serializable {
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private int roleId;
    private String roleName;
    private String status;
    private LocalDateTime createdAt;

    public int getUserId() { return userId; }
    public void setUserId(int v) { userId = v; }
    public String getFullName() { return fullName; }
    public void setFullName(String v) { fullName = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = v; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { passwordHash = v; }
    public int getRoleId() { return roleId; }
    public void setRoleId(int v) { roleId = v; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String v) { roleName = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
}