package model;

import java.util.List;
import java.util.ArrayList;
import notification.Observer;

public class Client implements Observer {
	
	//Attributes
    private String name;
    private String email;
    private List<SavedPaymentMethod> savedPaymentMethods;
    private List<PaymentReceipt> paymentHistory;
	
    //Constructor 
    public Client() {
        this.savedPaymentMethods = new ArrayList<>();
        this.paymentHistory = new ArrayList<>();
    }
    
    //Methods
    @Override
    public void update(String message) {
        // TODO: handle notification
    }

    public void addPaymentMethod(SavedPaymentMethod method) {
        // TODO
    }

    public void updatePaymentMethod(String methodId, SavedPaymentMethod method) {
        // TODO
    }

    public void removePaymentMethod(String methodId) {
        // TODO
    }

    public List<SavedPaymentMethod> listPaymentMethods() {
        return savedPaymentMethods;
    }

    public List<PaymentReceipt> viewPaymentHistory() {
        return paymentHistory;
    }
    
}
