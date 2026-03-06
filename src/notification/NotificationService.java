package notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    // Singleton Instance
    private static NotificationService instance;

    // Attributes
    private List<Observer> observers;
    private String lastMessage;

    // Constructor
    private NotificationService() {
        this.observers = new ArrayList<>();
    }

    // Methods
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    public void subscribe(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null.");
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void unsubscribe(Observer observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null.");
        }
        observers.remove(observer);
    }

    public void notifyObservers(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Notification message cannot be null or empty.");
        }
        this.lastMessage = message;
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    // Getters
    public List<Observer> getObservers() {
        return observers;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    // Reset for testing purposes
    public static void resetInstance() {
        instance = null;
    }
}
