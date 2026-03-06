package model;

import java.util.List;
import java.util.ArrayList;
import notification.Observer;

public class Client implements Observer {

    // Attributes
    private String name;
    private String email;
    private List<SavedPaymentMethod> savedPaymentMethods;
    private List<PaymentReceipt> paymentHistory;
    private List<Booking> bookings;
    private List<String> notifications;

    // Constructor
    public Client(String name, String email) {
    	this.name = name;
    	this.email = email;
        this.savedPaymentMethods = new ArrayList<>();
        this.paymentHistory = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    // Methods
    @Override
    public void update(String message) {
        notifications.add(message);
        System.out.println("[Client " + name + "] Notification: " + message);
    }

    public List<String> getNotifications() {
        return notifications;
    }

    public String getLastNotification() {
        if (notifications.isEmpty()) {
            return null;
        }
        return notifications.get(notifications.size() - 1);
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Payment Management

    public void addPaymentMethod(SavedPaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Payment method cannot be null.");
        }
        savedPaymentMethods.add(method);
    }

    public void updatePaymentMethod(String methodId, SavedPaymentMethod method) {

        if (methodId == null || methodId.isBlank()) {
            throw new IllegalArgumentException("methodId cannot be null or empty.");
        }

        if (method == null) {
            throw new IllegalArgumentException("Payment method cannot be null.");
        }

        for (int i = 0; i < savedPaymentMethods.size(); i++) {
            SavedPaymentMethod current = savedPaymentMethods.get(i);

            if (current.getId().equals(methodId)) {
                savedPaymentMethods.set(i, method);
                return;
            }
        }

        throw new IllegalArgumentException("Payment method not found.");

    }

    public void removePaymentMethod(String methodId) {

        if (methodId == null || methodId.isBlank()) {
            throw new IllegalArgumentException("methodId cannot be null or empty.");
        }

        savedPaymentMethods.removeIf(method -> method.getId().equals(methodId));
    }

    public List<SavedPaymentMethod> listPaymentMethods() {
        return savedPaymentMethods;
    }

    public List<PaymentReceipt> viewPaymentHistory() {
        return paymentHistory;
    }

    public void addPaymentReceipt(PaymentReceipt receipt) {
        if (receipt == null) {
            throw new IllegalArgumentException("Receipt cannot be null.");
        }

        paymentHistory.add(receipt);
    }

    // Booking
    public void addBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null.");
        }
        bookings.add(booking);
    }

    public List<Booking> viewBookingHistory() {
        return bookings;
    }

}
