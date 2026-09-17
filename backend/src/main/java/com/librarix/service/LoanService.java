package com.librarix.service;

import com.librarix.dto.LoanDTO;
import com.librarix.exception.InsufficientStockException;
import com.librarix.exception.QueueConflictException;
import com.librarix.exception.ResourceNotFoundException;
import com.librarix.model.Fine;
import com.librarix.model.Loan;
import com.librarix.model.Resource;
import com.librarix.model.User;
import com.librarix.model.enums.FineStatus;
import com.librarix.model.enums.LoanStatus;
import com.librarix.model.enums.ResourceStatus;
import com.librarix.model.enums.UrgencyLevel;
import com.librarix.repository.FineRepository;
import com.librarix.repository.LoanRepository;
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
public class LoanService {

    private final LoanRepository loanRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final FineRepository fineRepository;
    private final FineCalculationService fineCalculationService;
    private final QueueService queueService;
    private final NotificationService notificationService;

    public LoanDTO borrowResource(String resourceId, String userId, UrgencyLevel urgencyLevel) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + resourceId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // Check if user already has an active loan for this resource
        Optional<Loan> existingActiveLoan = loanRepository
                .findByUserIdAndResourceIdAndStatus(userId, resourceId, LoanStatus.ACTIVE);
        if (existingActiveLoan.isPresent()) {
            throw new QueueConflictException("You currently have an active loan for this item.");
        }

        // If resource is unavailable, auto-enqueue user into waitlist
        if (resource.getAvailableQuantity() <= 0) {
            log.info("Resource unavailable. Enqueuing user {} into waitlist queue.", user.getEmail());
            queueService.joinQueue(resourceId, userId, urgencyLevel);
            throw new InsufficientStockException("Resource is currently unavailable. You have been automatically added to the priority waitlist!");
        }

        // Decrement quantity & update status
        resource.setAvailableQuantity(resource.getAvailableQuantity() - 1);
        if (resource.getAvailableQuantity() == 0) {
            resource.setStatus(ResourceStatus.BORROWED);
        }
        resourceRepository.save(resource);

        // Create Loan (standard period: 7 days)
        Loan loan = Loan.builder()
                .userId(userId)
                .resourceId(resourceId)
                .borrowDate(Instant.now())
                .dueDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .status(LoanStatus.ACTIVE)
                .build();

        Loan savedLoan = loanRepository.save(loan);

        notificationService.sendNotification(
                userId,
                "Resource Borrowed",
                "Successfully borrowed '" + resource.getTitle() + "'. Due date: 7 days from today.",
                "LOAN_SUCCESS"
        );

        return mapToDTO(savedLoan, resource, user);
    }

    public LoanDTO returnResource(String loanId, String currentUserId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan record not found: " + loanId));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new QueueConflictException("This resource has already been returned.");
        }

        Resource resource = resourceRepository.findById(loan.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        User user = userRepository.findById(loan.getUserId()).orElse(null);

        loan.setReturnDate(Instant.now());
        loan.setStatus(LoanStatus.RETURNED);

        // Calculate fine if overdue
        double fineAmount = fineCalculationService.calculateFineAmount(loan, resource);
        if (fineAmount > 0) {
            Fine fine = Fine.builder()
                    .loanId(loan.getId())
                    .userId(loan.getUserId())
                    .amount(fineAmount)
                    .reason("Overdue return penalty for resource: " + resource.getTitle())
                    .status(FineStatus.PENDING)
                    .build();
            fineRepository.save(fine);

            notificationService.sendNotification(
                    loan.getUserId(),
                    "Overdue Fine Incurred",
                    String.format("An overdue fine of $%.2f was generated for '%s'.", fineAmount, resource.getTitle()),
                    "FINE_INCURRED"
            );
        }

        Loan savedLoan = loanRepository.save(loan);

        // Increment resource available stock
        resource.setAvailableQuantity(resource.getAvailableQuantity() + 1);
        if (resource.getAvailableQuantity() > 0) {
            resource.setStatus(ResourceStatus.AVAILABLE);
        }
        resourceRepository.save(resource);

        // Check and fulfill next person in queue automatically
        boolean queueFulfilled = queueService.processNextInQueue(resource.getId());
        if (!queueFulfilled) {
            log.info("No active queue entries for resource {}", resource.getTitle());
        }

        return mapToDTO(savedLoan, resource, user);
    }

    public List<LoanDTO> getUserLoans(String userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        return loans.stream()
                .map(loan -> {
                    Resource resource = resourceRepository.findById(loan.getResourceId()).orElse(null);
                    User user = userRepository.findById(loan.getUserId()).orElse(null);
                    return mapToDTO(loan, resource, user);
                })
                .toList();
    }

    public List<LoanDTO> getAllActiveLoans() {
        List<Loan> loans = loanRepository.findAll();
        return loans.stream()
                .map(loan -> {
                    Resource resource = resourceRepository.findById(loan.getResourceId()).orElse(null);
                    User user = userRepository.findById(loan.getUserId()).orElse(null);
                    return mapToDTO(loan, resource, user);
                })
                .toList();
    }

    private LoanDTO mapToDTO(Loan loan, Resource resource, User user) {
        double fineAmount = 0.0;
        if (resource != null) {
            fineAmount = fineCalculationService.calculateFineAmount(loan, resource);
        }

        return LoanDTO.builder()
                .id(loan.getId())
                .userId(loan.getUserId())
                .userFullName(user != null ? user.getFullName() : "Unknown")
                .resourceId(loan.getResourceId())
                .resourceTitle(resource != null ? resource.getTitle() : "Unknown")
                .resourceBarcode(resource != null ? resource.getBarcode() : "N/A")
                .borrowDate(loan.getBorrowDate())
                .dueDate(loan.getDueDate())
                .returnDate(loan.getReturnDate())
                .status(loan.getStatus())
                .calculatedFine(fineAmount)
                .build();
    }
}
