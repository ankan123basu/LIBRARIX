package com.librarix.ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HuggingFaceL2TokenizerTest {

    private HuggingFaceL2Tokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new HuggingFaceL2Tokenizer();
    }

    @Test
    void testL2NormalizedVectorMagnitude() {
        Map<String, Double> vec = tokenizer.createL2NormalizedVector("NVIDIA Jetson Orin Nano Developer Kit CUDA");
        assertFalse(vec.isEmpty());

        // Sum of squares of unit vector must equal 1.0
        double sumSq = vec.values().stream().mapToDouble(v -> v * v).sum();
        assertEquals(1.0, sumSq, 0.0001);
    }

    @Test
    void testL2DistanceAndSimilarity() {
        Map<String, Double> v1 = tokenizer.createL2NormalizedVector("NVIDIA Jetson Edge AI");
        Map<String, Double> v2 = tokenizer.createL2NormalizedVector("NVIDIA Jetson Robotics");
        Map<String, Double> v3 = tokenizer.createL2NormalizedVector("Martin Kleppmann Architecture Book");

        double sim12 = tokenizer.calculateL2SimilarityScore(v1, v2);
        double sim13 = tokenizer.calculateL2SimilarityScore(v1, v3);

        assertTrue(sim12 > sim13, "NVIDIA Jetson items should have higher L2 similarity than an unrelated book.");
    }
}
