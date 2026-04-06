package notification;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class SystemNotificationObserver implements Observer {

    @PostConstruct
    public void register() {
        NotificationService.getInstance().subscribe(this);
        System.out.println("[NotificationService] SystemNotificationObserver registered.");
    }

    @Override
    public void update(String message) {
        System.out.println("[NOTIFICATION] " + message);
    }
}
