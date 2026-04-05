package service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@org.springframework.stereotype.Service
public class ChatbotService {

    @Value("${llm.api.key:}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private static final int MAX_TOKENS = 4096;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CatalogService catalogService;

    public ChatbotService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public String chat(String userMessage) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            return "The AI assistant is currently unavailable. Please contact support or try again later.";
        }

        String systemPrompt = buildSystemPrompt();
        String requestBody = buildRequestBody(systemPrompt, userMessage);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return parseResponse(response.body());
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();

        sb.append("You are a helpful customer support agent for the Service Booking and Consultation Platform. ")
          .append("Answer questions about the platform, services, booking process, payment methods, and policies. ")
          .append("Never ask for or reveal personal user information, payment details, or private booking data. ")
          .append("Keep answers concise, friendly, and professional.\n\n");

        sb.append("=== BOOKING PROCESS ===\n")
          .append("1. Browse available services.\n")
          .append("2. Select a consultant and a suitable time slot.\n")
          .append("3. Confirm the booking and proceed to payment.\n")
          .append("4. A confirmation notification is sent after successful payment.\n\n");

        sb.append("=== AVAILABLE SERVICES ===\n");
        appendServiceCatalogue(sb);
        sb.append("\n");

        sb.append("=== PAYMENT METHODS ===\n")
          .append("Accepted payment methods: Credit Card, Debit Card, PayPal, Bank Transfer.\n\n");

        sb.append("=== GENERAL GUIDELINES ===\n")
          .append("- Consultants set their own availability via time slots.\n")
          .append("- Clients can manage saved payment methods.\n")
          .append("- Admin can update pricing and cancellation policies.\n");

        return sb.toString();
    }

    private void appendServiceCatalogue(StringBuilder sb) {
        List<Map<String, Object>> services = catalogService.getAllServices();

        if (services == null || services.isEmpty()) {
            sb.append("No services are currently listed.\n");
            return;
        }

        for (Map<String, Object> service : services) {
            String name = service.get("name") != null ? service.get("name").toString() : "Unknown Service";
            String consultantName = service.get("consultantName") != null
                    ? service.get("consultantName").toString()
                    : "Unknown Consultant";

            double price = 0.0;
            if (service.get("price") != null) {
                price = Double.parseDouble(service.get("price").toString());
            }

            Object durationObj = service.get("duration");
            String duration = durationObj != null ? durationObj.toString() : "60";

            sb.append("- ")
              .append(name)
              .append(": $")
              .append(String.format("%.2f", price))
              .append(", consultant ")
              .append(consultantName)
              .append(", ")
              .append(duration)
              .append(" minutes.\n");
        }
    }

    String buildRequestBody(String systemPrompt, String userMessage) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode systemInstruction = root.putObject("systemInstruction");
        ArrayNode systemParts = systemInstruction.putArray("parts");
        systemParts.addObject().put("text", systemPrompt);

        ArrayNode contents = root.putArray("contents");
        ObjectNode userContent = contents.addObject();
        userContent.put("role", "user");
        ArrayNode userParts = userContent.putArray("parts");
        userParts.addObject().put("text", userMessage);

        ObjectNode config = root.putObject("generationConfig");
        config.put("maxOutputTokens", MAX_TOKENS);
        config.put("temperature", 0.7);

        return objectMapper.writeValueAsString(root);
    }

    String parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        if (root.has("error")) {
            String errorMsg = root.path("error").path("message").asText("Unknown error");
            return "Sorry, I encountered an issue: " + errorMsg;
        }

        JsonNode firstCandidate = root.path("candidates").get(0);
        if (firstCandidate == null) {
            return "No response generated by the AI.";
        }

        JsonNode parts = firstCandidate.path("content").path("parts");
        StringBuilder reply = new StringBuilder();

        for (JsonNode part : parts) {
            if (part.has("thought") && part.path("thought").asBoolean()) {
                continue;
            }

            String text = part.path("text").asText("");
            if (!text.isBlank()) {
                reply.append(text);
            }
        }

        if (reply.length() == 0) {
            return "I'm sorry, I couldn't generate a response. Please try again.";
        }

        return reply.toString().trim();
    }
}