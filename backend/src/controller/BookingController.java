package controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import model.Booking;
import model.Service;
import model.SystemPolicy;
import notification.NotificationService;
import policy.CancellationPolicy;
import service.CatalogService;
import service.ClientService;
import service.ConsultantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ConsultantService consultantService;

    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getBookings(
            @RequestParam(required = false) Integer clientId,
            @RequestParam(required = false) Integer consultantId) {

        String sql = "SELECT * FROM bookings WHERE 1=1";
        List<Object> params = new ArrayList<>();
        if (clientId != null) {
            sql += " AND client_id = ?";
            params.add(clientId);
        }
        if (consultantId != null) {
            sql += " AND consultant_id = ?";
            params.add(consultantId);
        }
        sql += " ORDER BY id DESC";

        List<Map<String, Object>> rows = jdbc.queryForList(sql, params.toArray());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(toBookingMap(row));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/timeslots")
    public ResponseEntity<List<Map<String, Object>>> getTimeslots(
            @RequestParam(required = false) Integer consultantId) {

        String sql = "SELECT * FROM timeslots WHERE 1=1";
        List<Object> params = new ArrayList<>();
        if (consultantId != null) {
            sql += " AND consultant_id = ?";
            params.add(consultantId);
        }
        sql += " ORDER BY date, start_time";

        List<Map<String, Object>> rows = jdbc.queryForList(sql, params.toArray());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(toTimeslotMap(row));
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/timeslots/{timeslotId}")
    public ResponseEntity<Map<String, Object>> deleteTimeslot(@PathVariable int timeslotId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM timeslots WHERE id = ?", timeslotId);
        if (rows.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Timeslot not found."));
        }
        Boolean booked = (Boolean) rows.get(0).get("booked");
        if (Boolean.TRUE.equals(booked)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete a booked timeslot."));
        }
        jdbc.update("DELETE FROM timeslots WHERE id = ?", timeslotId);
        return ResponseEntity.ok(Map.of("message", "Timeslot deleted."));
    }

    @PostMapping("/timeslots")
    public ResponseEntity<Map<String, Object>> createTimeslot(@RequestBody Map<String, Object> body) {
        Object consultantIdObj = body.get("consultantId");
        Object dateObj = body.get("date");
        Object startTimeObj = body.get("startTime");
        Object endTimeObj = body.get("endTime");

        if (consultantIdObj == null || dateObj == null || startTimeObj == null || endTimeObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "consultantId, date, startTime, and endTime are required."));
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

        try {
            LocalDate.parse(date);
            LocalTime.parse(startTime);
            LocalTime.parse(endTime);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date or time format."));
        }

        // Insert and get generated id
        Integer newId = jdbc.queryForObject(
                "INSERT INTO timeslots (consultant_id, date, start_time, end_time, booked) VALUES (?, ?::date, ?::time, ?::time, false) RETURNING id",
                Integer.class, consultantId, date, startTime, endTime);

        Map<String, Object> timeslot = new LinkedHashMap<>();
        timeslot.put("id", newId);
        timeslot.put("consultantId", consultantId);
        timeslot.put("date", date);
        timeslot.put("startTime", startTime);
        timeslot.put("endTime", endTime);
        timeslot.put("booked", false);
        return ResponseEntity.ok(timeslot);
    }

    @PostMapping("/bookings")
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> body) {
        Object clientIdObj = body.get("clientId");
        Object serviceIdObj = body.get("serviceId");
        Object dateObj = body.get("date");

        if (clientIdObj == null || serviceIdObj == null || dateObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "clientId, serviceId, and date are required."));
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

        // Validate client
        Map<String, Object> client = clientService.getClientById(clientId);
        if (client == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Client not found. Please login first."));
        }

        // Validate service
        Service service = CatalogService.getInstance().findServiceById(serviceId);
        if (service == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid serviceId."));
        }

        // Handle optional timeslot
        Integer selectedTimeslotId = null;
        Object timeslotIdObj = body.get("timeslotId");
        if (timeslotIdObj != null && !timeslotIdObj.toString().isEmpty()) {
            int timeslotId;
            try {
                timeslotId = Integer.parseInt(timeslotIdObj.toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "timeslotId must be a number."));
            }
            List<Map<String, Object>> slots = jdbc.queryForList(
                    "SELECT * FROM timeslots WHERE id = ?", timeslotId);
            if (slots.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Timeslot not found."));
            }
            if (Boolean.TRUE.equals(slots.get(0).get("booked"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Timeslot already booked."));
            }
            jdbc.update("UPDATE timeslots SET booked = true WHERE id = ?", timeslotId);
            selectedTimeslotId = timeslotId;
        }

        // Resolve consultant ID by name from service (or default to 0)
        String consultantName = service.getConsultantName();
        Integer consultantId = consultantService.findConsultantIdByName(consultantName);
        if (consultantId == null) consultantId = 0;

        String clientName = client.get("name") != null ? client.get("name").toString() : "Client " + clientId;

        // Insert booking
        Integer bookingId = jdbc.queryForObject(
                "INSERT INTO bookings (client_id, consultant_id, service_id, service_name, consultant_name, client_name, date, status, timeslot_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'Requested', ?) RETURNING id",
                Integer.class,
                clientId, consultantId, serviceId, service.getName(), consultantName, clientName,
                date, selectedTimeslotId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", bookingId);
        result.put("clientId", clientId);
        result.put("consultantId", consultantId);
        result.put("serviceId", serviceId);
        result.put("serviceName", service.getName());
        result.put("consultantName", consultantName);
        result.put("clientName", clientName);
        result.put("date", date);
        result.put("status", "Requested");
        if (selectedTimeslotId != null) result.put("timeslotId", selectedTimeslotId);

        // Notify (Observer pattern)
        NotificationService.getInstance().notifyObservers(
                "New booking #" + bookingId + " requested by client " + clientId);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable int bookingId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM bookings WHERE id = ?", bookingId);
        if (rows.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }
        Map<String, Object> booking = rows.get(0);
        String status = (String) booking.get("status");

        if (!"Requested".equalsIgnoreCase(status)
                && !"Confirmed".equalsIgnoreCase(status)
                && !"PendingPayment".equalsIgnoreCase(status)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Only Requested, Confirmed, or PendingPayment bookings can be cancelled."));
        }

        Object serviceIdObj = booking.get("service_id");
        int serviceId = ((Number) serviceIdObj).intValue();
        Service service = CatalogService.getInstance().findServiceById(serviceId);
        if (service == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Service for booking not found."));
        }

        CancellationPolicy policy = SystemPolicy.getInstance().getCancellationPolicy();
        double cancellationFee = 0.0;
        if (policy != null) {
            String policyClass = policy.getClass().getSimpleName();
            switch (policyClass) {
                case "FlexibleCancellation" -> cancellationFee = 0.0;
                case "StrictCancellation" -> cancellationFee = SystemPolicy.getInstance().getCancellationFee();
                case "NoRefundCancellation" -> cancellationFee = service.getRate();
                default -> cancellationFee = 0.0;
            }
        }

        // Drive transition through the real State Pattern
        Booking bookingState = Booking.forStateTransition(status);
        bookingState.cancel();
        String newStatus = bookingState.getStateName(); // "Cancelled"

        jdbc.update("UPDATE bookings SET status = ?, cancellation_fee = ? WHERE id = ?",
                newStatus, cancellationFee, bookingId);

        // Free up timeslot if any
        Object timeslotId = booking.get("timeslot_id");
        if (timeslotId != null) {
            jdbc.update("UPDATE timeslots SET booked = false WHERE id = ?", timeslotId);
        }

        // Notify (Observer pattern fires to subscribed observers)
        NotificationService.getInstance().notifyObservers(
                "Booking #" + bookingId + " has been cancelled. Fee: $" + cancellationFee);

        return ResponseEntity.ok(Map.of(
                "message", "Booking cancelled successfully.",
                "bookingId", bookingId,
                "cancellationFee", cancellationFee));
    }

    @PostMapping("/bookings/{bookingId}/pay")
    public ResponseEntity<Map<String, Object>> payBooking(
            @PathVariable int bookingId,
            @RequestBody Map<String, Object> body) {

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM bookings WHERE id = ?", bookingId);
        if (rows.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }
        Map<String, Object> booking = rows.get(0);
        String status = (String) booking.get("status");

        // Validate booking is in a payable state
        if (!"PendingPayment".equalsIgnoreCase(status) && !"Confirmed".equalsIgnoreCase(status)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Payment can only be processed for bookings in PendingPayment or Confirmed status. Current status: " + status));
        }

        int clientId = ((Number) booking.get("client_id")).intValue();

        // Resolve payment details
        Map<String, Object> paymentDetails;
        Object paymentMethodIdObj = body.get("paymentMethodId");

        if (paymentMethodIdObj != null) {
            // Use saved payment method
            String paymentMethodId = paymentMethodIdObj.toString();
            List<Map<String, Object>> methods = clientService.getPaymentMethods(clientId);
            paymentDetails = methods.stream()
                    .filter(m -> paymentMethodId.equals(m.get("id")))
                    .findFirst()
                    .orElse(null);
            if (paymentDetails == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Saved payment method not found."));
            }
        } else {
            // New payment details
            String type = body.get("type") != null ? body.get("type").toString() : "credit-card";
            paymentDetails = new LinkedHashMap<>();
            paymentDetails.put("type", type);
            paymentDetails.put("cardNumber", body.get("cardNumber"));
            paymentDetails.put("cvv", body.get("cvv"));
            paymentDetails.put("expiryDate", body.get("expiryDate"));
        }

        // Get service and apply pricing strategy
        int serviceId = ((Number) booking.get("service_id")).intValue();
        Service service = CatalogService.getInstance().findServiceById(serviceId);
        if (service == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Service not found."));
        }

        double finalPrice = SystemPolicy.getInstance().getPricingStrategy() != null
                ? SystemPolicy.getInstance().getPricingStrategy().finalPrice(service)
                : service.getRate();

        String methodType = paymentDetails.get("type") != null ? paymentDetails.get("type").toString() : "credit-card";
        String transactionId = UUID.randomUUID().toString();

        // Drive transition through the real State Pattern
        Booking bookingState = Booking.forStateTransition(status);
        bookingState.pay();
        String newStatus = bookingState.getStateName(); // "Paid"

        jdbc.update("UPDATE bookings SET status = ?, final_price = ?, payment_method = ? WHERE id = ?",
                newStatus, finalPrice, methodType, bookingId);

        // Record in payment history (UC7)
        clientService.recordPayment(clientId, bookingId, finalPrice, methodType, transactionId);

        // Notify
        NotificationService.getInstance().notifyObservers(
                "Payment of $" + String.format("%.2f", finalPrice) + " received for booking #" + bookingId);

        return ResponseEntity.ok(Map.of(
                "message", String.format("Payment processed successfully using %s. Final price: $%.2f.", methodType, finalPrice),
                "bookingId", bookingId,
                "status", "Paid",
                "finalPrice", finalPrice,
                "paymentMethod", methodType,
                "transactionId", transactionId));
    }

    @PostMapping("/bookings/{bookingId}/{action}")
    public ResponseEntity<Map<String, Object>> updateBookingAction(
            @PathVariable int bookingId,
            @PathVariable String action) {

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM bookings WHERE id = ?", bookingId);
        if (rows.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }

        if (!action.equalsIgnoreCase("accept")
                && !action.equalsIgnoreCase("reject")
                && !action.equalsIgnoreCase("complete")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Action must be accept, reject, or complete."));
        }

        // Drive transition through the real State Pattern
        String currentStatus = (String) rows.get(0).get("status");
        Booking bookingState = Booking.forStateTransition(currentStatus);

        switch (action.toLowerCase()) {
            // accept: Requested → Confirmed → PendingPayment (two confirms, matching the state machine design)
            case "accept" -> { bookingState.confirm(); bookingState.confirm(); }
            case "reject"   -> bookingState.reject();
            case "complete" -> bookingState.complete();
        }

        String newStatus = bookingState.getStateName();
        jdbc.update("UPDATE bookings SET status = ? WHERE id = ?", newStatus, bookingId);

        // Notify relevant parties
        int clientId = ((Number) rows.get(0).get("client_id")).intValue();
        String msg = switch (newStatus) {
            case "PendingPayment" -> "Booking #" + bookingId + " accepted. Please proceed with payment.";
            case "Rejected" -> "Booking #" + bookingId + " was rejected by the consultant.";
            case "Completed" -> "Booking #" + bookingId + " has been completed.";
            default -> "Booking #" + bookingId + " status updated to " + newStatus;
        };
        NotificationService.getInstance().notifyObservers(msg);

        return ResponseEntity.ok(Map.of(
                "message", "Booking updated successfully.",
                "bookingId", bookingId,
                "status", newStatus));
    }

    // Helper: DB row to API map
    private Map<String, Object> toBookingMap(Map<String, Object> row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", row.get("id"));
        m.put("clientId", row.get("client_id"));
        m.put("consultantId", row.get("consultant_id"));
        m.put("serviceId", row.get("service_id"));
        m.put("serviceName", row.get("service_name"));
        m.put("consultantName", row.get("consultant_name"));
        m.put("clientName", row.get("client_name"));
        m.put("date", row.get("date") != null ? row.get("date").toString() : null);
        m.put("status", row.get("status"));
        m.put("timeslotId", row.get("timeslot_id"));
        m.put("finalPrice", row.get("final_price"));
        m.put("paymentMethod", row.get("payment_method"));
        m.put("cancellationFee", row.get("cancellation_fee"));
        return m;
    }

    private Map<String, Object> toTimeslotMap(Map<String, Object> row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", row.get("id"));
        m.put("consultantId", row.get("consultant_id"));
        m.put("date", row.get("date") != null ? row.get("date").toString() : null);
        m.put("startTime", row.get("start_time") != null ? row.get("start_time").toString() : null);
        m.put("endTime", row.get("end_time") != null ? row.get("end_time").toString() : null);
        m.put("booked", row.get("booked"));
        return m;
    }
}
