package state;

import model.Booking;

public class ConfirmedState implements BookingState {
    
    public ConfirmedState() {
        // Constructor could trigger notification to client to pay
    }

    @Override
    public void handleRequest(Booking context) {
        System.out.println("Booking is already confirmed.");
    }

    @Override
    public void confirm(Booking context) {
        System.out.println("Booking is already confirmed.");
        // Logic to move to payment processing if not automatic
        context.setState(new PendingPaymentState());
    }

    @Override
    public void reject(Booking context) {
         System.out.println("Cannot reject. Booking is already confirmed.");
    }

    @Override
    public void pay(Booking context) {
        System.out.println("Payment processed successfully from Confirmed state.");
        context.setState(new PaidState());
    }

    @Override
    public void cancel(Booking context) {
        context.setState(new CancelledState());
    }

    @Override
    public void complete(Booking context) {
        System.out.println("Cannot complete unpaid booking.");
    }
}