package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = {
        "controller",
        "model",
        "notification",
        "policy",
        "service",
        "state",
        "strategy",
})
public class BackendApplication {
    public static void main(String[] args) { // --- Manual .env loader so I can actually see if it works...
        java.util.Properties envProps = new java.util.Properties();
        try {
            Path envPath = Paths.get(".env");
            if (!Files.exists(envPath)) {
                envPath = Paths.get("../.env");
            }

            if (Files.exists(envPath)) {
                System.out.println("[ENV] Loading from: " + envPath.toAbsolutePath());
                Files.lines(envPath).forEach(line -> {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        String[] parts = trimmed.split("=", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim().replaceAll("^\"|\"$", "").trim();
                            envProps.setProperty(key, value);
                            System.setProperty(key, value);
                            if (key.equals("LLM_API_KEY")) {
                                System.out.println("[ENV] Found LLM_API_KEY, length=" + value.length()); // Explicitly map to the Spring property name used by @Value
                                envProps.setProperty("llm.api.key", value);
                                System.setProperty("llm.api.key", value);
                            }
                        }
                    }
                });
            } else {
                System.out.println("[ENV] WARNING: No .env file found at .env or ../.env");
            }
        } catch (Exception e) {
            System.err.println("[ENV] Could not load .env file: " + e.getMessage());
        }
        // Pass env vars directly into Spring
        SpringApplication app = new SpringApplication(BackendApplication.class);
        java.util.Map<String, Object> defaults = new java.util.HashMap<>();
        for (String name : envProps.stringPropertyNames()) {
            defaults.put(name, envProps.getProperty(name));
        }
        app.setDefaultProperties(defaults);
        app.run(args);
    }
}