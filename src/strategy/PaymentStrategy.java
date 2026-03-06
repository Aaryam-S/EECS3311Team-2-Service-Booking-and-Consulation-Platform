package strategy;

public interface PaymentStrategy {

//Methods
boolean validateDetails();

boolean processPayment(double amount);
	

}
