package com.librarix.controller;

import com.librarix.algorithm.AvailabilitySegmentTree;
import com.librarix.dto.ResourceCreateRequest;
import com.librarix.dto.ResourceDTO;
import com.librarix.graph.LibraryRouteOptimizer;
import com.librarix.search.TrieAutocompleteEngine;
import com.librarix.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final TrieAutocompleteEngine trieAutocompleteEngine;
    private final LibraryRouteOptimizer routeOptimizer;
    private final AvailabilitySegmentTree availabilitySegmentTree;

    @GetMapping
    public ResponseEntity<Page<ResourceDTO>> getAllResources(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(resourceService.getAllResources(search, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDTO> getResourceById(@PathVariable String id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_CURATOR', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<ResourceDTO> createResource(@Valid @RequestBody ResourceCreateRequest request) {
        ResourceDTO created = resourceService.createResource(request);
        trieAutocompleteEngine.rebuildTrie();
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_CURATOR', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<ResourceDTO> updateResource(@PathVariable String id, @Valid @RequestBody ResourceCreateRequest request) {
        ResourceDTO updated = resourceService.updateResource(id, request);
        trieAutocompleteEngine.rebuildTrie();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable String id) {
        resourceService.deleteResource(id);
        trieAutocompleteEngine.rebuildTrie();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-import")
    @PreAuthorize("hasAnyRole('ROLE_CURATOR', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<ResourceDTO>> bulkImportResources(@RequestBody List<@Valid ResourceCreateRequest> requests) {
        List<ResourceDTO> imported = resourceService.bulkImportResources(requests);
        trieAutocompleteEngine.rebuildTrie();
        return ResponseEntity.status(HttpStatus.CREATED).body(imported);
    }

    /**
     * Trie Autocomplete — O(p + k) prefix search.
     * GET /api/resources/autocomplete?prefix=dist&limit=5
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<Map<String, Object>> autocomplete(
            @RequestParam String prefix,
            @RequestParam(defaultValue = "5") int limit) {
        List<String> suggestions = trieAutocompleteEngine.autocomplete(prefix, limit);
        return ResponseEntity.ok(Map.of(
                "prefix", prefix,
                "suggestions", suggestions,
                "count", suggestions.size(),
                "trieSize", trieAutocompleteEngine.getTrieSize()
        ));
    }

    /**
     * Dijkstra Multi-Pickup Route — shortest walking path across library zones.
     * GET /api/resources/route?locations=SHELF-A,SHELF-C,SHELF-J
     */
    @GetMapping("/route")
    public ResponseEntity<Map<String, Object>> optimizePickupRoute(
            @RequestParam List<String> locations) {
        LibraryRouteOptimizer.RouteResult optimized = routeOptimizer.computeOptimalRoute(locations);
        double naiveDistance = routeOptimizer.computeNaiveRouteDistance(locations);
        double reduction = naiveDistance > 0 ? ((naiveDistance - optimized.totalDistanceMeters()) / naiveDistance) * 100 : 0;

        return ResponseEntity.ok(Map.of(
                "optimizedRoute", optimized.route(),
                "optimizedDistanceMeters", optimized.totalDistanceMeters(),
                "naiveDistanceMeters", naiveDistance,
                "distanceReductionPercent", reduction,
                "pickupCount", locations.size()
        ));
    }

    /**
     * Segment Tree Availability Forecast — O(log n) range query.
     * GET /api/resources/availability-forecast?fromDay=0&toDay=7
     */
    @GetMapping("/availability-forecast")
    public ResponseEntity<Map<String, Object>> availabilityForecast(
            @RequestParam(defaultValue = "0") int fromDay,
            @RequestParam(defaultValue = "7") int toDay) {
        int expectedReturns = availabilitySegmentTree.rangeSum(fromDay, toDay);
        return ResponseEntity.ok(Map.of(
                "fromDay", fromDay,
                "toDay", toDay,
                "expectedReturns", expectedReturns,
                "forecastWindow", availabilitySegmentTree.getForecastDays() + " days"
        ));
    }
}
