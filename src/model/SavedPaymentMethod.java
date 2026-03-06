package model;

public class SavedPaymentMethod {
	
	//Attributes
    private String id;
    private String type;
    private String maskedDetails;
    private int expiryMonth;
    private int expiryYear;	
	
	//Constructor for debit/credit cards
    public SavedPaymentMethod(String id, String type, String maskedDetails, int expiryMonth, int expiryYear) {
        this.id = id;
        this.type = type;
        this.maskedDetails = maskedDetails;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
    }
    
    //Constructor for PayPal / Bank Transfer
    public SavedPaymentMethod(String id, String type, String maskedDetails) {
        this.id = id;
        this.type = type;
        this.maskedDetails = maskedDetails;
    }
    
    //Getters
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getMaskedDetails() {
        return maskedDetails;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public int getExpiryYear() {
        return expiryYear;
    }
}
