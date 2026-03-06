package service;

import model.PaymentReceipt;
import model.SavedPaymentMethod;
import strategy.PaymentStrategy;


public class PaymentService {

	//Constructor
	public PaymentService() {
		
	}
	
	//Methods
	public PaymentReceipt process(double amount, PaymentStrategy strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("Payment strategy cant be null.");
		}
		if (amount <= 0) {
			throw new IllegalArgumentException("Amount has to be more than 0");
		}
		
		//Validate details
        boolean valid = strategy.validateDetails(); 
        if (!valid) {
        	//Invalid Exception 
            return null;
        }
        
        // 2-3 seconds delay
        simulateDelay(); 
        
        boolean success = strategy.processPayment(amount);
        if (!success) {
            return null;
        }
        
        // Create receipt (field filling needs to be done) 
        PaymentReceipt receipt = new PaymentReceipt();

        return receipt;
	}
	
	public PaymentReceipt processWithSavedMethod(double amount, SavedPaymentMethod method) {
		
        if (method == null) {
            throw new IllegalArgumentException("Saved payment method cannot be null.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
		
        // Need to implement Mapping SavedPaymentMethod to PaymentStrategy 
        return null;
	}
	
    //Helper for delay
    private void simulateDelay() {
        try {
            long delayMs = 2000 + (long) (Math.random() * 1000); 
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}
