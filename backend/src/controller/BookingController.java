package controller;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import service.CatalogService;
import service.ClientService;

@RestController
@CrossOrigin(origins = "*")
public class BookingController {

    private final JdbcTemplate jdbcTemplate;
    private final CatalogService catalogService;
    private final ClientService clientService;

    public BookingController(JdbcTemplate jdbcTemplate,
                             CatalogService catalogService,
                             ClientService clientService) {
        this.jdbcTemplate = jdbcTemplate;
        this.catalogService = catalogService;
        this.clientService = clientService;
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getBookings(
            @RequestParam(required = false) Integer clientId,
            @RequestParam(required = false) Integer consultantId) {

        StringBuilder sql = new StringBuilder(
            "SELECT id, client_id AS \"clientId\", consultant_id AS \"consultantId\", " +
            "client_name AS \"clientName\", consultant_name AS \"consultantName\", " +
            "service_id AS \"serviceId\", service_name AS \"serviceName\", " +
            "booking_time AS date, status, final_price AS \"finalPrice\", " +
            "payment_method AS \"paymentMethod\", cancellation_fee AS \"cancellationFee\", " +
            "timeslot_id AS \"timeslotId\" " +
            "FROM bookings WHERE 1=1 "
        );

        if (clientId != null) {
            sql.append("AND client_id = ").append(clientId).append(" ");
        }
        if (consultantId != null) {
            sql.append("AND consultant_id = ").append(consultantId).append(" ");
        }

        sql.append("ORDER BY id");
        return ResponseEntity.ok(jdbcTemplate.queryForList(sql.toString()));
    }

    @GetMapping("/timeslots")
    public ResponseEntity<List<Map<String, Object>>> getTimeslots(
            @RequestParam(required = false) Integer consultantId) {

        if (consultantId == null) {
            return ResponseEntity.ok(
                jdbcTemplate.queryForList(
                    "SELECT id, consultant_id AS \"consultantId\", slot_date AS date, " +
                    "start_time AS \"startTime\", end_time AS \"endTime\", booked " +
                    "FROM timeslots ORDER BY id"
                )
            );
        }

        return ResponseEntity.ok(
            jdbcTemplate.queryForList(
                "SELECT id, consultant_id AS \"consultantId\", slot_date AS date, " +
                "start_time AS \"startTime\", end_time AS \"endTime\", booked " +
                "FROM timeslots WHERE consultant_id = ? ORDER BY id",
                consultantId
            )
        );
    }

    @DeleteMapping("/timeslots/{timeslotId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteTimeslot(@PathVariable int timeslotId) {
        Map<String, Object> slot;

        try {
            slot = jdbcTemplate.queryForMap(
                "SELECT id, booked FROM timeslots WHERE id = ? FOR UPDATE",
                timeslotId
            );
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(404).body(Map.of("error", "Timeslot not found."));
        }

        if (Boolean.TRUE.equals(slot.get("booked"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cannot delete a booked timeslot."));
        }

        jdbcTemplate.update("DELETE FROM timeslots WHERE id = ?", timeslotId);
        return ResponseEntity.ok(Map.of("message", "Timeslot deleted."));
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    @Transactional
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable int bookingId) {
        Map<String, Object> booking = getBookingById(bookingId);

        if (booking == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }

        String status = booking.get("status").toString();
        if (!"Requested".equalsIgnoreCase(status) && !"Confirmed".equalsIgnoreCase(status)) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Only requested/confirmed bookings can be cancelled."
            ));
        }

        int serviceId = Integer.parseInt(booking.get("serviceId").toString());
        Map<String, Object> service = catalogService.findServiceById(serviceId);

        if (service == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Service for booking not found."));
        }

        Map<String, Object> policyRow = getPolicyRow();
        String cancellationPolicy = policyRow.get("cancellationPolicy").toString().toLowerCase();

        double cancellationFee;
        if (cancellationPolicy.contains("strict")) {
            cancellationFee = Double.parseDouble(policyRow.get("cancellationFee").toString());
        } else if (cancellationPolicy.contains("norefund") || cancellationPolicy.contains("no refund")) {
            cancellationFee = Double.parseDouble(service.get("price").toString());
        } else {
            cancellationFee = 0.0;
        }

        jdbcTemplate.update(
            "UPDATE bookings SET status = 'Cancelled', cancellation_fee = ? WHERE id = ?",
            cancellationFee, bookingId
        );

        if (booking.get("timeslotId") != null) {
            jdbcTemplate.update(
                "UPDATE timeslots SET booked = FALSE WHERE id = ?",
                Integer.parseInt(booking.get("timeslotId").toString())
            );
        }

        return ResponseEntity.ok(Map.of(
            "message", "Booking cancelled successfully.",
            "bookingId", bookingId,
            "cancellationFee", cancellationFee
        ));
    }

    @PostMapping("/bookings")
    @Transactional
    public ResponseEntity<Map<String, Object>> createBooking(@RequestBody Map<String, Object> body) {
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
            return ResponseEntity.badRequest().body(Map.of(
                "error", "clientId and serviceId must be numbers."
            ));
        }

        Map<String, Object> client = clientService.getClientById(clientId);
        if (client == null) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Client not found. Please login first."
            ));
        }

        Map<String, Object> service = catalogService.findServiceById(serviceId);
        if (service == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid serviceId."));
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

            Map<String, Object> slot;
            try {
                slot = jdbcTemplate.queryForMap(
                    "SELECT id, booked FROM timeslots WHERE id = ? FOR UPDATE",
                    timeslotId
                );
            } catch (EmptyResultDataAccessException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Timeslot not found."));
            }

            if (Boolean.TRUE.equals(slot.get("booked"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Timeslot already booked."));
            }

            jdbcTemplate.update(
                "UPDATE timeslots SET booked = TRUE WHERE id = ?",
                timeslotId
            );
            selectedTimeslotId = timeslotId;
        }

        int consultantId = service.get("consultantId") == null
                ? 0
                : Integer.parseInt(service.get("consultantId").toString());

        String consultantName = service.get("consultantName").toString();
        String serviceName = service.get("name").toString();
        String clientName = client.get("name").toString();
        String date = dateObj.toString();

        Integer bookingId = jdbcTemplate.queryForObject(
            "INSERT INTO bookings " +
            "(client_id, consultant_id, service_id, client_name, consultant_name, service_name, booking_time, status, timeslot_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 'Requested', ?) RETURNING id",
            Integer.class,
            clientId,
            consultantId,
            serviceId,
            clientName,
            consultantName,
            serviceName,
            Timestamp.valueOf(LocalDateTime.parse(date)),
            selectedTimeslotId
        );

        return ResponseEntity.ok(getBookingById(bookingId));
    }

    @PostMapping("/bookings/{bookingId}/pay")
    @Transactional
    public ResponseEntity<Map<String, Object>> payBooking(
            @PathVariable int bookingId,
            @RequestBody Map<String, Object> body) {

        Map<String, Object> booking = getBookingById(bookingId);
        if (booking == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }

        int clientId = Integer.parseInt(booking.get("clientId").toString());
        int serviceId = Integer.parseInt(booking.get("serviceId").toString());

        Map<String, Object> service = catalogService.findServiceById(serviceId);
        if (service == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Service for booking not found."));
        }

        Map<String, Object> paymentDetails = null;

        if (body.get("paymentMethodId") != null) {
            String paymentMethodId = body.get("paymentMethodId").toString();
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

            paymentDetails = new LinkedHashMap<>();
            paymentDetails.put("type", "credit-card");
        }

        double basePrice = Double.parseDouble(service.get("price").toString());
        Map<String, Object> policyRow = getPolicyRow();
        String pricingStrategy = policyRow.get("pricingStrategy").toString().toLowerCase();

        double finalPrice = pricingStrategy.contains("tax")
                ? Math.round(basePrice * 1.13 * 100.0) / 100.0
                : basePrice;

        jdbcTemplate.update(
            "UPDATE bookings SET status = 'Paid', final_price = ?, payment_method = ? WHERE id = ?",
            finalPrice,
            paymentDetails.get("type"),
            bookingId
        );

        return ResponseEntity.ok(Map.of(
            "message", String.format(
                "Payment processed successfully using %s. Final price is %.2f.",
                paymentDetails.get("type"), finalPrice
            ),
            "bookingId", bookingId,
            "status", "Paid",
            "finalPrice", finalPrice,
            "paymentMethod", paymentDetails.get("type")
        ));
    }

    @PostMapping("/timeslots")
    @Transactional
    public ResponseEntity<Map<String, Object>> createTimeslot(@RequestBody Map<String, Object> body) {
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

        LocalDate date = LocalDate.parse(dateObj.toString());
        LocalTime startTime = LocalTime.parse(startTimeObj.toString());
        LocalTime endTime = LocalTime.parse(endTimeObj.toString());

        Integer id = jdbcTemplate.queryForObject(
            "INSERT INTO timeslots (consultant_id, slot_date, start_time, end_time, booked) " +
            "VALUES (?, ?, ?, ?, FALSE) RETURNING id",
            Integer.class,
            consultantId, date, startTime, endTime
        );

        return ResponseEntity.ok(
            jdbcTemplate.queryForMap(
                "SELECT id, consultant_id AS \"consultantId\", slot_date AS date, " +
                "start_time AS \"startTime\", end_time AS \"endTime\", booked " +
                "FROM timeslots WHERE id = ?",
                id
            )
        );
    }

    @PostMapping("/bookings/{bookingId}/{action}")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateBookingAction(
            @PathVariable int bookingId,
            @PathVariable String action) {

        Map<String, Object> booking = getBookingById(bookingId);
        if (booking == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Booking not found."));
        }

        String newStatus;
        switch (action.toLowerCase()) {
            case "accept":
                newStatus = "Confirmed";
                break;
            case "reject":
                newStatus = "Rejected";
                break;
            case "complete":
                newStatus = "Completed";
                break;
            default:
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Action must be accept, reject, or complete."
                ));
        }

        jdbcTemplate.update(
            "UPDATE bookings SET status = ? WHERE id = ?",
            newStatus, bookingId
        );

        return ResponseEntity.ok(Map.of(
            "message", "Booking updated successfully.",
            "bookingId", bookingId,
            "status", newStatus
        ));
    }

    private Map<String, Object> getBookingById(int bookingId) {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT id, client_id AS \"clientId\", consultant_id AS \"consultantId\", " +
                "client_name AS \"clientName\", consultant_name AS \"consultantName\", " +
                "service_id AS \"serviceId\", service_name AS \"serviceName\", " +
                "booking_time AS date, status, final_price AS \"finalPrice\", " +
                "payment_method AS \"paymentMethod\", cancellation_fee AS \"cancellationFee\", " +
                "timeslot_id AS \"timeslotId\" " +
                "FROM bookings WHERE id = ?",
                bookingId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Map<String, Object> getPolicyRow() {
        try {
            return jdbcTemplate.queryForMap(
                "SELECT pricing_strategy AS \"pricingStrategy\", " +
                "cancellation_policy AS \"cancellationPolicy\", " +
                "cancellation_fee AS \"cancellationFee\", " +
                "notifications_enabled AS \"notificationsEnabled\", " +
                "refunds_enabled AS \"refundsEnabled\" " +
                "FROM system_policies WHERE id = 1"
            );
        } catch (EmptyResultDataAccessException e) {
            Map<String, Object> defaults = new LinkedHashMap<>();
            defaults.put("pricingStrategy", "BasePrice");
            defaults.put("cancellationPolicy", "Flexible");
            defaults.put("cancellationFee", 20.0);
            defaults.put("notificationsEnabled", true);
            defaults.put("refundsEnabled", true);
            return defaults;
        }
    }
}