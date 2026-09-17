package com.librarix.repository;

import com.librarix.model.ReservationQueueEntry;
import com.librarix.model.enums.QueueStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationQueueRepository extends MongoRepository<ReservationQueueEntry, String> {
    List<ReservationQueueEntry> findByResourceIdAndStatusOrderByCalculatedPriorityScoreDescRequestedAtAsc(
            String resourceId, QueueStatus status);
            
    List<ReservationQueueEntry> findByUserIdAndStatus(String userId, QueueStatus status);
    
    Optional<ReservationQueueEntry> findByResourceIdAndUserIdAndStatus(
            String resourceId, String userId, QueueStatus status);
            
    long countByResourceIdAndStatus(String resourceId, QueueStatus status);
}
