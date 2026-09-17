package com.librarix.graph;

import com.librarix.model.Loan;
import com.librarix.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Addition 4 — Algorithm 3: Co-Borrow Graph Clustering (Label Propagation)
 *
 * Constructs a co-borrow graph from loan history (nodes = resources, edges = co-borrow frequency by same user).
 * Runs Label Propagation Algorithm (LPA) to partition resources into "Reads Well Together" affinity clusters.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoBorrowGraphService {

    private final LoanRepository loanRepository;

    public Map<String, Integer> computeCoBorrowClusters(List<String> allResourceIds) {
        if (allResourceIds == null || allResourceIds.isEmpty()) {
            return Map.of();
        }

        // 1. Build Adjacency Matrix & Edge Weights
        Map<String, Map<String, Double>> graph = buildCoBorrowGraph();

        // Ensure all resource IDs are present in node list
        for (String id : allResourceIds) {
            graph.putIfAbsent(id, new HashMap<>());
        }

        // 2. Initialize Unique Community Labels
        Map<String, Integer> labels = new HashMap<>();
        int labelCounter = 1;
        for (String node : graph.keySet()) {
            labels.put(node, labelCounter++);
        }

        // 3. Label Propagation Iterations
        int maxIterations = 20;
        List<String> nodes = new ArrayList<>(graph.keySet());

        for (int iter = 0; iter < maxIterations; iter++) {
            boolean changed = false;
            Collections.shuffle(nodes); // Randomize node update sequence

            for (String node : nodes) {
                Map<String, Double> neighbors = graph.get(node);
                if (neighbors.isEmpty()) {
                    continue; // Isolated node keeps its label
                }

                // Sum edge weights per label
                Map<Integer, Double> labelWeights = new HashMap<>();
                for (Map.Entry<String, Double> nbrEntry : neighbors.entrySet()) {
                    String nbr = nbrEntry.getKey();
                    double weight = nbrEntry.getValue();
                    int nbrLabel = labels.get(nbr);
                    labelWeights.put(nbrLabel, labelWeights.getOrDefault(nbrLabel, 0.0) + weight);
                }

                // Find majority label
                int bestLabel = Collections.max(labelWeights.entrySet(), Map.Entry.comparingByValue()).getKey();
                if (labels.get(node) != bestLabel) {
                    labels.put(node, bestLabel);
                    changed = true;
                }
            }

            if (!changed) {
                log.info("Co-Borrow Graph Label Propagation converged at iteration {}", iter + 1);
                break;
            }
        }

        // 4. Normalize cluster IDs to contiguous 1..N indices
        return normalizeClusterIds(labels);
    }

    private Map<String, Map<String, Double>> buildCoBorrowGraph() {
        List<Loan> allLoans = loanRepository.findAll();

        // Group resource IDs by userId
        Map<String, List<String>> userLoansMap = allLoans.stream()
                .collect(Collectors.groupingBy(
                        Loan::getUserId,
                        Collectors.mapping(Loan::getResourceId, Collectors.toList())
                ));

        Map<String, Map<String, Double>> graph = new HashMap<>();

        for (List<String> resourceList : userLoansMap.values()) {
            // Deduplicate per user
            Set<String> uniqueResources = new HashSet<>(resourceList);
            List<String> list = new ArrayList<>(uniqueResources);

            for (int i = 0; i < list.size(); i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    String u = list.get(i);
                    String v = list.get(j);

                    graph.computeIfAbsent(u, k -> new HashMap<>()).merge(v, 1.0, Double::sum);
                    graph.computeIfAbsent(v, k -> new HashMap<>()).merge(u, 1.0, Double::sum);
                }
            }
        }

        return graph;
    }

    private Map<String, Integer> normalizeClusterIds(Map<String, Integer> rawLabels) {
        Map<Integer, Integer> remapped = new HashMap<>();
        Map<String, Integer> result = new HashMap<>();
        int counter = 1;

        for (Map.Entry<String, Integer> entry : rawLabels.entrySet()) {
            int originalLabel = entry.getValue();
            if (!remapped.containsKey(originalLabel)) {
                remapped.put(originalLabel, counter++);
            }
            result.put(entry.getKey(), remapped.get(originalLabel));
        }

        return result;
    }
}
