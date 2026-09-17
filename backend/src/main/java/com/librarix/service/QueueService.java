package com.librarix.service;

import com.librarix.algorithm.PriorityQueueEngine;
import com.librarix.dto.QueueEntryDTO;
import com.librarix.exception.QueueConflictException;
import com.librarix.exception.ResourceNotFoundException;
import com.librarix.model.Loan;
import com.librarix.model.ReservationQueueEntry;
import com.librarix.model.Resource;
import com.librarix.model.User;
import com.librarix.model.enums.LoanStatus;
import com.librarix.model.enums.QueueStatus;
import com.librarix.model.enums.ResourceStatus;
import com.librarix.model.enums.UrgencyLevel;
import com.librarix.repository.LoanRepository;
import com.librarix.repository.ReservationQueueRepository;
import com.librarix.repository.ResourceRepository;
import com.librarix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final ReservationQueueRepository queueRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final PriorityQueueEngine priorityQueueEngine;
    private final NotificationService notificationService;

    public QueueEntryDTO joinQueue(String resourceId, String userId, UrgencyLevel urgencyLevel) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Optional<ReservationQueueEntry> existingEntry = queueRepository
                .findByResourceIdAndUserIdAndStatus(resourceId, userId, QueueStatus.WAITING);

        if (existingEntry.isPresent()) {
            throw new QueueConflictException("You are already waiting in queue for resource: " + resource.getTitle());
        }

        UrgencyLevel finalUrgency = urgencyLevel != null ? urgencyLevel : UrgencyLevel.STANDARD;

        ReservationQueueEntry entry = ReservationQueueEntry.builder()
                .resourceId(resourceId)
                .userId(userId)
                .requestedAt(Instant.now())
                .urgencyLevel(finalUrgency)
                .status(QueueStatus.WAITING)
                .build();

        double score = priorityQueueEngine.calculatePriorityScore(entry, user);
        entry.setCalculatedPriorityScore(score);

        ReservationQueueEntry saved = queueRepository.save(entry);

        recalculateQueuePositions(resourceId);

        ReservationQueueEntry updated = queueRepository.findById(saved.getId()).orElse(saved);

        notificationService.sendNotification(
                userId,
                "Joined Queue Waitlist",
                "You are position #" + updated.getQueuePosition() + " for: " + resource.getTitle(),
                "QUEUE_JOIN"
        );

        return mapToDTO(updated, resource, user);
    }

    public List<QueueEntryDTO> getQueueForResource(String resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));

        List<ReservationQueueEntry> entries = queueRepository
                .findByResourceIdAndStatusOrderByCalculatedPriorityScoreDescRequestedAtAsc(resourceId, QueueStatus.WAITING);

        return entries.stream()
                .map(entry -> {
                    User user = userRepository.findById(entry.getUserId()).orElse(null);
                    return mapToDTO(entry, resource, user);
                })
                .toList();
    }

    public void recalculateQueuePositions(String resourceId) {
        List<ReservationQueueEntry> entries = queueRepository
                .findByResourceIdAndStatusOrderByCalculatedPriorityScoreDescRequestedAtAsc(resourceId, QueueStatus.WAITING);

        int pos = 1;
        for (ReservationQueueEntry entry : entries) {
            User user = userRepository.findById(entry.getUserId()).orElse(null);
            if (user != null) {
                double newScore = priorityQueueEngine.calculatePriorityScore(entry, user);
                entry.setCalculatedPriorityScore(newScore);
            }
            entry.setQueuePosition(pos++);
            queueRepository.save(entry);
        }
    }

    public boolean processNextInQueue(String resourceId) {
        List<ReservationQueueEntry> entries = queueRepository
                .findByResourceIdAndStatusOrderByCalculatedPriorityScoreDescRequestedAtAsc(resourceId, QueueStatus.WAITING);

        if (entries.isEmpty()) {
            return false;
        }

        ReservationQueueEntry nextEntry = entries.get(0);
        Resource resource = resourceRepository.findById(resourceId).orElse(null);
        User user = userRepository.findById(nextEntry.getUserId()).orElse(null);

        if (resource == null || user == null) {
            return false;
        }

        // Fulfill queue entry
        nextEntry.setStatus(QueueStatus.FULFILLED);
        queueRepository.save(nextEntry);

        // Auto-create loan for user
        Loan loan = Loan.builder()
                .userId(user.getId())
                .resourceId(resource.getId())
                .borrowDate(Instant.now())
                .dueDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .status(LoanStatus.ACTIVE)
                .build();
        loanRepository.save(loan);

        // Update resource quantity/status
        resource.setAvailableQuantity(Math.max(0, resource.getAvailableQuantity() - 1));
        if (resource.getAvailableQuantity() == 0) {
            resource.setStatus(ResourceStatus.BORROWED);
        }
        resourceRepository.save(resource);

        // Send Real-Time STOMP notification to user
        notificationService.sendNotification(
                user.getId(),
                "Item Ready! 🎉 You're next in line!",
                "Your reserved resource '" + resource.getTitle() + "' is now ready for pickup!",
                "QUEUE_FULFILLED"
        );

        // Recalculate positions for remaining entries
        recalculateQueuePositions(resourceId);

        log.info("Queue auto-fulfilled for user {} on resource {}", user.getEmail(), resource.getTitle());
        return true;
    }

    private QueueEntryDTO mapToDTO(ReservationQueueEntry entry, Resource resource, User user) {
        return QueueEntryDTO.builder()
                .id(entry.getId())
                .resourceId(entry.getResourceId())
                .resourceTitle(resource != null ? resource.getTitle() : "Unknown")
                .userId(entry.getUserId())
                .userFullName(user != null ? user.getFullName() : "Unknown")
                .requestedAt(entry.getRequestedAt())
                .urgencyLevel(entry.getUrgencyLevel())
                .calculatedPriorityScore(entry.getCalculatedPriorityScore())
                .queuePosition(entry.getQueuePosition())
                .status(entry.getStatus())
                .build();
    }
}
