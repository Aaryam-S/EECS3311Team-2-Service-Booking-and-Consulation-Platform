package state;

import model.Booking;

public class CancelledState implements BookingState {
    // This is a terminal state
    private void printError() {
        System.out.println("Booking has been cancelled and cannot be modified.");
    }

    @Override public void handleRequest(Booking context) { printError(); }
    @Override public void confirm(Booking context) { printError(); }
    @Override public void reject(Booking context) { printError(); }
    @Override public void pay(Booking context) { printError(); }
    @Override public void cancel(Booking context) { System.out.println("Booking is already cancelled."); }
    @Override public void complete(Booking context) { printError(); }
}