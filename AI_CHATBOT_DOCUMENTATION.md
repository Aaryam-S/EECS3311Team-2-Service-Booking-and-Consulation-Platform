# AI Customer Assistant — Documentation

## Overview

The AI Customer Assistant is a chatbot integrated within the client interface of the Service Booking and Consultation Platform. It responds to queries related to the platform, its offerings, the booking procedure, payment options, and cancellation rules.

**Privacy Assurance**: The AI assistant does not have any knowledge of the personal data of the user, confidential booking details, or payment information. It can only access publicly available information on the platform.

## Architecture

```
Client Browser
     │ POST /api/chat  { "message": "..." }
     ▼
ChatController  (controller/ChatController.java)
     │ delegates to
     ▼
ChatbotService  (service/ChatbotService.java)
     │ builds safe system prompt  ──▶  Public service catalogue
     │                            ──▶  Hard-coded platform policies
     │ POST https://generativelanguage.googleapis.com/v1beta/models/...
     ▼
Google Gemini 2.5 Flash  (external LLM API)
     │ JSON response
     ▼
ChatbotService  (parses reply)
     │
     ▼
ChatController  (returns { "reply": "..." })
     │
     ▼
Client Browser
```

No database queries are made on behalf of the AI. The LLM never sees any row from any database table.

---

## Components

### `service/ChatbotService.java`

| Responsibility | Detail |
|---|---|
| **Prompt construction** | Builds a system prompt that only includes information from the public platform |
| **LLM interaction** | Accesses Google Gemini API through `java.net.http.HttpClient` |
| **Response processing** | Retrieves assistant output by using Jackson `ObjectMapper` |
| **Exception handling** | Provides a fallback response in case the API is down |
| **Secure API key management** | API key passed in via `@Value("${llm.api.key}")` — not hardcoded |

**Sections included in the system prompt:**

1. Role and behavior definition
2. Description of the platform (public)
3. Steps to book an appointment (public)
4. List of offered services: service name, price, duration (using `CatalogService`, public)
5. Payment methods accepted (public)
6. Summary of cancellation and refund policies (public)
7. FAQs

### `controller/ChatController.java`

| Endpoint | `POST /api/chat` |
|---|---|
| **Content type consumed** | `application/json` — `{ "message": "string" }` |
| **Content type produced** | `application/json` — `{ "reply": "string" }` |
| **Input validation** | Rejects empty messages (400); clips messages longer than 1 000 characters |
| **Error surface** | All internal errors result in a general message without a stack trace shown |
| **CORS Configuration** | Development-only configuration of `@CrossOrigin(origins = "*")`; restrict in production |

### `backend/pom.xml`

Added dependency:

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

`spring-boot-starter-web` is already included (embedded Tomcat & Spring MVC).
The `java.net.http.HttpClient` class is built into JDK from Java 11.

### `backend/src/application.properties`

```properties
# API key for LLM service - to be retrieved from environment variable LLM_API_KEY
llm.api.key=${LLM_API_KEY:}
```

---

## AI Features

| Feature                 | Description                                          |
|-------------------------|-----------------------------------------------------|
| **Booking guidance**        | Provides a breakdown of booking procedure           |
| **Service catalogue**       | Shows available services and their prices & duration |
| **Payment information**     | Specifies available payment options                  |
| **Cancellation assistance** | Provides information about cancellations & refunds   |
| **Platform FAQ**            | Answers common questions                             |
| **Graceful fallback**       | Returns informative error when LLM API is offline    |

---
---

## Safety Precautions

### Privacy by Design

| Safeguard         | Implementation                                             |
|-------------------|------------------------------------------------------------|
| **No PII in prompt**  | `ChatController` receives only `message`; no `userId`, etc.  |
| **No DB access for LLM** | `ChatbotService` does not access the DB but uses `CatalogService` |
| **No personal booking information** | Bookings, clients' names and transactions are not in prompts |
| **Input truncation**   | Chat messages longer than 1 000 chars are silently cut off |
| **Error masking**      | Internal server errors show a generic error message        |

### API Key Security

- Gemini's API key is loaded from an **environment variable**: `LLM_API_KEY`.
- The environment variable is set in `ChatbotService` with the help of Spring's `@Value` annotation.
- Gemini's API key is **never** stored in source code.
- `.env.example` contains a placeholder: `LLM_API_KEY=your_api_key_here`.

### Input Validation

- Missing `message` parameter → `400 Bad Request`
- Long chat message (>1 000 chars) → silently cut off
- Any exception while calling the LLM service → `500 Internal Server Error`

### Injection Prevention

- User input is included in the **`user` role** prompt message only.
- The **`system` instruction** prompts are managed exclusively by the server.
- In case of any attempts to override instruction, the Gemini architecture makes sure the injection effects are minimalized.

---

## System Context (System Prompt)

The following is the format of the system prompt included with every query:

```
You are a helpful customer support agent for the Service Booking and Consultation Platform. Your role is to answer questions about the platform, services provided, the booking process, methods of payment, and general policies. 
You must NEVER ask for or reveal personal user information, payment details, or private booking data. Keep answers concise, friendly, and professional.

=== PLATFORM OVERVIEW ===
[Public description of the platform]

=== BOOKING PROCESS ===
[Instructions for booking on the platform]

=== AVAILABLE SERVICES ===
[Live data from CatalogService: service name, service rate, duration]

=== PAYMENT METHODS ===
[List of acceptable payment options]

=== CANCELLATION AND REFUNDS ===
[Public policy for cancellation and refunds]

=== GENERAL GUIDELINES ===
[Additional FAQ hints]
```

---

### Example Interactions

| Client Question          | AI Response Scope                  |
|-------------------------|------------------------------------|
| "How do I book a consultant?"   | Explains the 4-step booking process |
| "What payment methods do you accept?"    | Explains accepted payment types: Credit Card, Debit Card, PayPal |
| "Is cancellation possible?"       | Explains the cancellation policy and time period for refunds |
| "What services can I get?"       | Provides service list from the catalogue |
| "How much VAT do you charge?"     | Explains that rates can already include VAT |

---

### Environment Variables Reference

| Configuration property         | Environment variable           | Default value     | Description                     |
|------------------------------|------------------------------|---------------|--------------------------------|
| `server.port`                | `BACKEND_PORT`               | `8080`         | Server port                     |
| `llm.api.key`                | `LLM_API_KEY`                | _blank_        | Google Gemini API key           |

If `llm.api.key` is missing or contains an empty string, `ChatbotService` returns "Assistant not available" message instead of making an API request.

## Notes for Deployment

1. Make sure `@CrossOrigin` in `ChatController` is set for the real frontend
   origin prior to deploying into production.
2. Rate-limit `/api/chat` endpoint on the level of your API gateway to
   protect from potential abuse.
3. Track usage through the **Google AI Studio** dashboard.

---

## Alternative LLM Providers

The default implementation uses the **Google Gemini API** (free plan), but the application architecture is extensible. The following providers require modification of `ChatbotService.java` code only:

| Provider | API Endpoint                    | Example Model Name         |
|---|---|---|
| Google Gemini (default) | `https://generativelanguage.googleapis.com/v1beta/models/...` | `gemini-1.5-flash` |
| OpenAI                     | `https://api.openai.com/v1/chat/completions`         | `gpt-3.5-turbo`          |
| Anthropic Claude           | `https://api.anthropic.com/v1/messages`             | `claude-3-haiku-20240307` |

Notes: We initially started with OpenAI, but switched to Gemini bacause of cost. Within Gemini, we started with 1.5 Flash, but that seemed to be out of use, switched to 2 Flash, but that had 0/0 RPM, settled with 2.5 Flash.