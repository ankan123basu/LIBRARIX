package com.librarix.repository;

import com.librarix.model.Resource;
import com.librarix.model.enums.ResourceStatus;
import com.librarix.model.enums.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends MongoRepository<Resource, String> {
    Optional<Resource> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
    List<Resource> findByType(ResourceType type);
    List<Resource> findByStatus(ResourceStatus status);

    @org.springframework.data.mongodb.repository.Query("{ '$or': [ { 'title': { '$regex': ?0, '$options': 'i' } }, { 'authorOrBrand': { '$regex': ?1, '$options': 'i' } } ] }")
    Page<Resource> findByTitleContainingIgnoreCaseOrAuthorOrBrandContainingIgnoreCase(
            String title, String authorOrBrand, Pageable pageable);
}
