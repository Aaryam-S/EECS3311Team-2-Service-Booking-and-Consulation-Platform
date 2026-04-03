package controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import service.ClientService;
import service.ConsultantService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final ClientService clientService = ClientService.getInstance();
    private final ConsultantService consultantService = ConsultantService.getInstance();

    // Client Registration
    @PostMapping("/client/register")
    public ResponseEntity<Map<String, Object>> registerClient(
            @RequestBody Map<String, Object> body
    ) {
        Object usernameObj = body.get("username");
        Object passwordObj = body.get("password");
        Object emailObj = body.get("email");
        Object nameObj = body.get("name");

        if (usernameObj == null || passwordObj == null || emailObj == null || nameObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "username, password, email, and name are required."
            ));
        }

        try {
            int clientId = clientService.registerClient(
                    usernameObj.toString(),
                    passwordObj.toString(),
                    emailObj.toString(),
                    nameObj.toString()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Client registered successfully.",
                    "clientId", clientId,
                    "username", usernameObj.toString()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // Client Login
    @PostMapping("/client/login")
    public ResponseEntity<Map<String, Object>> loginClient(
            @RequestBody Map<String, Object> body
    ) {
        Object usernameObj = body.get("username");
        Object passwordObj = body.get("password");

        if (usernameObj == null || passwordObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "username and password are required."
            ));
        }

        try {
            Map<String, Object> client = clientService.loginClient(
                    usernameObj.toString(),
                    passwordObj.toString()
            );
            
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful.",
                    "clientId", client.get("id"),
                    "name", client.get("name"),
                    "email", client.get("email")
            ));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
            } else {
                return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
            }
        }
    }

    // Consultant Registration
    @PostMapping("/consultant/register")
    public ResponseEntity<Map<String, Object>> registerConsultant(
            @RequestBody Map<String, Object> body
    ) {
        Object usernameObj = body.get("username");
        Object passwordObj = body.get("password");
        Object emailObj = body.get("email");
        Object nameObj = body.get("name");
        Object specialtyObj = body.get("specialty");

        if (usernameObj == null || passwordObj == null || emailObj == null || nameObj == null || specialtyObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "username, password, email, name, and specialty are required."
            ));
        }

        try {
            int consultantId = consultantService.registerConsultant(
                    usernameObj.toString(),
                    passwordObj.toString(),
                    emailObj.toString(),
                    nameObj.toString(),
                    specialtyObj.toString()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Consultant registered successfully. Awaiting admin approval.",
                    "consultantId", consultantId,
                    "username", usernameObj.toString(),
                    "status", "Pending"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // Consultant Login
    @PostMapping("/consultant/login")
    public ResponseEntity<Map<String, Object>> loginConsultant(
            @RequestBody Map<String, Object> body
    ) {
        Object usernameObj = body.get("username");
        Object passwordObj = body.get("password");

        if (usernameObj == null || passwordObj == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "username and password are required."
            ));
        }

        try {
            Map<String, Object> consultant = consultantService.loginConsultant(
                    usernameObj.toString(),
                    passwordObj.toString()
            );
            
            return ResponseEntity.ok(Map.of(
                    "message", "Login successful.",
                    "consultantId", consultant.get("id"),
                    "name", consultant.get("name"),
                    "email", consultant.get("email"),
                    "specialty", consultant.get("specialty")
            ));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("not yet approved")) {
                return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
            } else {
                return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
            }
        }
    }
}
