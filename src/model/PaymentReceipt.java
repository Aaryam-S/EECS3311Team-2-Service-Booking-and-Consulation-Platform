package model;

import java.time.LocalDateTime;

public class PaymentReceipt {
	
	//Attributes
    private String transactionId;
    private double amount;
    private String methodType;
    private LocalDateTime timestamp;
    private String status;
    
    //Constructor
    public PaymentReceipt(String transactionId, double amount, String methodType, LocalDateTime timestamp, String status) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.methodType = methodType;
        this.timestamp = timestamp;
        this.status = status;
    }
    
    //Getters
    public String getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public String getMethodType() {
        return methodType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }
    
    
}
