package strategy;

public class PayPalStrategy implements PaymentStrategy {

	//Attributes
    private String email;

    //Constructor
    public PayPalStrategy(String email) {
    	this.email = email;
    }

    //Methods
    @Override
    public boolean validateDetails() {
        return email != null
                && !email.isBlank()
                && email.contains("@")
                && email.contains(".");
    }

    @Override
    public boolean processPayment(double amount) {
    	return amount > 0 && validateDetails();
    }	
	
}
