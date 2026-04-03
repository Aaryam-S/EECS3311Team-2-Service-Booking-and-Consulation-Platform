package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientService {
    private static ClientService instance;
    private final List<Map<String, Object>> clients = new ArrayList<>();
    private final AtomicInteger clientIdCounter = new AtomicInteger(1);

    private ClientService() {
    }

    public static synchronized ClientService getInstance() {
        if (instance == null) {
            instance = new ClientService();
        }
        return instance;
    }

    public int registerClient(String username, String password, String email, String name) {
        // Check if username already exists
        for (Map<String, Object> client : clients) {
            if (username.equals(client.get("username"))) {
                throw new IllegalArgumentException("Username already exists.");
            }
        }

        int clientId = clientIdCounter.getAndIncrement();

        Map<String, Object> client = new LinkedHashMap<>();
        client.put("id", clientId);
        client.put("username", username);
        client.put("password", password); // Not hashed for demo
        client.put("email", email);
        client.put("name", name);
        client.put("paymentMethods", new ArrayList<>());

        clients.add(client);
        return clientId;
    }

    public Map<String, Object> loginClient(String username, String password) {
        for (Map<String, Object> client : clients) {
            if (username.equals(client.get("username"))) {
                if (password.equals(client.get("password"))) {
                    return new HashMap<>(client);
                } else {
                    throw new IllegalArgumentException("Invalid password.");
                }
            }
        }
        throw new IllegalArgumentException("Client not found.");
    }

    public Map<String, Object> getClientById(int clientId) {
        for (Map<String, Object> client : clients) {
            if ((Integer) client.get("id") == clientId) {
                return new HashMap<>(client);
            }
        }
        return null;
    }

    public List<Map<String, Object>> getPaymentMethods(int clientId) {
        Map<String, Object> client = getClientById(clientId);
        if (client != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> methods = (List<Map<String, Object>>) client.get("paymentMethods");
            return methods != null ? methods : new ArrayList<>();
        }
        return new ArrayList<>();
    }

    public void addPaymentMethod(int clientId, Map<String, Object> paymentMethod) {
        for (Map<String, Object> client : clients) {
            if ((Integer) client.get("id") == clientId) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> methods = (List<Map<String, Object>>) client.get("paymentMethods");
                if (methods == null) {
                    methods = new ArrayList<>();
                    client.put("paymentMethods", methods);
                }
                methods.add(new LinkedHashMap<>(paymentMethod));
                return;
            }
        }
    }

    public void removePaymentMethod(int clientId, String methodId) {
        for (Map<String, Object> client : clients) {
            if ((Integer) client.get("id") == clientId) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> methods = (List<Map<String, Object>>) client.get("paymentMethods");
                if (methods != null) {
                    methods.removeIf(m -> methodId.equals(m.get("id")));
                }
                return;
            }
        }
    }

    public int getTotalClients() {
        return clients.size();
    }
}
