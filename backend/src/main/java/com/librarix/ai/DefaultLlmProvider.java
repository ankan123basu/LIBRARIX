package com.librarix.ai;

import org.springframework.stereotype.Component;

@Component
public class DefaultLlmProvider implements LlmProvider {

    @Override
    public String generateResponse(String systemPrompt, String userQuery) {
        return "🤖 LIBRA-AI (LIBRARIX Intelligent Campus Librarian)\n" +
               "Engineering & Platform Architecture by Ankan Basu\n\n" +
               "Grounding Context & RAG Inventory Snapshot:\n" +
               systemPrompt + "\n\n" +
               "Response for: \"" + userQuery + "\"\n" +
               "Based on our active catalog, I have retrieved the relevant textbook entries above. You can check out available copies directly from the Catalogue or join the Weighted Fair Queue waitlist if checked out!";
    }
}
