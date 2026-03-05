package model;

import notification.Observer;

public class Consultant implements Observer {
	
	//Attributes
    private String name;
    private String expertise;

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
	
}
