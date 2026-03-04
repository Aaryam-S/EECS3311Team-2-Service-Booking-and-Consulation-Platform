package state;

import model.Booking;

public class RequestedState implements BookingState {

    @Override
    public void handleRequest(Booking context) {
        System.out.println("Booking is already requested.");
    }

    @Override
    public void confirm(Booking context) {
        System.out.println("Consultant accepted. Booking Confirmed.");
        context.setState(new ConfirmedState()); 
    }

    @Override
    public void reject(Booking context) {
        System.out.println("Consultant rejected the request.");
        context.setState(new RejectedState());
    }

    @Override
    public void pay(Booking context) {
        System.out.println("Cannot pay for a booking that hasn't been confirmed.");
    }

    @Override
    public void cancel(Booking context) {
        System.out.println("Request cancelled by client.");
        context.setState(new CancelledState());
    }

    @Override
    public void complete(Booking context) {
        System.out.println("Cannot complete a booking that is only requested.");
    }
}