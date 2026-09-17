package com.librarix.controller;

import com.librarix.cache.LruKCacheService;
import com.librarix.dto.ShelfPositionDTO;
import com.librarix.graph.CoBorrowGraphService;
import com.librarix.model.Loan;
import com.librarix.model.Resource;
import com.librarix.repository.LoanRepository;
import com.librarix.repository.ResourceRepository;
import com.librarix.shelf.ShelfSlottingOptimizer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shelf")
@RequiredArgsConstructor
public class ShelfController {

    private final ResourceRepository resourceRepository;
    private final LoanRepository loanRepository;
    private final CoBorrowGraphService coBorrowGraphService;
    private final ShelfSlottingOptimizer shelfSlottingOptimizer;
    private final LruKCacheService lruKCacheService;

    @GetMapping("/layout")
    public ResponseEntity<List<ShelfPositionDTO>> get3DShelfLayout() {
        List<Resource> resources = resourceRepository.findAll();
        List<String> resourceIds = resources.stream().map(Resource::getId).toList();

        Map<String, Integer> clusterMap = coBorrowGraphService.computeCoBorrowClusters(resourceIds);

        List<Loan> allLoans = loanRepository.findAll();
        Map<String, Long> loanCountMap = allLoans.stream()
                .collect(Collectors.groupingBy(Loan::getResourceId, Collectors.counting()));

        List<ShelfPositionDTO> layout = shelfSlottingOptimizer.optimize3DLayout(resources, clusterMap, loanCountMap);
        return ResponseEntity.ok(layout);
    }

    @PostMapping("/reoptimize")
    @PreAuthorize("hasAnyRole('ROLE_CURATOR', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> reoptimizeShelfLayout() {
        lruKCacheService.clear();

        List<Resource> resources = resourceRepository.findAll();
        List<String> resourceIds = resources.stream().map(Resource::getId).toList();
        Map<String, Integer> clusterMap = coBorrowGraphService.computeCoBorrowClusters(resourceIds);

        return ResponseEntity.ok(Map.of(
                "status", "REOPTIMIZED",
                "totalAssetsSlotting", resources.size(),
                "clustersFormed", new HashSet<>(clusterMap.values()).size(),
                "timestamp", System.currentTimeMillis()
        ));
    }
}
