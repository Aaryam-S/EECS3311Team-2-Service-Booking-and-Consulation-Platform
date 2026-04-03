package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import service.ChatbotService;

import java.util.Map;

/**
 * ChatController
 *
 * Exposes the POST /api/chat endpoint for the client-facing AI assistant.
 *
 * Request body  (JSON): { "message": "How do I book a consultant?" }
 * Response body (JSON): { "reply":   "Here is how you can book a consultant..." }
 *
 * Design notes:
 *  - The controller accepts only a plain text question; no user identifiers or
 *    session tokens are forwarded to the LLM, ensuring privacy by design.
 *  - All prompt construction and LLM communication is delegated to ChatbotService.
 *  - CORS is open for development; restrict to the real frontend origin in production.
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")   // Adjust to frontend origin before production deploy
public class ChatController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * Handles a client question and proxies it (with safe platform context)
     * to the configured LLM, returning an AI-generated answer.
     *
     * @param requestBody Map containing the key "message" with the user's question.
     * @return 200 OK with JSON body { "reply": "..." }
     *         400 Bad Request if the message field is missing or empty.
     *         500 Internal Server Error if the LLM call fails unexpectedly.
     */
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> handleChat(
            @RequestBody Map<String, String> requestBody) {

        String message = requestBody.get("message");

        // Validate that a non-empty message was provided
        if (message == null || message.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Message must not be empty."));
        }

        // Truncate extremely long inputs to prevent prompt injection / abuse
        if (message.length() > 1000) {
            message = message.substring(0, 1000);
        }

        try {
            String reply = chatbotService.chat(message);
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception e) {
            // Do NOT expose internal error details to the client
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of("error",
                            "The assistant is temporarily unavailable. "
                            + "Please try again later."));
        }
    }
}