package com.librarix.service;

import com.librarix.cache.LruKCacheService;
import com.librarix.dto.ResourceCreateRequest;
import com.librarix.dto.ResourceDTO;
import com.librarix.exception.QueueConflictException;
import com.librarix.exception.ResourceNotFoundException;
import com.librarix.model.Resource;
import com.librarix.model.enums.QueueStatus;
import com.librarix.model.enums.ResourceStatus;
import com.librarix.repository.ReservationQueueRepository;
import com.librarix.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ReservationQueueRepository queueRepository;
    private final LruKCacheService lruKCacheService;

    public Page<ResourceDTO> getAllResources(String search, Pageable pageable) {
        Page<Resource> resources;
        if (StringUtils.hasText(search)) {
            resources = resourceRepository.findByTitleContainingIgnoreCaseOrAuthorOrBrandContainingIgnoreCase(search, search, pageable);
        } else {
            resources = resourceRepository.findAll(pageable);
        }
        return resources.map(this::mapToDTO);
    }

    public ResourceDTO getResourceById(String id) {
        ResourceDTO cached = lruKCacheService.get(id);
        if (cached != null) {
            return cached;
        }

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));

        ResourceDTO dto = mapToDTO(resource);
        lruKCacheService.put(id, dto);
        return dto;
    }

    public ResourceDTO createResource(ResourceCreateRequest request) {
        if (resourceRepository.existsByBarcode(request.getBarcode())) {
            throw new QueueConflictException("Resource with barcode already exists: " + request.getBarcode());
        }

        Resource resource = Resource.builder()
                .barcode(request.getBarcode())
                .title(request.getTitle())
                .authorOrBrand(request.getAuthorOrBrand())
                .description(request.getDescription())
                .coverImageUrl(request.getCoverImageUrl())
                .type(request.getType())
                .totalQuantity(request.getTotalQuantity())
                .availableQuantity(request.getTotalQuantity())
                .location(request.getLocation())
                .tags(request.getTags() != null ? request.getTags() : List.of())
                .status(ResourceStatus.AVAILABLE)
                .build();

        Resource saved = resourceRepository.save(resource);
        return mapToDTO(saved);
    }

    public ResourceDTO updateResource(String id, ResourceCreateRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));

        resource.setTitle(request.getTitle());
        resource.setAuthorOrBrand(request.getAuthorOrBrand());
        resource.setDescription(request.getDescription());
        resource.setCoverImageUrl(request.getCoverImageUrl());
        resource.setType(request.getType());
        resource.setTotalQuantity(request.getTotalQuantity());
        resource.setLocation(request.getLocation());

        if (request.getTags() != null) {
            resource.setTags(request.getTags());
        }

        Resource updated = resourceRepository.save(resource);
        lruKCacheService.evict(id);
        return mapToDTO(updated);
    }

    public void deleteResource(String id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with ID: " + id));
        resourceRepository.delete(resource);
        lruKCacheService.evict(id);
    }

    public List<ResourceDTO> bulkImportResources(List<ResourceCreateRequest> requests) {
        List<ResourceDTO> imported = new ArrayList<>();
        for (ResourceCreateRequest req : requests) {
            if (!resourceRepository.existsByBarcode(req.getBarcode())) {
                imported.add(createResource(req));
            }
        }
        return imported;
    }

    public ResourceDTO mapToDTO(Resource resource) {
        long queueCount = queueRepository.countByResourceIdAndStatus(resource.getId(), QueueStatus.WAITING);
        return ResourceDTO.builder()
                .id(resource.getId())
                .barcode(resource.getBarcode())
                .title(resource.getTitle())
                .authorOrBrand(resource.getAuthorOrBrand())
                .description(resource.getDescription())
                .coverImageUrl(resource.getCoverImageUrl())
                .type(resource.getType())
                .totalQuantity(resource.getTotalQuantity())
                .availableQuantity(resource.getAvailableQuantity())
                .location(resource.getLocation())
                .tags(resource.getTags())
                .status(resource.getStatus())
                .activeQueueCount(queueCount)
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
