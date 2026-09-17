package com.librarix.ai;

import com.librarix.dto.ResourceDTO;
import com.librarix.graph.CoBorrowGraphService;
import com.librarix.model.Loan;
import com.librarix.model.Resource;
import com.librarix.repository.LoanRepository;
import com.librarix.repository.ResourceRepository;
import com.librarix.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Addition 3 — AI Feature 3: Personalized Recommendations
 *
 * Uses co-borrow graph cluster affinity from historical user loans.
 * If user is new (zero loans), falls back to overall top borrowed assets.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final LoanRepository loanRepository;
    private final ResourceRepository resourceRepository;
    private final CoBorrowGraphService coBorrowGraphService;
    private final ResourceService resourceService;

    public List<ResourceDTO> getPersonalizedRecommendations(String userId, int limit) {
        List<Loan> userLoans = loanRepository.findByUserId(userId);
        List<Resource> allResources = resourceRepository.findAll();

        if (allResources.isEmpty()) {
            return List.of();
        }

        List<String> allIds = allResources.stream().map(Resource::getId).toList();
        Map<String, Integer> clusterMap = coBorrowGraphService.computeCoBorrowClusters(allIds);

        Set<String> borrowedResourceIds = userLoans.stream()
                .map(Loan::getResourceId)
                .collect(Collectors.toSet());

        // Count user loans per cluster
        Map<Integer, Long> clusterFrequency = userLoans.stream()
                .map(loan -> clusterMap.getOrDefault(loan.getResourceId(), 1))
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        List<Resource> recommended = new ArrayList<>();

        if (!clusterFrequency.isEmpty()) {
            // Pick top cluster
            int favoriteCluster = Collections.max(clusterFrequency.entrySet(), Map.Entry.comparingByValue()).getKey();

            List<Resource> clusterMatches = allResources.stream()
                    .filter(res -> !borrowedResourceIds.contains(res.getId())) // Don't recommend already borrowed
                    .filter(res -> clusterMap.getOrDefault(res.getId(), 0) == favoriteCluster)
                    .toList();
            recommended.addAll(clusterMatches);
        }

        // Fallback if not enough cluster recommendations
        if (recommended.size() < limit) {
            Set<String> existingIds = recommended.stream().map(Resource::getId).collect(Collectors.toSet());
            List<Resource> fallback = allResources.stream()
                    .filter(res -> !borrowedResourceIds.contains(res.getId()))
                    .filter(res -> !existingIds.contains(res.getId()))
                    .limit(limit - recommended.size())
                    .toList();
            recommended.addAll(fallback);
        }

        return recommended.stream()
                .limit(limit > 0 ? limit : 6)
                .map(resourceService::mapToDTO)
                .toList();
    }
}
