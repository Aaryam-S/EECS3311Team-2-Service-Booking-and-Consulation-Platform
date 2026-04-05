package controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import model.SystemPolicy;
import policy.FlexibleCancellation;
import policy.NoRefundCancellation;
import policy.StrictCancellation;
import service.ClientService;
import service.ConsultantService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin")
public class AdminController {

    private final ConsultantService consultantService;
    private final ClientService clientService;
    private final JdbcTemplate jdbcTemplate;

    public AdminController(ConsultantService consultantService,
                           ClientService clientService,
                           JdbcTemplate jdbcTemplate) {
        this.consultantService = consultantService;
        this.clientService = clientService;
        this.jdbcTemplate = jdbcTemplate;
        syncRuntimePolicy();
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
            @PathVariable String action) {

        if (!action.equalsIgnoreCase("approve") && !action.equalsIgnoreCase("reject")) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Action must be approve or reject."
            ));
        }

        if (consultantService.getConsultantById(consultantId) == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Consultant not found."));
        }

        try {
            if (action.equalsIgnoreCase("approve")) {
                consultantService.approveConsultant(consultantId);
            } else {
                consultantService.rejectConsultant(consultantId);
            }

            return ResponseEntity.ok(Map.of(
                "message", "Consultant " + action.toLowerCase() + "d successfully.",
                "consultantId", consultantId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/policies")
    public ResponseEntity<Map<String, Object>> getPolicies() {
        return ResponseEntity.ok(getPolicyRow());
    }

    @PostMapping("/policies")
    public ResponseEntity<Map<String, Object>> updatePolicies(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> current = getPolicyRow();

            String pricingStrategy = body.get("pricingStrategy") != null
                    ? body.get("pricingStrategy").toString()
                    : current.get("pricingStrategy").toString();

            String cancellationPolicy = body.get("cancellationPolicy") != null
                    ? body.get("cancellationPolicy").toString()
                    : current.get("cancellationPolicy").toString();

            double cancellationFee = body.get("cancellationFee") != null
                    ? Double.parseDouble(body.get("cancellationFee").toString())
                    : Double.parseDouble(current.get("cancellationFee").toString());

            boolean notificationsEnabled = body.get("notificationsEnabled") != null
                    ? Boolean.parseBoolean(body.get("notificationsEnabled").toString())
                    : Boolean.parseBoolean(current.get("notificationsEnabled").toString());

            boolean refundsEnabled = body.get("refundsEnabled") != null
                    ? Boolean.parseBoolean(body.get("refundsEnabled").toString())
                    : Boolean.parseBoolean(current.get("refundsEnabled").toString());

            jdbcTemplate.update(
                "UPDATE system_policies " +
                "SET pricing_strategy = ?, cancellation_policy = ?, cancellation_fee = ?, " +
                "notifications_enabled = ?, refunds_enabled = ? WHERE id = 1",
                pricingStrategy, cancellationPolicy, cancellationFee, notificationsEnabled, refundsEnabled
            );

            syncRuntimePolicy();
            return ResponseEntity.ok(getPolicyRow());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/system-status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new LinkedHashMap<>();

        List<Map<String, Object>> pendingConsultants = consultantService.getPendingConsultants();
        List<Map<String, Object>> approvedConsultants = consultantService.getApprovedConsultants();

        status.put("totalConsultants", pendingConsultants.size() + approvedConsultants.size());
        status.put("approvedConsultants", approvedConsultants.size());
        status.put("pendingConsultants", pendingConsultants.size());
        status.put("totalClients", clientService.getTotalClients());

        Map<String, Object> bookingStats = jdbcTemplate.queryForMap(
            "SELECT " +
            "COUNT(*) AS \"totalBookings\", " +
            "COALESCE(SUM(final_price), 0) AS \"totalRevenue\", " +
            "COUNT(*) FILTER (WHERE status = 'Paid') AS \"paidBookings\", " +
            "COUNT(*) FILTER (WHERE status = 'Completed') AS \"completedBookings\", " +
            "COUNT(*) FILTER (WHERE status = 'Cancelled') AS \"cancelledBookings\" " +
            "FROM bookings"
        );

        status.putAll(bookingStats);
        return ResponseEntity.ok(status);
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

    private void syncRuntimePolicy() {
        Map<String, Object> policies = getPolicyRow();

        String cancellationPolicy = policies.get("cancellationPolicy").toString();
        double cancellationFee = Double.parseDouble(policies.get("cancellationFee").toString());

        SystemPolicy.getInstance().setCancellationFee(cancellationFee);

        switch (cancellationPolicy.toLowerCase()) {
            case "flexible":
                SystemPolicy.getInstance().setCancellationPolicy(new FlexibleCancellation());
                break;
            case "strict":
                SystemPolicy.getInstance().setCancellationPolicy(new StrictCancellation());
                break;
            case "norefund":
            case "no refund":
                SystemPolicy.getInstance().setCancellationPolicy(new NoRefundCancellation());
                break;
            default:
                SystemPolicy.getInstance().setCancellationPolicy(new FlexibleCancellation());
        }
    }
}