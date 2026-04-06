package controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import model.Booking;
import model.Service;
import model.SystemPolicy;
import policy.CancellationPolicy;
import service.CatalogService;
import service.ClientService;
import service.ConsultantService;
import notification.NotificationService;

@RestController
@CrossOrigin(origins = "*")
public class BookingController {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ConsultantService consultantService;

    // -------------------------------------------------------------------------
    // Timeslots
    // -------------------------------------------------------------------------

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
                    "error", "consultantId, date, startTime, and endTime are required."));
        }

        int consultantId;
        try {
            consultantId = Integer.parseInt(consultantIdObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "consultantId must be a number."));
        }

        java.sql.Date sqlDate;
        java.sql.Time sqlStart;
        java.sql.Time sqlEnd;
        try {
            sqlDate  = java.sql.Date.valueOf(LocalDate.parse(dateObj.toString()));
            sqlStart = java.sql.Time.valueOf(LocalTime.parse(startTimeObj.toString()));
            sqlEnd   = java.sql.Time.valueOf(LocalTime.parse(endTimeObj.toString()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid date or time format."));
        }

        // Verify consultant exists — FK constraint requires it
        List<Map<String, Object>> consultantCheck = jdbc.queryForList(
                "SELECT id FROM consultants WHERE id = ?", consultantId);
        if (consultantCheck.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Consultant not found. Please log out and log in again."));
        }

        List<Map<String, Object>> rows;
        try {
            rows = jdbc.queryForList(
                    "INSERT INTO timeslots (consultant_id, date, start_time, end_time) VALUES (?,?,?,?) RETURNING id",
                    consultantId, sqlDate, sqlStart, sqlEnd);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create timeslot: " + e.getMessage()));
        }

        int newId = ((Number) rows.get(0).get("id")).intValue();
        Map<String, Object> slot = new LinkedHashMap<>();
        slot.put("id", newId);
        slot.put("consultantId", consultantId);
        slot.put("date", dateObj.toString());
        slot.put("startTime", startTimeObj.toString());
        slot.put("endTime", endTimeObj.toString());
        slot.put("booked", false);
        return ResponseEntity.ok(slot);
    }

    @GetMapping("/timeslots")
    public ResponseEntity<List<Map<String, Object>>> getTimeslots(
            @RequestParam(required = false) Integer consultantId
    ) {
        List<Map<String, Object>> rawRows;
        if (consultantId != null) {
            rawRows = jdbc.queryForList(
                    "SELECT id, consultant_id, date, start_time, end_time, booked FROM timeslots WHERE consultant_id = ?",
                    consultantId);
        } else {
            rawRows = jdbc.queryForList(
                    "SELECT id, consultant_id, date, start_time, end_time, booked FROM timeslots");
        }
        // Rename snake_case DB columns to camelCase for frontend
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map<String, Object> row : rawRows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", row.get("id"));
            m.put("consultantId", row.get("consultant_id"));
            m.put("date", row.get("date") != null ? row.get("date").toString() : null);
            m.put("startTime", row.get("start_time") != null ? row.get("start_time").toString() : null);
            m.put("endTime", row.get("end_time") != null ? row.get("end_time").toString() : null);
            m.put("booked", row.get("booked"));
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/timeslots/{timeslotId}")
    public ResponseEntity<Map<String, Object>> deleteTimeslot(
            @PathVariable int timeslotId
    ) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, booked FROM timeslots WHERE id = ?", timeslotId);
        if (rows.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Timeslot not found."));
        }
        if (Boolean.TRUE.equals(rows.get(0).get("booked"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete a booked timeslot."));
        }
        jdbc.update("DELETE FROM timeslots WHERE id = ?", timeslotId);
        return ResponseEntity.ok(Map.of("message", "Timeslot deleted."));
    }

    // -------------------------------------------------------------------------
    // Bookings
    // -------------------------------------------------------------------------

    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getBookings(
            @RequestParam(required = false) Integer clientId,
            @RequestParam(required = false) Integer consultantId
    ) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, client_id AS \"clientId\", consultant_id AS \"consultantId\", " +
                "service_id AS \"serviceId\", service_name AS \"serviceName\", " +
                "consultant_name AS \"consultantName\", timeslot_id AS \"timeslotId\", " +
                "date, status, final_price AS \"finalPrice\", payment_method AS \"paymentMethod\" " +
                "FROM bookings WHERE 1=1");

        java.util.ArrayList<Object> params = new java.util.ArrayList<>();
        if (clientId != null) { sql.append(" AND client_id = ?"); params.add(clientId); }
        if (consultantId != null) { sql.append(" AND consultant_id = ?"); params.add(consultantId); }

        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), params.toArray());
        return ResponseEntity.ok(rows);
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

        Map<String, Object> client = clientService.getClientById(clientId);
        if (client == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Client not found. Please login first."));
        }

        Service service = CatalogService.getInstance().findServiceById(serviceId);
        if (service == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid serviceId."));
        }

        // Resolve timeslot
        Integer timeslotId = null;
        Object timeslotIdObj = body.get("timeslotId");
        if (timeslotIdObj != null) {
            int tsId;
            try {
                tsId = Integer.parseInt(timeslotIdObj.toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "timeslotId must be a number."));
            }
            List<Map<String, Object>> tsRows = jdbc.queryForList(
                    "SELECT id, booked FROM timeslots WHERE id = ?", tsId);
            if (tsRows.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Timeslot not found."));
            }
            if (Boolean.TRUE.equals(tsRows.get(0).get("booked"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Timeslot already booked."));
            }
            jdbc.update("UPDATE timeslots SET booked = TRUE WHERE id = ?", tsId);
            timeslotId = tsId;
        }

        // Resolve consultant
        Integer consultantId = null;
        String consultantName = service.getConsultantName();
        if (consultantName != null && !consultantName.isBlank()) {
            consultantId = consultantService.findConsultantIdByName(consultantName);
        }
        if (consultantId == null) consultantId = 1; // fallback

        List<Map<String, Object>> rows = jdbc.queryForList(
                "INSERT INTO bookings (client_id, consultant_id, service_id, service_name, consultant_name, timeslot_id, date, status) " +
                "VALUES (?,?,?,?,?,?,?,?) RETURNING id",
                clientId, consultantId, serviceId, service.getName(), consultantName,
                timeslotId, dateObj.toString(), "Requested");

        int bookingId = ((Number) rows.get(0).get("id")).intValue();

        // Fire Observer notification
        NotificationService.getInstance().notifyObservers(
                "New booking #" + bookingId + " created by client " + clientId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", bookingId);
        result.put("clientId", clientId);
        result.put("consultantId", consultantId);
        result.put("serviceId", serviceId);
        result.put("serviceName", service.getName());
        result.put("consultantName", consultantName);
        result.put("timeslotId", timeslotId);
        result.put("date", dateObj.toString());
        result.put("status", "Requested");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/bookings/{bookingId}/{action}")
    public ResponseEntity<Map<String, Object>> updateBookingAction(
            @PathVariable int bookingId,
            @PathVariable String action
    ) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, status, timeslot_id FROM bookings WHERE id = ?", bookingId);
        if (rows.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }

        String currentStatus = (String) rows.get(0).get("status");

        // Use State pattern to validate + advance the transition
        Booking b = Booking.forStateTransition(currentStatus);
        String newStatus;
        switch (action.toLowerCase()) {
            case "accept" -> {
                b.confirm();
                newStatus = "PendingPayment";
            }
            case "reject" -> {
                b.reject();
                newStatus = "Rejected";
                freeTimeslot(rows.get(0).get("timeslot_id"));
            }
            case "complete" -> {
                b.complete();
                newStatus = "Completed";
            }
            default -> {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Action must be accept, reject, or complete."));
            }
        }

        jdbc.update("UPDATE bookings SET status = ? WHERE id = ?", newStatus, bookingId);

        NotificationService.getInstance().notifyObservers(
                "Booking #" + bookingId + " transitioned to " + newStatus);

        return ResponseEntity.ok(Map.of(
                "message", "Booking updated successfully.",
                "bookingId", bookingId,
                "status", newStatus));
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(
            @PathVariable int bookingId
    ) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, status, service_id, timeslot_id, client_id FROM bookings WHERE id = ?", bookingId);
        if (rows.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }

        Map<String, Object> row = rows.get(0);
        String currentStatus = (String) row.get("status");

        // Use State pattern to validate cancellability
        Booking b = Booking.forStateTransition(currentStatus);
        b.cancel(); // will succeed for Requested/PendingPayment/Paid states

        int serviceId = ((Number) row.get("service_id")).intValue();
        Service service = CatalogService.getInstance().findServiceById(serviceId);

        CancellationPolicy policy = SystemPolicy.getInstance().getCancellationPolicy();
        double cancellationFee = 0.0;
        if (policy != null && service != null) {
            String policyClass = policy.getClass().getSimpleName();
            cancellationFee = switch (policyClass) {
                case "FlexibleCancellation" -> 0.0;
                case "StrictCancellation" -> SystemPolicy.getInstance().getCancellationFee();
                case "NoRefundCancellation" -> service.getRate();
                default -> 0.0;
            };
        }

        jdbc.update("UPDATE bookings SET status = 'Cancelled', cancellation_fee = ? WHERE id = ?",
                cancellationFee, bookingId);
        freeTimeslot(row.get("timeslot_id"));

        NotificationService.getInstance().notifyObservers(
                "Booking #" + bookingId + " cancelled. Fee: " + cancellationFee);

        return ResponseEntity.ok(Map.of(
                "message", "Booking cancelled successfully.",
                "bookingId", bookingId,
                "cancellationFee", cancellationFee));
    }

    @PostMapping("/bookings/{bookingId}/pay")
    public ResponseEntity<Map<String, Object>> payBooking(
            @PathVariable int bookingId,
            @RequestBody Map<String, Object> body
    ) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, status, service_id, client_id FROM bookings WHERE id = ?", bookingId);
        if (rows.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }

        Map<String, Object> row = rows.get(0);
        String currentStatus = (String) row.get("status");

        if (!"PendingPayment".equalsIgnoreCase(currentStatus)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Booking must be in PendingPayment status before paying. Current: " + currentStatus));
        }

        int clientId = ((Number) row.get("client_id")).intValue();
        int serviceId = ((Number) row.get("service_id")).intValue();

        Service service = CatalogService.getInstance().findServiceById(serviceId);
        if (service == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Service for booking not found."));
        }

        // Resolve payment method details
        String paymentType = "credit-card";
        Object paymentMethodIdObj = body.get("paymentMethodId");
        if (paymentMethodIdObj != null) {
            List<Map<String, Object>> methods = clientService.getPaymentMethods(clientId);
            boolean found = false;
            for (Map<String, Object> m : methods) {
                if (paymentMethodIdObj.toString().equals(m.get("id"))) {
                    paymentType = (String) m.get("type");
                    found = true;
                    break;
                }
            }
            if (!found) {
                return ResponseEntity.badRequest().body(Map.of("error", "Saved payment method not found."));
            }
        } else {
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
            Object typeObj = body.get("type");
            if (typeObj != null) paymentType = typeObj.toString();
        }

        // Calculate final price via Strategy pattern
        double finalPrice;
        if (SystemPolicy.getInstance().getPricingStrategy() != null) {
            finalPrice = SystemPolicy.getInstance().getPricingStrategy().finalPrice(service);
        } else {
            finalPrice = service.getRate();
        }

        // Use State pattern to advance to Paid
        Booking b = Booking.forStateTransition(currentStatus);
        b.pay();

        jdbc.update("UPDATE bookings SET status = 'Paid', final_price = ?, payment_method = ? WHERE id = ?",
                finalPrice, paymentType, bookingId);

        // Record in payment_history (UC7)
        // paymentType = actual instrument (credit-card, paypal, …); "booking-payment" = transaction category
        clientService.recordPayment(clientId, bookingId, finalPrice, paymentType, "booking-payment");

        NotificationService.getInstance().notifyObservers(
                "Booking #" + bookingId + " paid. Amount: " + finalPrice);

        return ResponseEntity.ok(Map.of(
                "message", String.format("Payment processed successfully using %s. Final price: %.2f.", paymentType, finalPrice),
                "bookingId", bookingId,
                "status", "Paid",
                "finalPrice", finalPrice,
                "paymentMethod", paymentType));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void freeTimeslot(Object timeslotIdObj) {
        if (timeslotIdObj != null) {
            int tsId = ((Number) timeslotIdObj).intValue();
            jdbc.update("UPDATE timeslots SET booked = FALSE WHERE id = ?", tsId);
        }
    }
}
