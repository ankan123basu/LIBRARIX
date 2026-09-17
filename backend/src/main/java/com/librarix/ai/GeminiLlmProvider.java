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
 * GeminiLlmProvider — Live Google Gemini API Implementation
 *
 * Activated automatically when `librarix.ai.gemini-api-key` is configured in application.yml.
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(name = "librarix.ai.gemini-api-key")
public class GeminiLlmProvider implements LlmProvider {

    @Value("${librarix.ai.gemini-api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String generateResponse(String systemPrompt, String userQuery) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
            log.warn("Gemini API key not configured. Using grounded prompt context response.");
            return fallbackResponse(systemPrompt, userQuery);
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

            String combinedPrompt = String.format("%s\n\nUser Query: %s\n\nPlease provide a helpful, concise answer based ONLY on the catalog and policy context above.",
                    systemPrompt, userQuery);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", combinedPrompt)
                            ))
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List candidates = (List) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map firstCandidate = (Map) candidates.get(0);
                    Map content = (Map) firstCandidate.get("content");
                    List parts = (List) content.get("parts");
                    Map firstPart = (Map) parts.get(0);
                    return (String) firstPart.get("text");
                }
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
        }

        return fallbackResponse(systemPrompt, userQuery);
    }

    private String fallbackResponse(String systemPrompt, String userQuery) {
        return "🤖 LIBRARIX Grounded Assistant:\n\n" + systemPrompt + "\n\nQuery Answer for: \"" + userQuery + "\"";
    }
}
