package service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

// ChatbotService
@Service
public class ChatbotService {

    @Value("${llm.api.key:}") // env variable
    private String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"; // endpoint

    private static final String MODEL = "gemini-2.5-flash"; // Model to use

    private static final int MAX_TOKENS = 4096; // Maximum tokens

    private static final Duration TIMEOUT = Duration.ofSeconds(30); // HTTP timeout

    // Shared HTTP and JSON infrastructure
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Public API
    // Processes a client question and returns an AI-generated answer.
    public String chat(String userMessage) throws Exception {

        if (apiKey == null || apiKey.isBlank()) {
            return "The AI assistant is currently unavailable. "
                    + "Please contact support or try again later.";
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

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return parseResponse(response.body());
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();

        sb.append("You are a helpful customer support agent for the ")
                .append("Service Booking and Consultation Platform. ")
                .append("Your role is to answer questions about the platform, ")
                .append("services provided, the booking process, methods of payment, ")
                .append("and general policies. ")
                .append("You must NEVER ask for or reveal personal user information, ")
                .append("payment details, or private booking data. ")
                .append("Keep answers concise, friendly, and professional.\n\n");

        sb.append("=== PLATFORM OVERVIEW ===\n")
                .append("This is an online platform where clients can book consulting ")
                .append("sessions with professional consultants across various domains.\n\n");

        sb.append("=== BOOKING PROCESS ===\n")
                .append("1. Browse available services in the Service Catalogue.\n")
                .append("2. Select a consultant and a suitable time slot.\n")
                .append("3. Confirm the booking and proceed to payment.\n")
                .append("4. A confirmation notification is sent after successful payment.\n\n");

        sb.append("=== AVAILABLE SERVICES ===\n");
        appendServiceCatalogue(sb);
        sb.append("\n");

        sb.append("=== PAYMENT METHODS ===\n")
                .append("Accepted payment methods: Credit Card, Debit Card, PayPal.\n")
                .append("All transactions are secured and encrypted.\n\n");

        sb.append("=== CANCELLATION & REFUND POLICY ===\n")
                .append("Clients may cancel a booking before the session starts. ")
                .append("A cancellation fee may apply depending on how close to the ")
                .append("session the cancellation is made. ")
                .append("Refunds (when applicable) are processed within 5-7 business days. ")
                .append("Please review the platform refund policy for full details.\n\n");

        sb.append("=== GENERAL GUIDELINES ===\n")
                .append("- Consultants set their own availability via time slots.\n")
                .append("- Session duration varies by service type (see service list).\n")
                .append("- Clients receive email notifications for bookings and updates.\n")
                .append("- For account issues, direct users to the support team.\n");

        return sb.toString();
    }

    // Reads ServiceCatalog to help with questions and accuracy
    private void appendServiceCatalogue(StringBuilder sb) {
        try {
            List<model.Service> services = CatalogService.getInstance().getAllServices();
            if (services == null || services.isEmpty()) {
                sb.append("No services are currently listed in the catalogue.\n");
                return;
            }
            for (model.Service s : services) {
                sb.append("- ").append(s.getName())
                        .append(": $").append(String.format("%.2f", s.getRate()))
                        .append(" per session, ")
                        .append(s.getDuration()).append(" minutes.\n");
            }
        } catch (Exception e) {
            sb.append("Service catalogue temporarily unavailable.\n");
        }
    }

    // JSON format: https://ai.google.dev/gemini-api/docs/request-reference
    String buildRequestBody(String systemPrompt,
            String userMessage) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();

        ObjectNode systemInstruction = root.putObject("systemInstruction"); // System Instruction
        ArrayNode systemParts = systemInstruction.putArray("parts");
        systemParts.addObject().put("text", systemPrompt);

        ArrayNode contents = root.putArray("contents"); // User Content
        ObjectNode userContent = contents.addObject();
        userContent.put("role", "user");
        ArrayNode userParts = userContent.putArray("parts");
        userParts.addObject().put("text", userMessage);

        ObjectNode config = root.putObject("generationConfig"); // Generation Config
        config.put("maxOutputTokens", MAX_TOKENS);
        config.put("temperature", 0.7);

        return objectMapper.writeValueAsString(root);
    }

    // Chatbot's responses are noted here
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
            if (part.has("thought") && part.path("thought").asBoolean()) { // skip internal thought parts, we are using
                                                                           // free...
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