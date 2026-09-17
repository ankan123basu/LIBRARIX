package com.librarix.ai;

import com.librarix.dto.ResourceDTO;
import com.librarix.model.Resource;
import com.librarix.repository.ResourceRepository;
import com.librarix.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Addition 3 — AI Feature 1: RAG Vector Search with HuggingFace L2 Distance
 *
 * Uses HuggingFaceL2Tokenizer unit vectors to score documents using L2 Euclidean Distance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticSearchService {

    private final ResourceRepository resourceRepository;
    private final ResourceService resourceService;
    private final HuggingFaceL2Tokenizer l2Tokenizer;

    public List<ResourceDTO> searchSemantic(String query, int topK) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        List<Resource> allResources = resourceRepository.findAll();
        Map<String, Double> queryVector = l2Tokenizer.createL2NormalizedVector(query);

        Map<String, Double> scores = new HashMap<>();

        for (Resource resource : allResources) {
            String docText = String.format("%s %s %s %s",
                    resource.getTitle(),
                    resource.getAuthorOrBrand(),
                    resource.getLocation(),
                    String.join(" ", resource.getTags() != null ? resource.getTags() : List.of())
            );

            Map<String, Double> docVector = l2Tokenizer.createL2NormalizedVector(docText);
            double similarity = l2Tokenizer.calculateL2SimilarityScore(queryVector, docVector);

            if (similarity > 0.45) { // L2 similarity threshold
                scores.put(resource.getId(), similarity);
            }
        }

        List<Resource> matched = allResources.stream()
                .filter(res -> scores.containsKey(res.getId()))
                .sorted((r1, r2) -> Double.compare(scores.get(r2.getId()), scores.get(r1.getId())))
                .limit(topK > 0 ? topK : 10)
                .toList();

        return matched.stream().map(resourceService::mapToDTO).toList();
    }
}
