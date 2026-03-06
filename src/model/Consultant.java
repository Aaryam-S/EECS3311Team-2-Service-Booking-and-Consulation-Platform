package model;

import java.util.ArrayList;
import java.util.List;
import notification.Observer;

public class Consultant implements Observer {

    // Attributes
    private String name;
    private String expertise;
    private boolean approved;
    private List<String> notifications;
    private List<TimeSlot> availability;

    // Constructor
    public Consultant() {
        this.notifications = new ArrayList<>();
        this.availability = new ArrayList<>();
        
    }

    public Consultant(String name, String expertise) {
        this.name = name;
        this.expertise = expertise;
        this.notifications = new ArrayList<>();
    }

    // Methods
    @Override
    public void update(String message) {
        notifications.add(message);
        System.out.println("[Consultant " + name + "] Notification: " + message);
    }

    public void setAvailability() {
        if (availability == null) {
        	availability = new ArrayList<>();
        }
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public List<String> getNotifications() {
        return notifications;
    }

    public String getLastNotification() {
        if (notifications.isEmpty()) {
            return null;
        }
        return notifications.get(notifications.size() - 1);
    }

}
