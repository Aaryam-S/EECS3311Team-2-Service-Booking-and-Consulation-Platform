package model;

public class SavedPaymentMethod {
	
	//Attributes
    private String id;
    private String type;
    
    private String cardNumber;
    private String cvv;
    private int expiryMonth;
    private int expiryYear;	
	
    private String email;
    
    private String accountNumber;
    private String routingNumber;
    
	//Constructor for debit/credit cards
    public SavedPaymentMethod(String id, String type, String cardNumber, String cvv, int expiryMonth, int expiryYear) {
        this.id = id;
        this.type = type;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
    }
    
    //Constructor for PayPal 
    public SavedPaymentMethod(String id, String type, String email) {
        this.id = id;
        this.type = type;
        this.email = email;
    }
    
  //Constructor for Bank Transfer
    public SavedPaymentMethod(String id, String type, String accountNumber, String routingNumber) {
    	this.id = id;
    	this.type = type;
    	this.accountNumber = accountNumber;
    }
    
    //Getters
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCardNumber() {
        return cardNumber;
    }
    
    public String getCvv() {
        return cvv;
    }

    public int getExpiryMonth() {
        return expiryMonth;
    }

    public int getExpiryYear() {
        return expiryYear;
    }
    
    public String getEmail() {
    	return email;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }
    
    //Display helper
    public String getMaskedDetails() {
        switch (type.toLowerCase()) {
            case "credit card":
            case "creditcard":
            case "debit card":
            case "debitcard":
                if (cardNumber != null && cardNumber.length() >= 4) {
                    return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
                }
                return "****";

            case "paypal":
                return email != null ? email : "";

            case "bank transfer":
            case "banktransfer":
                if (accountNumber != null && accountNumber.length() >= 4) {
                    return "Account ****" + accountNumber.substring(accountNumber.length() - 4);
                }
                return "Account ****";

            default:
                return "";
        }
    }
}
