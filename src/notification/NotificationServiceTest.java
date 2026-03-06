package notification;

import model.Client;
import model.Consultant;

public class NotificationServiceTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=== NotificationService Tests ===\n");

        NotificationService.resetInstance();

        testSingletonInstance();
        testSubscribeObserver();
        testSubscribeDuplicateObserver();
        testSubscribeNullObserverThrows();
        testUnsubscribeObserver();
        testUnsubscribeNullObserverThrows();
        testNotifyObservers();
        testNotifyMultipleObservers();
        testNotifyAfterUnsubscribe();
        testNotifyNullMessageThrows();
        testNotifyBlankMessageThrows();
        testLastMessage();
        testClientReceivesNotification();
        testConsultantReceivesNotification();
        testMultipleNotifications();

        System.out.println("\n=== Results ===");
        System.out.println("Passed: " + testsPassed);
        System.out.println("Failed: " + testsFailed);
        System.out.println("Total:  " + (testsPassed + testsFailed));

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void testSingletonInstance() {
        NotificationService.resetInstance();
        NotificationService a = NotificationService.getInstance();
        NotificationService b = NotificationService.getInstance();
        assertEqual("Singleton returns same instance", true, a == b);
    }

    private static void testSubscribeObserver() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        Client client = new Client();
        ns.subscribe(client);
        assertEqual("Subscribe adds observer", 1, ns.getObservers().size());
    }

    private static void testSubscribeDuplicateObserver() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        Client client = new Client();
        ns.subscribe(client);
        ns.subscribe(client);
        assertEqual("Duplicate subscribe ignored", 1, ns.getObservers().size());
    }

    private static void testSubscribeNullObserverThrows() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        try {
            ns.subscribe(null);
            fail("Subscribe null should throw");
        } catch (IllegalArgumentException e) {
            pass("Subscribe null throws IllegalArgumentException");
        }
    }

    private static void testUnsubscribeObserver() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        Client client = new Client();
        ns.subscribe(client);
        ns.unsubscribe(client);
        assertEqual("Unsubscribe removes observer", 0, ns.getObservers().size());
    }

    private static void testUnsubscribeNullObserverThrows() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        try {
            ns.unsubscribe(null);
            fail("Unsubscribe null should throw");
        } catch (IllegalArgumentException e) {
            pass("Unsubscribe null throws IllegalArgumentException");
        }
    }

    private static void testNotifyObservers() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        Client client = new Client();
        client.setName("Alice");
        ns.subscribe(client);
        ns.notifyObservers("Booking confirmed.");
        assertEqual("Client receives notification", "Booking confirmed.", client.getLastNotification());
    }

    private static void testNotifyMultipleObservers() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        Client client = new Client();
        client.setName("Bob");
        Consultant consultant = new Consultant("Dr. Smith", "Software");
        ns.subscribe(client);
        ns.subscribe(consultant);
        ns.notifyObservers("Session starting soon.");
        assertEqual("Client got message", "Session starting soon.", client.getLastNotification());
        assertEqual("Consultant got message", "Session starting soon.", consultant.getLastNotification());
    }

    private static void testNotifyAfterUnsubscribe() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        Client client = new Client();
        client.setName("Carol");
        ns.subscribe(client);
        ns.unsubscribe(client);
        ns.notifyObservers("You should not see this.");
        assertEqual("Unsubscribed client has no notifications", 0, client.getNotifications().size());
    }

    private static void testNotifyNullMessageThrows() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        try {
            ns.notifyObservers(null);
            fail("Null message should throw");
        } catch (IllegalArgumentException e) {
            pass("Null message throws IllegalArgumentException");
        }
    }

    private static void testNotifyBlankMessageThrows() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        try {
            ns.notifyObservers("   ");
            fail("Blank message should throw");
        } catch (IllegalArgumentException e) {
            pass("Blank message throws IllegalArgumentException");
        }
    }

    private static void testLastMessage() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        Client client = new Client();
        ns.subscribe(client);
        ns.notifyObservers("First");
        ns.notifyObservers("Second");
        assertEqual("Last message tracked", "Second", ns.getLastMessage());
    }

    private static void testClientReceivesNotification() {
        Client client = new Client();
        client.setName("TestClient");
        client.update("Direct update test");
        assertEqual("Client update stores message", "Direct update test", client.getLastNotification());
        assertEqual("Client notification count", 1, client.getNotifications().size());
    }

    private static void testConsultantReceivesNotification() {
        Consultant consultant = new Consultant("TestDoc", "Legal");
        consultant.update("Direct update test");
        assertEqual("Consultant update stores message", "Direct update test", consultant.getLastNotification());
        assertEqual("Consultant notification count", 1, consultant.getNotifications().size());
    }

    private static void testMultipleNotifications() {
        NotificationService.resetInstance();
        NotificationService ns = NotificationService.getInstance();
        Client client = new Client();
        client.setName("MultiTest");
        ns.subscribe(client);
        ns.notifyObservers("Message 1");
        ns.notifyObservers("Message 2");
        ns.notifyObservers("Message 3");
        assertEqual("Client received 3 notifications", 3, client.getNotifications().size());
        assertEqual("Last notification is Message 3", "Message 3", client.getLastNotification());
    }

    // --- Helpers ---

    private static void assertEqual(String testName, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            pass(testName);
        } else {
            System.out.println("  FAIL: " + testName + " (expected: " + expected + ", got: " + actual + ")");
            testsFailed++;
        }
    }

    private static void pass(String testName) {
        System.out.println("  PASS: " + testName);
        testsPassed++;
    }

    private static void fail(String testName) {
        System.out.println("  FAIL: " + testName);
        testsFailed++;
    }
}
