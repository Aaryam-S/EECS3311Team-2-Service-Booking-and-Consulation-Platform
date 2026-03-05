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
		// TODO: validate + simulate payment + create receipt 
		return null;
	}
	
	public PaymentReceipt processWithSavedMethod(double amount, SavedPaymentMethod method) {
		// TODO
		return null;
	}
	
}
