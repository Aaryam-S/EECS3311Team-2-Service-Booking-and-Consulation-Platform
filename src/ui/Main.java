package ui;

import java.time.LocalDateTime;
import java.util.List;

import model.Admin;
import model.Booking;
import model.Client;
import model.Consultant;
import model.PaymentReceipt;
import model.SavedPaymentMethod;
import model.Service;
import model.SystemPolicy;
import model.TimeSlot;
import policy.FlexibleCancellation;
import policy.NoRefundCancellation;
import policy.StrictCancellation;
import policy.TaxedPriceStrategy;
import service.CatalogService;
import service.PaymentService;
import notification.NotificationService;


public class Main {

    public static void main(String[] args) {
    	


        System.out.println("=======================================");
        System.out.println("Consulting Platform Demo");
        System.out.println("=======================================");

        // Shared objects
        Admin admin = new Admin();
        Client client = new Client("Joe","joe@gmail.com");
        Consultant consultant = new Consultant("Dave","Software Consulting");
        PaymentService paymentService = new PaymentService();
        CatalogService catalogService = CatalogService.getInstance();
        
        NotificationService notificationService = NotificationService.getInstance();
        notificationService.subscribe(client);
        notificationService.subscribe(consultant);

        // =========================
        // UC11 - Approve Consultant Registration
        // =========================
        System.out.println("\n[UC11] Approve Consultant Registration");
        admin.approveConsultant(consultant);
        System.out.println("Consultant approved: " + consultant.isApproved());

        // =========================
        // UC12 - Define System Policies
        // =========================
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

        // =========================
        // UC1 - Browse Consulting Services
        // =========================
        System.out.println("\n[UC1] Browse Consulting Services"); 
        List<Service> services = catalogService.getAllServices();	
        for (Service service : services) {
            System.out.println(service);
        }

        // Pick a service for demo
        Service selectedService = catalogService.findServiceByName("Software Consulting");

        // =========================
        // UC8 - Manage Availability
        // =========================
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

        // =========================
        // UC2 - Request a Booking
        // =========================
        System.out.println("\n[UC2] Request a Booking");
        Booking booking1 = new Booking(client, consultant, selectedService, slot1);
        client.addBooking(booking1);
        System.out.println("Booking created.");
        System.out.println("Booking ID: " + booking1.getId());
        System.out.println("Booking state: " + booking1.getStateName());

        // =========================
        // UC9 - Accept Booking Request
        // =========================
        System.out.println("\n[UC9] Accept Booking Request"); 
        consultant.acceptBooking(booking1);
        System.out.println("Booking state: " + booking1.getStateName());

        // =========================
        // UC6 - Manage Payment Methods
        // =========================
        System.out.println("\n[UC6] Manage Payment Methods");

        SavedPaymentMethod creditCard =
                new SavedPaymentMethod("1", "creditcard", "1234567812345678", "789", 12, 2028); 

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

        // =========================
        // UC5 - Process Payment
        // =========================
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

        // =========================
        // UC7 - View Payment History
        // =========================
        System.out.println("\n[UC7] View Payment History");
        for (PaymentReceipt receipt : client.viewPaymentHistory()) {
            System.out.println("Transaction ID: " + receipt.getTransactionId()
                    + " | Amount: " + receipt.getAmount()
                    + " | Method: " + receipt.getMethodType()
                    + " | Status: " + receipt.getStatus());
        }

        // =========================
        // UC10 - Complete a Booking
        // =========================
        System.out.println("\n[UC10] Complete a Booking"); 
        consultant.completeBooking(booking1);
        System.out.println("Booking state: " + booking1.getStateName());

        // =========================
        // UC4 - View Booking History
        // =========================
        System.out.println("\n[UC4] View Booking History");
        for (Booking booking : client.viewBookingHistory()) {
            System.out.println("Booking ID: " + booking.getId()
                    + " | Service: " + booking.getService().getName()
                    + " | State: " + booking.getStateName());
        }

        // =========================
        // UC3 - Cancel a Booking
        // =========================
        System.out.println("\n[UC3] Cancel a Booking");
        Booking booking2 = new Booking(client, consultant, selectedService, slot2);
        client.addBooking(booking2);
        booking2.confirm();

        double strictFee = SystemPolicy.getInstance().getCancellationPolicy().cancellationFee(booking2);
        booking2.cancel();

        System.out.println("Booking cancelled.");
        System.out.println("Cancellation fee under Strict policy: " + strictFee);
        System.out.println("Booking state: " + booking2.getStateName());

        // =========================
        // UC9 - Reject Booking Request
        // =========================
        System.out.println("\n[UC9] Reject Booking Request");
        TimeSlot slot3 = new TimeSlot(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(3).plusHours(1)
        );
        Booking booking3 = new Booking(client, consultant, selectedService, slot3);
        client.addBooking(booking3);
        consultant.rejectBooking(booking3);

        
        System.out.println("Booking state: " + booking3.getStateName());

        // =========================
        // Extra: Show all cancellation policy behaviors
        // =========================
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

        System.out.println("\n=======================================");
        System.out.println("Demo complete");
        System.out.println("=======================================");
    }
}