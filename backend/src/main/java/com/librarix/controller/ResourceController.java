package com.librarix.controller;

import com.librarix.dto.ResourceCreateRequest;
import com.librarix.dto.ResourceDTO;
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

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.createResource(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_CURATOR', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<ResourceDTO> updateResource(@PathVariable String id, @Valid @RequestBody ResourceCreateRequest request) {
        return ResponseEntity.ok(resourceService.updateResource(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<Void> deleteResource(@PathVariable String id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-import")
    @PreAuthorize("hasAnyRole('ROLE_CURATOR', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<ResourceDTO>> bulkImportResources(@RequestBody List<@Valid ResourceCreateRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.bulkImportResources(requests));
    }
}
