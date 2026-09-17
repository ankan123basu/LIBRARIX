package com.librarix.ai;

public interface LlmProvider {
    String generateResponse(String systemPrompt, String userQuery);
}
