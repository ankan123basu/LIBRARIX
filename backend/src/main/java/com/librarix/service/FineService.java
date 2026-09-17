package com.librarix.service;

import com.librarix.dto.FineDTO;
import com.librarix.exception.ResourceNotFoundException;
import com.librarix.model.Fine;
import com.librarix.model.Loan;
import com.librarix.model.Resource;
import com.librarix.model.User;
import com.librarix.model.enums.FineStatus;
import com.librarix.repository.FineRepository;
import com.librarix.repository.LoanRepository;
import com.librarix.repository.ResourceRepository;
import com.librarix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;
    private final LoanRepository loanRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public List<FineDTO> getUserFines(String userId) {
        List<Fine> fines = fineRepository.findByUserId(userId);
        return fines.stream().map(this::mapToDTO).toList();
    }

    public List<FineDTO> getAllFines() {
        List<Fine> fines = fineRepository.findAll();
        return fines.stream().map(this::mapToDTO).toList();
    }

    public FineDTO waiveFine(String fineId, String librarianUserId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine record not found: " + fineId));

        User librarian = userRepository.findById(librarianUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Librarian user not found"));

        fine.setStatus(FineStatus.WAIVED);
        fine.setWaivedBy(librarian.getFullName());

        Fine saved = fineRepository.save(fine);

        notificationService.sendNotification(
                fine.getUserId(),
                "Fine Waived",
                String.format("Your fine of $%.2f was waived by %s.", fine.getAmount(), librarian.getFullName()),
                "FINE_WAIVED"
        );

        return mapToDTO(saved);
    }

    private FineDTO mapToDTO(Fine fine) {
        User user = userRepository.findById(fine.getUserId()).orElse(null);
        String resourceTitle = "Unknown";
        if (fine.getLoanId() != null) {
            Loan loan = loanRepository.findById(fine.getLoanId()).orElse(null);
            if (loan != null) {
                Resource resource = resourceRepository.findById(loan.getResourceId()).orElse(null);
                if (resource != null) {
                    resourceTitle = resource.getTitle();
                }
            }
        }

        return FineDTO.builder()
                .id(fine.getId())
                .loanId(fine.getLoanId())
                .resourceTitle(resourceTitle)
                .userId(fine.getUserId())
                .userFullName(user != null ? user.getFullName() : "Unknown")
                .amount(fine.getAmount())
                .reason(fine.getReason())
                .status(fine.getStatus())
                .createdAt(fine.getCreatedAt())
                .waivedBy(fine.getWaivedBy())
                .build();
    }
}
