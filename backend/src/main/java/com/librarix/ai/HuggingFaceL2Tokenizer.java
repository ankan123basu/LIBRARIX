package com.librarix.ai;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * HuggingFaceL2Tokenizer
 *
 * Implements tokenization and L2 (Euclidean) Vector Distance metric:
 * D_L2(U, V) = sqrt( sum( (U_i - V_i)^2 ) )
 *
 * Vectors are L2-normalized to unit magnitude (||V||_2 = 1.0)
 * producing similarity score S_L2 = 1.0 / (1.0 + D_L2).
 */
@Component
public class HuggingFaceL2Tokenizer {

    private static final Pattern WORD_PATTERN = Pattern.compile("[^a-zA-Z0-9]+");

    public Map<String, Double> createL2NormalizedVector(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }

        String[] rawTokens = WORD_PATTERN.split(text.toLowerCase());
        Map<String, Double> rawFreq = new HashMap<>();

        for (String token : rawTokens) {
            if (token.length() >= 2) {
                rawFreq.put(token, rawFreq.getOrDefault(token, 0.0) + 1.0);
            }
        }

        // Calculate L2 norm: ||V||_2 = sqrt(sum(v_i^2))
        double sumSquares = 0.0;
        for (double val : rawFreq.values()) {
            sumSquares += val * val;
        }

        double l2Norm = Math.sqrt(sumSquares);
        if (l2Norm == 0.0) {
            return Map.of();
        }

        // Normalize to unit vector
        Map<String, Double> l2Vector = new HashMap<>();
        for (Map.Entry<String, Double> entry : rawFreq.entrySet()) {
            l2Vector.put(entry.getKey(), entry.getValue() / l2Norm);
        }

        return l2Vector;
    }

    public double calculateL2Distance(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> allKeys = new HashSet<>(v1.keySet());
        allKeys.addAll(v2.keySet());

        double sumSquaredDiffs = 0.0;
        for (String key : allKeys) {
            double val1 = v1.getOrDefault(key, 0.0);
            double val2 = v2.getOrDefault(key, 0.0);
            double diff = val1 - val2;
            sumSquaredDiffs += diff * diff;
        }

        return Math.sqrt(sumSquaredDiffs);
    }

    public double calculateL2SimilarityScore(Map<String, Double> v1, Map<String, Double> v2) {
        double distance = calculateL2Distance(v1, v2);
        return 1.0 / (1.0 + distance);
    }
}
