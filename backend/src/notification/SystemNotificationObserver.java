package notification;

import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

/**
 * Observer Pattern – concrete observer.
 * Subscribes itself to NotificationService at startup so every booking
 * lifecycle event (create, accept, pay, cancel, complete) is received and logged.
 */
@Component
public class SystemNotificationObserver implements Observer {

    @PostConstruct
    public void register() {
        NotificationService.getInstance().subscribe(this);
        System.out.println("[Observer] SystemNotificationObserver registered with NotificationService.");
    }

    @Override
    public void update(String message) {
        System.out.println("[NOTIFICATION] " + message);
    }
}
