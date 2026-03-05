package strategy;

public class PayPalStrategy implements PaymentStrategy {

	//Attributes
    private String email;

    //Constructor
    public PayPalStrategy() {

    }

    //Methods
    @Override
    public boolean validateDetails() {
        // TODO
        return true;
    }

    @Override
    public boolean processPayment(double amount) {
        // TODO
        return true;
    }	
	
}
