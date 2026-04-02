package strategy;

public class BankTransferStrategy implements PaymentStrategy {

    //Attributes
    private String accountNumber;
    private String routingNumber;

    //Constructor
    public BankTransferStrategy(String accountNumber, String routingNumber) {
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
    }

    //Methods
    @Override
    public boolean validateDetails() {
        boolean validAccount = accountNumber != null && accountNumber.matches("\\d{6,17}");
        boolean validRouting = routingNumber != null && routingNumber.matches("\\d{5,9}");

        return validAccount && validRouting;
    }

    @Override
    public boolean processPayment(double amount) {
    	 return amount > 0 && validateDetails();
    }

}
