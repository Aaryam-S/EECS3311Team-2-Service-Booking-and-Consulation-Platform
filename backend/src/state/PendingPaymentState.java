package state;

import model.Booking;

public class PendingPaymentState implements BookingState {

    @Override
    public void handleRequest(Booking context) {
        System.out.println("Booking is already confirmed and pending payment.");
    }

    @Override
    public void confirm(Booking context) {
        System.out.println("Booking is already confirmed.");
    }

    @Override
    public void reject(Booking context) {
        System.out.println("Cannot reject a confirmed booking. Must be cancelled.");
    }

    @Override
    public void pay(Booking context) {
        System.out.println("Payment processed successfully.");
        context.setState(new PaidState());
    }

    @Override
    public void cancel(Booking context) {
        context.setState(new CancelledState());
    }

    @Override
    public void complete(Booking context) {
        System.out.println("Cannot complete a booking that hasn't been paid.");
    }
}