package ui;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import model.Admin;
import model.Booking;
import model.Client;
import model.Consultant;
import model.PaymentReceipt;
import model.SavedPaymentMethod;
import model.Service;
import model.SystemPolicy;
import model.TimeSlot;
import notification.NotificationService;
import policy.FlexibleCancellation;
import policy.NoRefundCancellation;
import policy.StrictCancellation;
import policy.TaxedPriceStrategy;
import service.CatalogService;
import service.PaymentService;

public class Main {

    private static Scanner scanner;
    private static Client interactiveClient;
    private static Consultant interactiveConsultant;
    private static PaymentService paymentService;
    private static CatalogService catalogService;

    public static void main(String[] args) {

        System.out.println("=======================================");
        System.out.println("Consulting Platform Demo");
        System.out.println("=======================================");

        Admin admin = new Admin();
        Client client = new Client("Joe", "joe@gmail.com");
        Consultant consultant = new Consultant("Dave", "Software Consulting");
        paymentService = new PaymentService(); 
        catalogService = CatalogService.getInstance();
        
        NotificationService notificationService = NotificationService.getInstance();
        notificationService.subscribe(client);
        notificationService.subscribe(consultant);

        System.out.println("\n[UC11] Approve Consultant Registration");
        admin.approveConsultant(consultant);
        System.out.println("Consultant approved: " + consultant.isApproved());

        System.out.println("\n[UC12] Define System Policies");
        admin.setPricingStrategy(new TaxedPriceStrategy());
        admin.setCancellationPolicy(new StrictCancellation());
        admin.setNotificationEnabled(true);
        admin.setRefundEnabled(true);

        SystemPolicy.getInstance().setTaxRate(0.13);
        SystemPolicy.getInstance().setCancellationFee(20.0);

        System.out.println("Pricing strategy set to TaxedPriceStrategy");
        System.out.println("Cancellation policy set to StrictCancellation");
        System.out.println("Tax rate: " + SystemPolicy.getInstance().getTaxRate());
        System.out.println("Cancellation fee: " + SystemPolicy.getInstance().getCancellationFee());

        System.out.println("\n[UC1] Browse Consulting Services");
        List<Service> services = catalogService.getAllServices();
        for (Service service : services) {
            System.out.println(service);
        }

        Service selectedService = catalogService.findServiceByName("Software Consulting");

        System.out.println("\n[UC8] Manage Availability");
        consultant.setAvailability();
        TimeSlot slot1 = new TimeSlot(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1)
        );
        TimeSlot slot2 = new TimeSlot(
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(1)
        );
        System.out.println("Available slot 1: " + slot1);
        System.out.println("Available slot 2: " + slot2);

        System.out.println("\n[UC2] Request a Booking");
        Booking booking1 = new Booking(client, consultant, selectedService, slot1);
        client.addBooking(booking1);
        System.out.println("Booking created.");
        System.out.println("Booking ID: " + booking1.getId());
        System.out.println("Booking state: " + booking1.getStateName());

        System.out.println("\n[UC9] Accept Booking Request");
        booking1.confirm();
        System.out.println("Booking accepted.");
        System.out.println("Booking state: " + booking1.getStateName());

        System.out.println("\n[UC6] Manage Payment Methods");

        SavedPaymentMethod creditCard =
                new SavedPaymentMethod("1", "creditcard", "1234567812345678", "123", 12, 2028);

        SavedPaymentMethod paypal =
                new SavedPaymentMethod("2", "paypal", "user@email.com");

        client.addPaymentMethod(creditCard);
        client.addPaymentMethod(paypal);

        System.out.println("Payment methods after adding:");
        for (SavedPaymentMethod method : client.listPaymentMethods()) {
            System.out.println("- " + method.getId() + " | " + method.getType() + " | " + method.getMaskedDetails());
        }

        SavedPaymentMethod updatedPaypal =
                new SavedPaymentMethod("2", "paypal", "updated@email.com");

        client.updatePaymentMethod("2", updatedPaypal);

        System.out.println("Payment methods after update:");
        for (SavedPaymentMethod method : client.listPaymentMethods()) {
            System.out.println("- " + method.getId() + " | " + method.getType() + " | " + method.getMaskedDetails());
        }

        client.removePaymentMethod("2");

        System.out.println("Payment methods after removal:");
        for (SavedPaymentMethod method : client.listPaymentMethods()) {
            System.out.println("- " + method.getId() + " | " + method.getType() + " | " + method.getMaskedDetails());
        }

        System.out.println("\n[UC5] Process Payment");
        PaymentReceipt receipt1 = paymentService.processWithSavedMethod(150.0, creditCard);

        if (receipt1 != null) {
            client.addPaymentReceipt(receipt1);
            booking1.setPaymentReceipt(receipt1);
            booking1.pay();

            System.out.println("Payment successful.");
            System.out.println("Transaction ID: " + receipt1.getTransactionId());
            System.out.println("Amount: " + receipt1.getAmount());
            System.out.println("Method: " + receipt1.getMethodType());
            System.out.println("Timestamp: " + receipt1.getTimestamp());
            System.out.println("Status: " + receipt1.getStatus());
            System.out.println("Booking state: " + booking1.getStateName());
        } else {
            System.out.println("Payment failed.");
        }

        System.out.println("\n[UC7] View Payment History");
        for (PaymentReceipt receipt : client.viewPaymentHistory()) {
            System.out.println("Transaction ID: " + receipt.getTransactionId()
                    + " | Amount: " + receipt.getAmount()
                    + " | Method: " + receipt.getMethodType()
                    + " | Status: " + receipt.getStatus());
        }

        System.out.println("\n[UC10] Complete a Booking");
        booking1.complete();
        System.out.println("Booking completed.");
        System.out.println("Booking state: " + booking1.getStateName());

        System.out.println("\n[UC4] View Booking History");
        for (Booking booking : client.viewBookingHistory()) {
            System.out.println("Booking ID: " + booking.getId()
                    + " | Service: " + booking.getService().getName()
                    + " | State: " + booking.getStateName());
        }

        System.out.println("\n[UC3] Cancel a Booking");
        Booking booking2 = new Booking(client, consultant, selectedService, slot2);
        client.addBooking(booking2);
        booking2.confirm();

        double strictFee = SystemPolicy.getInstance().getCancellationPolicy().cancellationFee(booking2);
        booking2.cancel();

        System.out.println("Booking cancelled.");
        System.out.println("Cancellation fee under Strict policy: " + strictFee);
        System.out.println("Booking state: " + booking2.getStateName());

        System.out.println("\n[UC9] Reject Booking Request");
        TimeSlot slot3 = new TimeSlot(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(3).plusHours(1)
        );
        Booking booking3 = new Booking(client, consultant, selectedService, slot3);
        client.addBooking(booking3);
        booking3.reject();

        System.out.println("Booking rejected.");
        System.out.println("Booking state: " + booking3.getStateName());

        System.out.println("\n[Policy Comparison Demo]");

        admin.setCancellationPolicy(new FlexibleCancellation());
        double flexibleFee = SystemPolicy.getInstance().getCancellationPolicy().cancellationFee(booking1);
        System.out.println("Flexible cancellation fee: " + flexibleFee + " (full refund)");

        admin.setCancellationPolicy(new StrictCancellation());
        double newStrictFee = SystemPolicy.getInstance().getCancellationPolicy().cancellationFee(booking1);
        System.out.println("Strict cancellation fee: " + newStrictFee + " (partial refund)");

        admin.setCancellationPolicy(new NoRefundCancellation());
        double noRefundFee = SystemPolicy.getInstance().getCancellationPolicy().cancellationFee(booking1);
        System.out.println("No refund cancellation fee: " + noRefundFee + " (no refund)");

        System.out.println("\n[Notifications]");
        System.out.println("NotificationService integration will be added when the notification package is completed.");

        System.out.println("\n=======================================");
        System.out.println("Demo complete");
        System.out.println("=======================================");

        startInteractivePhase();
    }

    private static void startInteractivePhase() {
        System.out.println("\nInteractive Phase 1 (CLI)");

        scanner = new Scanner(System.in);
        interactiveClient = new Client("Interactive User", "user@test.com");
        
        interactiveClient.addPaymentMethod(new SavedPaymentMethod("99", "creditcard", "1234567812349999", "123", 12, 2026));
        interactiveClient.addPaymentMethod(new SavedPaymentMethod("100", "paypal", "user@example.com"));

        interactiveConsultant = new Consultant("Dr. Interactive", "Business Strategy");

        runMainMenu();
    }

    private static void runMainMenu() {
        while (true) {
            System.out.println("\nMain Menu");
            System.out.println("1. Client View");
            System.out.println("2. Consultant View");
            System.out.println("3. Exit");
            System.out.print("Select: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1": runClientMenu(); break;
                case "2": runConsultantMenu(); break;
                case "3": return;
                default: System.out.println("Invalid.");
            }
        }
    }

    private static void runClientMenu() {
        while (true) {
            System.out.println("\nClient Portal");
            System.out.println("1. Browse Services");
            System.out.println("2. Book a Session");
            System.out.println("3. Process Payment");
            System.out.println("4. Back");
            System.out.print("Select: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    List<Service> services = catalogService.getAllServices();
                    for (int i = 0; i < services.size(); i++) System.out.println((i + 1) + ". " + services.get(i));
                    break;
                case "2":
                    System.out.print("Enter Service ID to book: ");
                    try {
                        int id = Integer.parseInt(scanner.nextLine()) - 1;
                        if (id >= 0 && id < catalogService.getAllServices().size()) {
                            Service s = catalogService.getAllServices().get(id);
                            TimeSlot ts = new TimeSlot(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1));
                            Booking b = new Booking(interactiveClient, interactiveConsultant, s, ts);
                            interactiveClient.addBooking(b);
                            System.out.println("Booking Requested (ID: " + b.getId() + ")");
                        }
                    } catch (Exception e) { System.out.println("Invalid input"); }
                    break;
                case "3":
                    List<Booking> list = interactiveClient.viewBookingHistory();
                    boolean found = false;
                    for (Booking b : list) {
                        if (b.getStateName().equalsIgnoreCase("Confirmed")) {
                            System.out.println("ID: " + b.getId() + " | Price: " + b.getService().getRate());
                            found = true;
                        }
                    }
                    if (!found) { System.out.println("No confirmed bookings to pay."); break; }
                    
                    System.out.print("Enter Booking ID to pay: ");
                    try {
                        int pid = Integer.parseInt(scanner.nextLine());
                        Booking toPay = null;
                        for (Booking b : list) {
                            if (b.getId() == pid && b.getStateName().equalsIgnoreCase("Confirmed")) {
                                toPay = b;
                            }
                        }
                        
                        if (toPay != null) {
                            System.out.println("Select Payment Method:");
                            List<SavedPaymentMethod> methods = interactiveClient.listPaymentMethods();
                            for (int i=0; i < methods.size(); i++) {
                                System.out.println((i+1) + ". " + methods.get(i).getType() + " (" + methods.get(i).getMaskedDetails() + ")");
                            }
                            
                            int mid = Integer.parseInt(scanner.nextLine()) - 1;
                            if (mid >= 0 && mid < methods.size()) {
                                PaymentReceipt r = paymentService.processWithSavedMethod(toPay.getService().getRate(), methods.get(mid));
                                if (r != null) {
                                    toPay.setPaymentReceipt(r);
                                    toPay.pay();
                                    System.out.println("Payment Successful.");
                                    System.out.println("Transaction ID: " + r.getTransactionId());
                                } else {
                                    System.out.println("Payment failed.");
                                }
                            } else {
                                System.out.println("Invalid method.");
                            }
                        } else {
                            System.out.println("Booking not found.");
                        }
                    } catch (NumberFormatException e) { 
                        System.out.println("Invalid input format."); 
                    }
                    break;
                case "4": return;
            }
        }
    }

    private static void runConsultantMenu() {
        List<Booking> requests = interactiveClient.viewBookingHistory();
        boolean found = false;
        for (Booking b : requests) {
            if (b.getStateName().equalsIgnoreCase("Requested")) {
                found = true;
                System.out.println("Request ID: " + b.getId() + " | " + b.getService().getName());
                System.out.print("[1] Accept [2] Reject: ");
                String c = scanner.nextLine();
                if (c.equals("1")) { b.confirm(); System.out.println("Accepted."); }
                else if (c.equals("2")) { b.reject(); System.out.println("Rejected."); }
            }
        }
        if (!found) System.out.println("No pending requests.");
    }
}