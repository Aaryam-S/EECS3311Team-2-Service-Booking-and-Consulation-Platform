package strategy;

import java.time.YearMonth;

public class DebitCardStrategy implements PaymentStrategy {

	//Attributes
	private String cardNumber;
	private int expiryMonth;
	private int expiryYear;
	private String cvv;
	
	//Constructor 
	public DebitCardStrategy(String cardNumber, int expiryMonth, int expiryYear, String cvv) {
        this.cardNumber = cardNumber;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
	}
	
	//Methods 
	@Override 
	public boolean validateDetails() {
        boolean validCardNumber = cardNumber != null && cardNumber.matches("\\d{16}");
        boolean validCvv = cvv != null && cvv.matches("\\d{3,4}");
        boolean validMonth = expiryMonth >= 1 && expiryMonth <= 12;

        if (!validCardNumber || !validCvv || !validMonth) {
            return false;
        }

        YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
        YearMonth now = YearMonth.now();

        return expiry.isAfter(now) || expiry.equals(now);
	}
	
	@Override 
	public boolean processPayment(double amount) {
		return amount > 0 && validateDetails();
	}
	
}
