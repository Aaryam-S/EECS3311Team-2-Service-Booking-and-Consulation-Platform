package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsultantService {
    private static ConsultantService instance;
    private final List<Map<String, Object>> consultants = new ArrayList<>();
    private final AtomicInteger consultantIdCounter = new AtomicInteger(1);

    private ConsultantService() {
    }

    public static synchronized ConsultantService getInstance() {
        if (instance == null) {
            instance = new ConsultantService();
        }
        return instance;
    }

    public int registerConsultant(String username, String password, String email, String name, String specialty) {
        // Check if username already exists
        for (Map<String, Object> consultant : consultants) {
            if (username.equals(consultant.get("username"))) {
                throw new IllegalArgumentException("Username already exists.");
            }
        }

        int consultantId = consultantIdCounter.getAndIncrement();

        Map<String, Object> consultant = new LinkedHashMap<>();
        consultant.put("id", consultantId);
        consultant.put("username", username);
        consultant.put("password", password); // Not hashed for demo
        consultant.put("email", email);
        consultant.put("name", name);
        consultant.put("specialty", specialty);
        consultant.put("approved", false);
        consultant.put("status", "Pending");

        consultants.add(consultant);
        return consultantId;
    }

    public Map<String, Object> loginConsultant(String username, String password) {
        for (Map<String, Object> consultant : consultants) {
            if (username.equals(consultant.get("username"))) {
                if (password.equals(consultant.get("password"))) {
                    Boolean approved = (Boolean) consultant.get("approved");
                    if (approved == null || !approved) {
                        throw new IllegalArgumentException("Consultant account not yet approved by admin.");
                    }
                    return new HashMap<>(consultant);
                } else {
                    throw new IllegalArgumentException("Invalid password.");
                }
            }
        }
        throw new IllegalArgumentException("Consultant not found.");
    }

    public Map<String, Object> getConsultantById(int consultantId) {
        for (Map<String, Object> consultant : consultants) {
            if ((Integer) consultant.get("id") == consultantId) {
                return new HashMap<>(consultant);
            }
        }
        return null;
    }

    public List<Map<String, Object>> getPendingConsultants() {
        List<Map<String, Object>> pending = new ArrayList<>();
        for (Map<String, Object> consultant : consultants) {
            Boolean approved = (Boolean) consultant.get("approved");
            if (approved == null || !approved) {
                pending.add(new HashMap<>(consultant));
            }
        }
        return pending;
    }

    public List<Map<String, Object>> getApprovedConsultants() {
        List<Map<String, Object>> approved = new ArrayList<>();
        for (Map<String, Object> consultant : consultants) {
            Boolean isApproved = (Boolean) consultant.get("approved");
            if (isApproved != null && isApproved) {
                approved.add(new HashMap<>(consultant));
            }
        }
        return approved;
    }

    public void approveConsultant(int consultantId) {
        for (Map<String, Object> consultant : consultants) {
            if ((Integer) consultant.get("id") == consultantId) {
                consultant.put("approved", true);
                consultant.put("status", "Approved");
                return;
            }
        }
    }

    public void rejectConsultant(int consultantId) {
        consultants.removeIf(c -> (Integer) c.get("id") == consultantId);
    }
}
