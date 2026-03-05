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

    //Payment Management
    
    public void addPaymentMethod(SavedPaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Payment method cannot be null.");
        }
        savedPaymentMethods.add(method);   
    }

    public void updatePaymentMethod(String methodId, SavedPaymentMethod method) {
        // TODO: implement after when SavedPaymentMethod is ready
    }

    public void removePaymentMethod(String methodId) {
        // TODO: implement after when SavedPaymentMethod is ready
    }

    public List<SavedPaymentMethod> listPaymentMethods() {
        return savedPaymentMethods;
    }

    public List<PaymentReceipt> viewPaymentHistory() {
        return paymentHistory;
    }
    
}
