package controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import service.ClientService;

@RestController
@RequestMapping("/clients")
@CrossOrigin(origins = "*")
public class ClientController {

    private final ClientService clientService;
    private final AtomicInteger paymentMethodIdCounter = new AtomicInteger(1000);

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/{clientId}/payment-methods")
    public ResponseEntity<List<Map<String, Object>>> getPaymentMethods(@PathVariable int clientId) {
        return ResponseEntity.ok(clientService.getPaymentMethods(clientId));
    }

    @PostMapping("/{clientId}/payment-methods")
    public ResponseEntity<Map<String, Object>> addPaymentMethod(
            @PathVariable int clientId,
            @RequestBody Map<String, Object> body) {

        Object typeObj = body.get("type");

        if (typeObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "type is required (credit-card, debit-card, paypal, bank-transfer)."
            ));
        }

        String type = typeObj.toString().toLowerCase();
        String methodId = "pm_" + paymentMethodIdCounter.incrementAndGet();

        Map<String, Object> paymentMethod = new LinkedHashMap<>();
        paymentMethod.put("id", methodId);
        paymentMethod.put("type", type);

        if (type.equals("credit-card") || type.equals("debit-card")) {
            Object cardNumberObj = body.get("cardNumber");
            Object cvvObj = body.get("cvv");
            Object expiryDateObj = body.get("expiryDate");

            if (cardNumberObj == null || cvvObj == null || expiryDateObj == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "cardNumber, cvv, and expiryDate are required for card payments."
                ));
            }

            String cardNumber = cardNumberObj.toString();
            if (cardNumber.length() != 16) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Card number must be 16 digits."
                ));
            }

            paymentMethod.put("cardNumber", cardNumber);
            paymentMethod.put("cvv", cvvObj.toString());
            paymentMethod.put("expiryDate", expiryDateObj.toString());
            paymentMethod.put("lastFour", cardNumber.substring(12));

        } else if (type.equals("paypal")) {
            Object emailObj = body.get("email");
            if (emailObj == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "email is required for PayPal."
                ));
            }

            paymentMethod.put("email", emailObj.toString());
            paymentMethod.put("lastFour", emailObj.toString());

        } else if (type.equals("bank-transfer")) {
            Object accountNumberObj = body.get("accountNumber");
            Object routingNumberObj = body.get("routingNumber");

            if (accountNumberObj == null || routingNumberObj == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "accountNumber and routingNumber are required for bank transfers."
                ));
            }

            String accountNumber = accountNumberObj.toString();
            paymentMethod.put("accountNumber", accountNumber);
            paymentMethod.put("routingNumber", routingNumberObj.toString());
            paymentMethod.put("lastFour", accountNumber.length() >= 4
                    ? accountNumber.substring(accountNumber.length() - 4)
                    : accountNumber);

        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid payment type. Must be credit-card, debit-card, paypal, or bank-transfer."
            ));
        }

        try {
            clientService.addPaymentMethod(clientId, paymentMethod);
            return ResponseEntity.ok(Map.of(
                "message", "Payment method added successfully.",
                "methodId", methodId,
                "type", type
            ));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{clientId}/payment-methods/{methodId}")
    public ResponseEntity<Map<String, Object>> deletePaymentMethod(
            @PathVariable int clientId,
            @PathVariable String methodId) {
        try {
            clientService.removePaymentMethod(clientId, methodId);
            return ResponseEntity.ok(Map.of(
                "message", "Payment method deleted successfully.",
                "methodId", methodId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}