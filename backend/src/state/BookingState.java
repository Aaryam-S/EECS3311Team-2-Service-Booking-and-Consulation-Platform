package state;

import model.Booking;

public interface BookingState {
    void handleRequest(Booking context);
    void confirm(Booking context);
    void reject(Booking context);
    void pay(Booking context);
    void cancel(Booking context);
    void complete(Booking context);
}