package controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import model.SystemPolicy;
import policy.CancellationPolicy;
import policy.FlexibleCancellation;
import policy.NoRefundCancellation;
import policy.StrictCancellation;
import service.ClientService;
import service.ConsultantService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ConsultantService consultantService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private JdbcTemplate jdbc;

    private final Map<String, Object> policies = new LinkedHashMap<>();

    public AdminController() {
        policies.put("pricingStrategy", "Base");
        policies.put("cancellationPolicy", "Flexible");
        policies.put("cancellationFee", 20.0);
        SystemPolicy.getInstance().setCancellationPolicy(new FlexibleCancellation());
        SystemPolicy.getInstance().setCancellationFee(20.0);
    }

    @GetMapping("/consultants/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingConsultants() {
        return ResponseEntity.ok(consultantService.getPendingConsultants());
    }

    @GetMapping("/consultants/approved")
    public ResponseEntity<List<Map<String, Object>>> getApprovedConsultants() {
        return ResponseEntity.ok(consultantService.getApprovedConsultants());
    }

    @PostMapping("/consultants/{consultantId}/{action}")
    public ResponseEntity<Map<String, Object>> updateConsultantStatus(
            @PathVariable int consultantId,
            @PathVariable String action
    ) {
        if (!action.equalsIgnoreCase("approve") && !action.equalsIgnoreCase("reject")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Action must be approve or reject."));
        }

        if (consultantService.getConsultantById(consultantId) == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Consultant not found."));
        }

        if (action.equalsIgnoreCase("approve")) {
            consultantService.approveConsultant(consultantId);
        } else {
            consultantService.rejectConsultant(consultantId);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Consultant " + action.toLowerCase() + "d successfully.",
                "consultantId", consultantId));
    }

    @GetMapping("/policies")
    public ResponseEntity<Map<String, Object>> getPolicies() {
        return ResponseEntity.ok(policies);
    }

    @PostMapping("/policies")
    public ResponseEntity<Map<String, Object>> updatePolicies(
            @RequestBody Map<String, Object> body
    ) {
        Object pricingStrategy = body.get("pricingStrategy");
        Object cancellationPolicy = body.get("cancellationPolicy");
        Object cancellationFee = body.get("cancellationFee");

        if (pricingStrategy != null) {
            policies.put("pricingStrategy", pricingStrategy.toString());
        }
        if (cancellationPolicy != null) {
            policies.put("cancellationPolicy", cancellationPolicy.toString());
            CancellationPolicy policy = switch (cancellationPolicy.toString().toLowerCase()) {
                case "flexible" -> new FlexibleCancellation();
                case "strict" -> new StrictCancellation();
                case "norefund" -> new NoRefundCancellation();
                default -> throw new IllegalArgumentException("Invalid cancellation policy.");
            };
            SystemPolicy.getInstance().setCancellationPolicy(policy);
        }
        if (cancellationFee != null) {
            try {
                double fee = Double.parseDouble(cancellationFee.toString());
                policies.put("cancellationFee", fee);
                SystemPolicy.getInstance().setCancellationFee(fee);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "cancellationFee must be a number."));
            }
        }

        return ResponseEntity.ok(policies);
    }

    @GetMapping("/system-status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new LinkedHashMap<>();

        // Consultant counts
        List<Map<String, Object>> pending = consultantService.getPendingConsultants();
        List<Map<String, Object>> approved = consultantService.getApprovedConsultants();
        status.put("totalConsultants", pending.size() + approved.size());
        status.put("approvedConsultants", approved.size());
        status.put("pendingConsultants", pending.size());

        // Client count
        status.put("totalClients", clientService.getTotalClients());

        // Booking statistics from DB
        Integer totalBookings = jdbc.queryForObject("SELECT COUNT(*) FROM bookings", Integer.class);
        Double totalRevenue = jdbc.queryForObject(
                "SELECT COALESCE(SUM(final_price),0) FROM bookings WHERE status IN ('Paid','Completed')", Double.class);
        Integer paidBookings = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE status = 'Paid'", Integer.class);
        Integer completedBookings = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE status = 'Completed'", Integer.class);
        Integer cancelledBookings = jdbc.queryForObject(
                "SELECT COUNT(*) FROM bookings WHERE status = 'Cancelled'", Integer.class);

        status.put("totalBookings", totalBookings == null ? 0 : totalBookings);
        status.put("totalRevenue", totalRevenue == null ? 0.0 : totalRevenue);
        status.put("paidBookings", paidBookings == null ? 0 : paidBookings);
        status.put("completedBookings", completedBookings == null ? 0 : completedBookings);
        status.put("cancelledBookings", cancelledBookings == null ? 0 : cancelledBookings);

        return ResponseEntity.ok(status);
    }
}
