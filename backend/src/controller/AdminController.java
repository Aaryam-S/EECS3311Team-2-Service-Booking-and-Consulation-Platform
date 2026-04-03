package controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
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

    private final ConsultantService consultantService = ConsultantService.getInstance();
    private final ClientService clientService = ClientService.getInstance();
    private final Map<String, Object> policies = new LinkedHashMap<>();

    public AdminController() {
        // Initialize with default policies
        policies.put("pricingStrategy", "Base");
        policies.put("cancellationPolicy", "Flexible");
        policies.put("cancellationFee", 20.0);
        
        // Set initial cancellation policy
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
                    "error", "Action must be approve or reject."
            ));
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
                "consultantId", consultantId
        ));
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
            
            // Set the actual cancellation policy on SystemPolicy
            CancellationPolicy policy = switch (cancellationPolicy.toString().toLowerCase()) {
                case "flexible" -> new FlexibleCancellation();
                case "strict" -> new StrictCancellation();
                case "norefund" -> new NoRefundCancellation();
                default -> throw new IllegalArgumentException("Invalid cancellation policy. Must be Flexible, Strict, or NoRefund.");
            };
            SystemPolicy.getInstance().setCancellationPolicy(policy);
        }
        if (cancellationFee != null) {
            try {
                double fee = Double.parseDouble(cancellationFee.toString());
                policies.put("cancellationFee", fee);
                SystemPolicy.getInstance().setCancellationFee(fee);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "cancellationFee must be a number."
                ));
            }
        }

        return ResponseEntity.ok(policies);
    }

    @GetMapping("/system-status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        
        // Get consultant counts
        List<Map<String, Object>> pendingConsultants = consultantService.getPendingConsultants();
        List<Map<String, Object>> approvedConsultants = consultantService.getApprovedConsultants();
        status.put("totalConsultants", pendingConsultants.size() + approvedConsultants.size());
        status.put("approvedConsultants", approvedConsultants.size());
        status.put("pendingConsultants", pendingConsultants.size());
        
        // Get client count
        status.put("totalClients", clientService.getTotalClients());
        
        // Get booking statistics
        Map<String, Object> bookingStats = BookingController.getBookingStatistics();
        status.put("totalBookings", bookingStats.get("totalBookings"));
        status.put("totalRevenue", bookingStats.get("totalRevenue"));
        status.put("paidBookings", bookingStats.get("paidBookings"));
        status.put("completedBookings", bookingStats.get("completedBookings"));
        status.put("cancelledBookings", bookingStats.get("cancelledBookings"));
        
        return ResponseEntity.ok(status);
    }
}
