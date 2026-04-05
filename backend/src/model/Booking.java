package model;

import state.BookingState;
import state.CancelledState;
import state.CompletedState;
import state.ConfirmedState;
import state.PaidState;
import state.PendingPaymentState;
import state.RejectedState;
import state.RequestedState;

public class Booking {
    private int id;
    private Client client;
    private Consultant consultant;
    private Service service;
    private TimeSlot timeSlot;
    private BookingState state;
    private PaymentReceipt paymentReceipt;

    private static int idCounter = 1;

    public Booking(Client client, Consultant consultant, Service service, TimeSlot timeSlot) {
        this.id = idCounter++;
        this.client = client;
        this.consultant = consultant;
        this.service = service;
        this.timeSlot = timeSlot;
        this.timeSlot.setBooked(true); // Mark slot as booked when created
        this.state = new RequestedState(); 
    }

    // Context methods for State Pattern
    public void setState(BookingState state) {
        this.state = state;
    }

    public BookingState getState() {
        return state;
    }

    public String getStateName() {
        return state.getClass().getSimpleName().replace("State", "");
    }

    // State delegation methods
    public void request() {
        state.handleRequest(this);
    }

    public void confirm() {
        state.confirm(this);
    }

    public void reject() {
        state.reject(this);
        // Free up the slot if rejected
        if (this.timeSlot != null) {
            this.timeSlot.setBooked(false);
        }
    }

    public void pay() {
        state.pay(this);
    }

    public void cancel() {
        state.cancel(this);
        // Free up the slot if cancelled
        if (this.timeSlot != null) {
            this.timeSlot.setBooked(false);
        }
    }

    public void complete() {
        state.complete(this);
    }

    // Getters
    public int getId() { return id; }
    public Client getClient() { return client; }
    public Consultant getConsultant() { return consultant; }
    public Service getService() { return service; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    
    public void setPaymentReceipt(PaymentReceipt receipt) {
        this.paymentReceipt = receipt;
    }
    
    public PaymentReceipt getPaymentReceipt() {
        return paymentReceipt;
    }

    /**
     * Factory for state-machine-only use in controllers.
     * Reconstructs a Booking in the given state so real state transitions can be run.
     */
    public static Booking forStateTransition(String currentStatus) {
        TimeSlot stub = new TimeSlot(
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now().plusHours(1));
        Booking b = new Booking(null, null, null, stub);
        b.state = statusToState(currentStatus);
        return b;
    }

    private static BookingState statusToState(String status) {
        if (status == null) return new RequestedState();
        return switch (status.toLowerCase()) {
            case "confirmed"       -> new ConfirmedState();
            case "pendingpayment"  -> new PendingPaymentState();
            case "paid"            -> new PaidState();
            case "cancelled"       -> new CancelledState();
            case "completed"       -> new CompletedState();
            case "rejected"        -> new RejectedState();
            default                -> new RequestedState();
        };
    }
}