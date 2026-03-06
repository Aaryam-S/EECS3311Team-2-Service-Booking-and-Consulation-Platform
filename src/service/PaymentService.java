package service;

import java.time.LocalDateTime;
import java.util.UUID;
import model.PaymentReceipt;
import model.SavedPaymentMethod;
import notification.NotificationService;
import strategy.PaymentStrategy;
import strategy.BankTransferStrategy;
import strategy.CreditCardStrategy;
import strategy.DebitCardStrategy;
import strategy.PayPalStrategy;


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
        
        // Create receipt
        String transactionId = generateTransactionId();
        String methodType = strategy.getClass().getSimpleName();
        LocalDateTime timestamp = LocalDateTime.now();
        String status = "SUCCESS";
        
        PaymentReceipt receipt = new PaymentReceipt(transactionId, amount, methodType, timestamp, status);
        
        NotificationService.getInstance().notifyObservers(
        		"Payment confirmation: payment of $" +amount + " was processed successfully."
        );
        return receipt;
	}
	
	public PaymentReceipt processWithSavedMethod(double amount, SavedPaymentMethod method) {
		
        if (method == null) {
            throw new IllegalArgumentException("Saved payment method cannot be null.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
		
        PaymentStrategy strategy;
        
        switch (method.getType().toLowerCase()) {
        case "credit card":
        case "creditcard":
            strategy = new CreditCardStrategy(
            		method.getCardNumber(),
                    method.getExpiryMonth(),
                    method.getExpiryYear(),
                    method.getCvv());
            break;

        case "debit card":
        case "debitcard":
            strategy = new DebitCardStrategy(
            		method.getCardNumber(),
                    method.getExpiryMonth(),
                    method.getExpiryYear(),
                    method.getCvv());
            break;

        case "paypal":
            strategy = new PayPalStrategy(method.getEmail());
            break;

        case "bank transfer":
        case "banktransfer":
            strategy = new BankTransferStrategy(
            		method.getAccountNumber(),
                    method.getRoutingNumber());
            break;
            
        default:
            throw new IllegalArgumentException("Unsupported saved payment method type.");
        }    
        return process(amount, strategy);     
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
    
    //Helper for transaction id
    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
}
