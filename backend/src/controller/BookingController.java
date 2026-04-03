package controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@CrossOrigin(origins = "*")
public class BookingController {

    private final List<Map<String, Object>> services = new ArrayList<>();
    private final List<Map<String, Object>> bookings = new ArrayList<>();
    private final List<Map<String, Object>> timeslots = new ArrayList<>();
    private final AtomicInteger bookingIdCounter = new AtomicInteger(101);

    public BookingController() {
        seedData();
    }

    private void seedData() {
        if (!services.isEmpty()) return;

        services.add(new LinkedHashMap<>(Map.of(
                "id", 1,
                "name", "Tax Consultation",
                "consultantName", "John Doe",
                "price", 150.00
        )));

        services.add(new LinkedHashMap<>(Map.of(
                "id", 2,
                "name", "Career Coaching",
                "consultantName", "Sarah Lee",
                "price", 100.00
        )));

        services.add(new LinkedHashMap<>(Map.of(
                "id", 3,
                "name", "Legal Advice",
                "consultantName", "Alice Johnson",
                "price", 200.00
        )));

        bookings.add(new LinkedHashMap<>(Map.of(
                "id", 101,
                "clientId", 1,
                "consultantId", 1,
                "clientName", "Jane Smith",
                "serviceId", 1,
                "serviceName", "Tax Consultation",
                "date", "2026-04-10T10:00",
                "status", "Requested"
        )));
    }

    @GetMapping("/services")
    public ResponseEntity<List<Map<String, Object>>> getServices() {
        return ResponseEntity.ok(services);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getBookings(
            @RequestParam(required = false) Integer clientId,
            @RequestParam(required = false) Integer consultantId
    ) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> booking : bookings) {
            Integer bookingClientId = (Integer) booking.get("clientId");
            Integer bookingConsultantId = (Integer) booking.get("consultantId");

            if (clientId != null && clientId.equals(bookingClientId)) {
                result.add(filterClientBooking(booking));
            } else if (consultantId != null && consultantId.equals(bookingConsultantId)) {
                result.add(filterConsultantBooking(booking));
            }
        }

        return ResponseEntity.ok(result);
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

        int clientId = Integer.parseInt(clientIdObj.toString());
        int serviceId = Integer.parseInt(serviceIdObj.toString());
        String date = dateObj.toString();

        Map<String, Object> service = findServiceById(serviceId);
        if (service == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid serviceId."
            ));
        }

        int consultantId = consultantIdForService(serviceId);
        String consultantName = service.get("consultantName").toString();

        Map<String, Object> booking = new LinkedHashMap<>();
        booking.put("id", bookingIdCounter.incrementAndGet());
        booking.put("clientId", clientId);
        booking.put("consultantId", consultantId);
        booking.put("clientName", "Client " + clientId);
        booking.put("consultantName", consultantName);
        booking.put("serviceId", serviceId);
        booking.put("serviceName", service.get("name"));
        booking.put("date", date);
        booking.put("status", "Requested");

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

        Object cardNumber = body.get("cardNumber");
        if (cardNumber == null || cardNumber.toString().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "cardNumber is required."));
        }

        booking.put("status", "Paid");

        return ResponseEntity.ok(Map.of(
                "message", "Payment processed successfully.",
                "bookingId", bookingId,
                "status", booking.get("status")
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

        int consultantId = Integer.parseInt(consultantIdObj.toString());
        String date = dateObj.toString();
        String startTime = startTimeObj.toString();
        String endTime = endTimeObj.toString();

        // validate format lightly
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

        booking.put("status", newStatus);

        return ResponseEntity.ok(Map.of(
                "message", "Booking updated successfully.",
                "bookingId", bookingId,
                "status", newStatus
        ));
    }

    private Map<String, Object> findServiceById(int serviceId) {
        for (Map<String, Object> service : services) {
            if (((Integer) service.get("id")) == serviceId) {
                return service;
            }
        }
        return null;
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
        switch (serviceId) {
            case 1: return 1;
            case 2: return 2;
            case 3: return 5;
            default: return 1;
        }
    }

    private Map<String, Object> filterClientBooking(Map<String, Object> booking) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", booking.get("id"));
        result.put("serviceName", booking.get("serviceName"));
        result.put("date", booking.get("date"));
        result.put("status", booking.get("status"));
        return result;
    }

    private Map<String, Object> filterConsultantBooking(Map<String, Object> booking) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", booking.get("id"));
        result.put("clientName", booking.get("clientName"));
        result.put("serviceName", booking.get("serviceName"));
        result.put("date", booking.get("date"));
        result.put("status", booking.get("status"));
        return result;
    }
}