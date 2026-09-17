package com.librarix.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * GroqLlmProvider — Live Groq LLaMA-3 REST API Implementation
 *
 * Activated automatically when `librarix.ai.groq-api-key` is configured in application.yml.
 * Endpoint: https://api.groq.com/openai/v1/chat/completions
 * Model: llama-3.3-70b-versatile (or configured model)
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "librarix.ai.groq-api-key")
public class GroqLlmProvider implements LlmProvider {

    @Value("${librarix.ai.groq-api-key}")
    private String apiKey;

    @Value("${librarix.ai.groq-model:llama-3.3-70b-versatile}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String generateResponse(String systemPrompt, String userQuery) {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("YOUR_GROQ_API_KEY")) {
            log.warn("Groq API key not configured. Falling back to grounded RAG context output.");
            return fallbackResponse(systemPrompt, userQuery);
        }

        try {
            String url = "https://api.groq.com/openai/v1/chat/completions";

            Map<String, Object> requestBody = Map.of(
                    "model", modelName,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userQuery)
                    ),
                    "temperature", 0.3,
                    "max_tokens", 800
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey.trim());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List choices = (List) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map firstChoice = (Map) choices.get(0);
                    Map message = (Map) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            log.error("Error invoking Groq API (model: {}): {}", modelName, e.getMessage());
        }

        return fallbackResponse(systemPrompt, userQuery);
    }

    private String fallbackResponse(String systemPrompt, String userQuery) {
        return "🤖 LIBRARIX Grounded Assistant:\n\n" + systemPrompt + "\n\nAnswer for: \"" + userQuery + "\"";
    }
}
