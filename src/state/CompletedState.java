package state;

import model.Booking;

public class CompletedState implements BookingState {

    @Override
    public void handleRequest(Booking context) {
        System.out.println("Booking is already completed.");
    }

    @Override
    public void confirm(Booking context) {
        System.out.println("Booking is already completed.");
    }

    @Override
    public void reject(Booking context) {
        System.out.println("Cannot reject a completed booking.");
    }

    @Override
    public void pay(Booking context) {
        System.out.println("Booking is already paid and completed.");
    }

    @Override
    public void cancel(Booking context) {
        System.out.println("Cannot cancel a completed booking.");
    }

    @Override
    public void complete(Booking context) {
        System.out.println("Booking is already completed.");
    }
}