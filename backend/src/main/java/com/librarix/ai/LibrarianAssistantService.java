package com.librarix.ai;

import com.librarix.dto.ResourceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Addition 3 — AI Feature 2: "Ask the Librarian" Assistant Service
 *
 * Retrieves top-k catalog entries via SemanticSearchService and passes them
 * along with campus circulation policy grounding context to an LLM provider.
 */
@Service
@RequiredArgsConstructor
public class LibrarianAssistantService {

    private final SemanticSearchService semanticSearchService;
    private final LlmProvider llmProvider;

    public String askLibrarian(String question) {
        List<ResourceDTO> relevantResources = semanticSearchService.searchSemantic(question, 5);

        String contextText = relevantResources.stream()
                .map(r -> String.format("- Asset '%s' (%s): Type=%s, Location=%s, Available=%d/%d, Waitlist=%d",
                        r.getTitle(), r.getBarcode(), r.getType(), r.getLocation(),
                        r.getAvailableQuantity(), r.getTotalQuantity(), r.getActiveQueueCount()))
                .collect(Collectors.joining("\n"));

        String policyText = """
                Campus Policy Summary:
                - Standard loan period is 7 days.
                - Grace period is 24 hours post due date.
                - Academic book borrowing incurs $2.00/day overdue fines (capped at $50.00 max).
                - Unavailable books trigger a Priority Waitlist Queue using dynamic urgency scores.
                """;

        String systemPrompt = String.format("RELEVANT CATALOG ASSETS:\n%s\n\nCIRCULATION POLICIES:\n%s",
                contextText.isEmpty() ? "No direct catalog match found." : contextText, policyText);

        return llmProvider.generateResponse(systemPrompt, question);
    }
}
