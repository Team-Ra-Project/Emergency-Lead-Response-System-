package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Service implements Serializable {
    private int serviceId;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private String duration;
    private String status;   // Active | Inactive | Temporarily Unavailable
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public int getServiceId() { return serviceId; }
    public void setServiceId(int v) { serviceId = v; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getCategory() { return category; }
    public void setCategory(String v) { category = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal v) { price = v; }
    public String getDuration() { return duration; }
    public void setDuration(String v) { duration = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { updatedAt = v; }
}
