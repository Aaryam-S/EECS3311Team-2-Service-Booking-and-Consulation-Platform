package strategy;

public class BankTransferStrategy implements PaymentStrategy {

    //Attributes
    private String accountNumber;
    private String routingNumber;

    //Constructor
    public BankTransferStrategy() {

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
