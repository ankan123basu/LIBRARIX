package com.librarix.ai;

import org.springframework.stereotype.Component;

@Component
public class DefaultLlmProvider implements LlmProvider {

    @Override
    public String generateResponse(String systemPrompt, String userQuery) {
        return "🤖 LIBRARIX Intelligent AI Assistant:\n\n" +
               "Based on our active catalog and circulation policy grounding context:\n" +
               systemPrompt + "\n\n" +
               "Answer for user request: \"" + userQuery + "\"";
    }
}
