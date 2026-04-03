package controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import model.Service;
import model.SystemPolicy;
import policy.CancellationPolicy;
import service.CatalogService;
import service.ClientService;

@RestController
@CrossOrigin(origins = "*")
public class BookingController {

    private final List<Map<String, Object>> bookings = new ArrayList<>();
    private final List<Map<String, Object>> timeslots = new ArrayList<>();
    private final AtomicInteger bookingIdCounter = new AtomicInteger(101);
    private final ClientService clientService = ClientService.getInstance();

    public BookingController() {
        // No seed data - start with empty lists
    }

    // BookingController no longer exposes /services; ServiceController handles service catalog.
    // @GetMapping("/services")
    // public ResponseEntity<List<Map<String, Object>>> getServices() {
    //     return ResponseEntity.ok(services);
    // }

    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getBookings(
            @RequestParam(required = false) Integer clientId,
            @RequestParam(required = false) Integer consultantId
    ) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> booking : bookings) {
            Integer bookingClientId = (Integer) booking.get("clientId");
            Integer bookingConsultantId = (Integer) booking.get("consultantId");

            if (clientId != null && !clientId.equals(bookingClientId)) {
                continue;
            }
            if (consultantId != null && !consultantId.equals(bookingConsultantId)) {
                continue;
            }

            result.add(filterClientBooking(booking));
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/timeslots")
    public ResponseEntity<List<Map<String, Object>>> getTimeslots(
            @RequestParam(required = false) Integer consultantId
    ) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> slot : timeslots) {
            if (consultantId != null && !consultantId.equals(slot.get("consultantId"))) {
                continue;
            }
            result.add(slot);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/timeslots/{timeslotId}")
    public ResponseEntity<Map<String, Object>> deleteTimeslot(
            @PathVariable int timeslotId
    ) {
        Map<String, Object> target = null;
        for (Map<String, Object> slot : timeslots) {
            if (((Integer) slot.get("id")).equals(timeslotId)) {
                target = slot;
                break;
            }
        }
        if (target == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Timeslot not found."));
        }
        if (Boolean.TRUE.equals(target.get("booked"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete a booked timeslot."));
        }
        timeslots.remove(target);
        return ResponseEntity.ok(Map.of("message", "Timeslot deleted."));
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(
            @PathVariable int bookingId
    ) {
        Map<String, Object> booking = findBookingById(bookingId);
        if (booking == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }

        String status = (String) booking.get("status");
        if (!"Requested".equalsIgnoreCase(status) && !"Confirmed".equalsIgnoreCase(status)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only requested/confirmed bookings can be cancelled."));
        }

        // Get the service to calculate cancellation fee
        Object serviceIdObj = booking.get("serviceId");
        if (serviceIdObj == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Booking has no serviceId."));
        }

        int serviceId;
        try {
            serviceId = Integer.parseInt(serviceIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Invalid serviceId on booking."));
        }

        Service service = CatalogService.getInstance().findServiceById(serviceId);
        if (service == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Service for booking not found."));
        }

        // Use the cancellation policy to determine fee
        CancellationPolicy policy = SystemPolicy.getInstance().getCancellationPolicy();
        double cancellationFee = 0.0;
        
        if (policy != null) {
            // Create a simple booking-like object for the policy
            // Since we don't have the full Booking model, we'll calculate based on policy type
            String policyClass = policy.getClass().getSimpleName();
            switch (policyClass) {
                case "FlexibleCancellation" -> cancellationFee = 0.0;
                case "StrictCancellation" -> cancellationFee = SystemPolicy.getInstance().getCancellationFee();
                case "NoRefundCancellation" -> cancellationFee = service.getRate();
                default -> cancellationFee = 0.0;
            }
        }

        booking.put("cancellationFee", cancellationFee);
        booking.put("status", "Cancelled");
        
        return ResponseEntity.ok(Map.of(
                "message", "Booking cancelled successfully.",
                "bookingId", bookingId,
                "cancellationFee", cancellationFee
        ));
    }

    @PostMapping("/bookings")
    public ResponseEntity<Map<String, Object>> createBooking(
            @RequestBody Map<String, Object> body
    ) {
        Object clientIdObj = body.get("clientId");
        Object serviceIdObj = body.get("serviceId");
        Object dateObj = body.get("date");

        if (clientIdObj == null || serviceIdObj == null || dateObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "clientId, serviceId, and date are required."
            ));
        }

        int clientId;
        int serviceId;
        try {
            clientId = Integer.parseInt(clientIdObj.toString());
            serviceId = Integer.parseInt(serviceIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "clientId and serviceId must be numbers."));
        }

        String date = dateObj.toString();

        // Validate client exists
        Map<String, Object> client = clientService.getClientById(clientId);
        if (client == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Client not found. Please login first."
            ));
        }

        Integer selectedTimeslotId = null;
        Object timeslotIdObj = body.get("timeslotId");
        if (timeslotIdObj != null) {
            int timeslotId;
            try {
                timeslotId = Integer.parseInt(timeslotIdObj.toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "timeslotId must be a number."));
            }
            boolean slotFound = false;
            for (Map<String, Object> slot : timeslots) {
                if (((Integer) slot.get("id")).equals(timeslotId)) {
                    if (Boolean.TRUE.equals(slot.get("booked"))) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Timeslot already booked."));
                    }
                    slot.put("booked", true);
                    slotFound = true;
                    selectedTimeslotId = timeslotId;
                    break;
                }
            }
            if (!slotFound) {
                return ResponseEntity.badRequest().body(Map.of("error", "Timeslot not found."));
            }
        }

        Service service = CatalogService.getInstance().findServiceById(serviceId);
        if (service == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid serviceId."
            ));
        }

        int consultantId = consultantIdForService(serviceId);
        String consultantName = service.getConsultantName();

        Map<String, Object> booking = new LinkedHashMap<>();
        booking.put("id", bookingIdCounter.incrementAndGet());
        booking.put("clientId", clientId);
        booking.put("consultantId", consultantId);
        booking.put("clientName", "Client " + clientId);
        booking.put("consultantName", consultantName);
        booking.put("serviceId", serviceId);
        booking.put("serviceName", service.getName());
        booking.put("date", date);
        booking.put("status", "Requested");
        if (selectedTimeslotId != null) {
            booking.put("timeslotId", selectedTimeslotId);
        }

        bookings.add(booking);

        return ResponseEntity.ok(booking);
    }

    @PostMapping("/bookings/{bookingId}/pay")
    public ResponseEntity<Map<String, Object>> payBooking(
            @PathVariable int bookingId,
            @RequestBody Map<String, Object> body
    ) {
        Map<String, Object> booking = findBookingById(bookingId);
        if (booking == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }

        // Check if using saved payment method or new payment details
        Object paymentMethodIdObj = body.get("paymentMethodId");
        Object clientIdObj = booking.get("clientId");
        
        if (clientIdObj == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Booking has no clientId."));
        }
        
        int clientId;
        try {
            clientId = Integer.parseInt(clientIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Invalid clientId on booking."));
        }

        Map<String, Object> paymentDetails = null;
        
        if (paymentMethodIdObj != null) {
            // Using saved payment method
            String paymentMethodId = paymentMethodIdObj.toString();
            List<Map<String, Object>> savedMethods = clientService.getPaymentMethods(clientId);
            
            for (Map<String, Object> method : savedMethods) {
                if (paymentMethodId.equals(method.get("id"))) {
                    paymentDetails = method;
                    break;
                }
            }
            
            if (paymentDetails == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Saved payment method not found."));
            }
        } else {
            // Using new payment details - validate required fields
            Object cardNumber = body.get("cardNumber");
            Object cvv = body.get("cvv");
            Object expiryDate = body.get("expiryDate");

            if (cardNumber == null || cardNumber.toString().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "cardNumber is required."));
            }
            if (cvv == null || cvv.toString().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "cvv is required."));
            }
            if (expiryDate == null || expiryDate.toString().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "expiryDate is required."));
            }

            // Create payment details map from request
            paymentDetails = new LinkedHashMap<>();
            paymentDetails.put("type", "credit-card"); // Default for manual entry
            paymentDetails.put("cardNumber", cardNumber.toString());
            paymentDetails.put("cvv", cvv.toString());
            paymentDetails.put("expiryDate", expiryDate.toString());
        }

        // determine final price based on system policy and service details
        Object serviceIdObj = booking.get("serviceId");
        if (serviceIdObj == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Booking has no serviceId."));
        }

        int serviceId;
        try {
            serviceId = Integer.parseInt(serviceIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Invalid serviceId on booking."));
        }

        Service service = CatalogService.getInstance().findServiceById(serviceId);
        if (service == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Service for booking not found."));
        }

        double finalPrice;
        if (SystemPolicy.getInstance().getPricingStrategy() != null) {
            finalPrice = SystemPolicy.getInstance().getPricingStrategy().finalPrice(service);
        } else {
            finalPrice = service.getRate();
        }

        // payment logic simulation - in real app, would process with payment provider
        booking.put("status", "Paid");
        booking.put("finalPrice", finalPrice);
        booking.put("paymentMethod", paymentDetails.get("type"));
        
        return ResponseEntity.ok(Map.of(
                "message", String.format("Payment processed successfully using %s. Final price is %.2f.", 
                        paymentDetails.get("type"), finalPrice),
                "bookingId", bookingId,
                "status", booking.get("status"),
                "finalPrice", finalPrice,
                "paymentMethod", paymentDetails.get("type")
        ));
    }

    @PostMapping("/timeslots")
    public ResponseEntity<Map<String, Object>> createTimeslot(
            @RequestBody Map<String, Object> body
    ) {
        Object consultantIdObj = body.get("consultantId");
        Object dateObj = body.get("date");
        Object startTimeObj = body.get("startTime");
        Object endTimeObj = body.get("endTime");

        if (consultantIdObj == null || dateObj == null || startTimeObj == null || endTimeObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "consultantId, date, startTime, and endTime are required."
            ));
        }

        int consultantId;
        try {
            consultantId = Integer.parseInt(consultantIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "consultantId must be a number."));
        }

        String date = dateObj.toString();
        String startTime = startTimeObj.toString();
        String endTime = endTimeObj.toString();

        LocalDate.parse(date);
        LocalTime.parse(startTime);
        LocalTime.parse(endTime);

        Map<String, Object> timeslot = new LinkedHashMap<>();
        timeslot.put("consultantId", consultantId);
        timeslot.put("date", date);
        timeslot.put("startTime", startTime);
        timeslot.put("endTime", endTime);

        timeslots.add(timeslot);

        return ResponseEntity.ok(timeslot);
    }

    @PostMapping("/bookings/{bookingId}/{action}")
    public ResponseEntity<Map<String, Object>> updateBookingAction(
            @PathVariable int bookingId,
            @PathVariable String action
    ) {
        Map<String, Object> booking = findBookingById(bookingId);
        if (booking == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }

        String newStatus;
        switch (action.toLowerCase()) {
            case "accept" -> newStatus = "Confirmed";
            case "reject" -> newStatus = "Rejected";
            case "complete" -> newStatus = "Completed";
            default -> {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Action must be accept, reject, or complete."
                ));
            }
        }

        booking.put("status", newStatus);

        return ResponseEntity.ok(Map.of(
                "message", "Booking updated successfully.",
                "bookingId", bookingId,
                "status", newStatus
        ));
    }

    private Map<String, Object> findBookingById(int bookingId) {
        for (Map<String, Object> booking : bookings) {
            if (((Integer) booking.get("id")) == bookingId) {
                return booking;
            }
        }
        return null;
    }

    private int consultantIdForService(int serviceId) {
        return switch (serviceId) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 5;
            default -> 1;
        };
    }

    private Map<String, Object> filterClientBooking(Map<String, Object> booking) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", booking.get("id"));
        result.put("serviceName", booking.get("serviceName"));
        result.put("date", booking.get("date"));
        result.put("status", booking.get("status"));
        return result;
    }

    // Static method to get system statistics
    public static Map<String, Object> getBookingStatistics() {
        // This is a simplified approach - in a real application, we'd have a proper service
        // For now, we'll create a temporary instance to access the data
        BookingController tempController = new BookingController();
        
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalBookings", tempController.bookings.size());
        
        // Calculate revenue from paid bookings
        double totalRevenue = 0.0;
        int paidBookings = 0;
        int completedBookings = 0;
        int cancelledBookings = 0;
        
        for (Map<String, Object> booking : tempController.bookings) {
            String status = (String) booking.get("status");
            switch (status) {
                case "Paid" -> {
                    paidBookings++;
                    Object finalPrice = booking.get("finalPrice");
                    if (finalPrice instanceof Number) {
                        totalRevenue += ((Number) finalPrice).doubleValue();
                    }
                }
                case "Completed" -> completedBookings++;
                case "Cancelled" -> cancelledBookings++;
            }
        }
        
        stats.put("totalRevenue", totalRevenue);
        stats.put("paidBookings", paidBookings);
        stats.put("completedBookings", completedBookings);
        stats.put("cancelledBookings", cancelledBookings);
        
        return stats;
    }
}
