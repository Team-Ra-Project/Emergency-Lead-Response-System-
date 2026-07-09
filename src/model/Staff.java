package model;

import java.time.LocalDate;

public class Staff {
    private int staffId;
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String designation;
    private String specialization;
    private String availability;
    private LocalDate joinedOn;

    public int getStaffId() { return staffId; } public void setStaffId(int v){staffId=v;}
    public int getUserId() { return userId; } public void setUserId(int v){userId=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getDesignation(){return designation;} public void setDesignation(String v){designation=v;}
    public String getSpecialization(){return specialization;} public void setSpecialization(String v){specialization=v;}
    public String getAvailability(){return availability;} public void setAvailability(String v){availability=v;}
    public LocalDate getJoinedOn(){return joinedOn;} public void setJoinedOn(LocalDate v){joinedOn=v;}
}