package state;

import model.Booking;

public class PaidState implements BookingState {

    @Override
    public void handleRequest(Booking context) {
        System.out.println("Booking is already paid.");
    }

    @Override
    public void confirm(Booking context) {
        System.out.println("Booking is already confirmed and paid.");
    }

    @Override
    public void reject(Booking context) {
        System.out.println("Cannot reject a paid booking.");
    }

    @Override
    public void pay(Booking context) {
        System.out.println("Booking is already paid.");
    }

    @Override
    public void cancel(Booking context) {
        // Depending on policy, a paid booking might be cancellable with refund logic
        context.setState(new CancelledState());
    }

    @Override
    public void complete(Booking context) {
        context.setState(new CompletedState());
    }
}