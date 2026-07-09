package model;

import java.time.LocalDateTime;

public class Notification {
    private int notificationId;
    private int userId;
    private String channel;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public int getNotificationId(){return notificationId;} public void setNotificationId(int v){notificationId=v;}
    public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
    public String getChannel(){return channel;} public void setChannel(String v){channel=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public boolean isRead(){return read;} public void setRead(boolean v){read=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}