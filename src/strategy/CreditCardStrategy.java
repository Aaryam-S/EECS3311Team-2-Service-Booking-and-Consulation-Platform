package strategy;

public class CreditCardStrategy implements PaymentStrategy {

	//Attributes
	private String cardNumber;
	private int exipryMonth;
	private int expiryYear;
	private String cvv;
	
	//Constructor 
	public CreditCardStrategy() {

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
