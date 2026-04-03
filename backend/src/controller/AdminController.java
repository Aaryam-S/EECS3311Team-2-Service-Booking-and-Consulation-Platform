package controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin")
public class AdminController {

    private final List<Map<String, Object>> pendingConsultants = new ArrayList<>();
    private final Map<String, Object> policies = new LinkedHashMap<>();

    public AdminController() {
        seedData();
    }

    private void seedData() {
        if (pendingConsultants.isEmpty()) {
            pendingConsultants.add(new LinkedHashMap<>(Map.of(
                    "id", 5,
                    "name", "Alice Johnson",
                    "email", "alice@example.com",
                    "specialty", "Legal"
            )));

            pendingConsultants.add(new LinkedHashMap<>(Map.of(
                    "id", 6,
                    "name", "Mark Chen",
                    "email", "mark@example.com",
                    "specialty", "Finance"
            )));
        }

        if (policies.isEmpty()) {
            policies.put("pricingStrategy", "BasePrice");
            policies.put("cancellationPolicy", "Flexible");
        }
    }

    @GetMapping("/consultants/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingConsultants() {
        return ResponseEntity.ok(pendingConsultants);
    }

    @PostMapping("/consultants/{consultantId}/{action}")
    public ResponseEntity<Map<String, Object>> updateConsultantStatus(
            @PathVariable int consultantId,
            @PathVariable String action
    ) {
        Map<String, Object> consultant = null;
        for (Map<String, Object> c : pendingConsultants) {
            if (((Integer) c.get("id")) == consultantId) {
                consultant = c;
                break;
            }
        }

        if (consultant == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Consultant not found."));
        }

        if (!action.equalsIgnoreCase("approve") && !action.equalsIgnoreCase("reject")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Action must be approve or reject."
            ));
        }

        pendingConsultants.remove(consultant);

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

        if (pricingStrategy == null || cancellationPolicy == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "pricingStrategy and cancellationPolicy are required."
            ));
        }

        policies.put("pricingStrategy", pricingStrategy.toString());
        policies.put("cancellationPolicy", cancellationPolicy.toString());

        return ResponseEntity.ok(policies);
    }
}