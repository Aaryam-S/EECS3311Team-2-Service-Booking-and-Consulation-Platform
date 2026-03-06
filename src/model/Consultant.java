package model;

import notification.Observer;

public class Consultant implements Observer {
	
	//Attributes
    private String name;
    private String expertise;
    private boolean approved;
    

    //Constructor
    public Consultant() {

    }
	
	//Methods
    @Override
    public void update(String message) {
        // TODO: handle notification
    }

    public void setAvailability() {
        // TODO
    }
    
    public boolean isApproved() {
        return approved;
    }
    
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
	
}
