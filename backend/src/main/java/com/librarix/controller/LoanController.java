package com.librarix.controller;

import com.librarix.dto.LoanDTO;
import com.librarix.dto.QueueJoinRequest;
import com.librarix.security.UserPrincipal;
import com.librarix.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/borrow/{resourceId}")
    public ResponseEntity<LoanDTO> borrowResource(
            @PathVariable String resourceId,
            @RequestBody(required = false) QueueJoinRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        LoanDTO loan = loanService.borrowResource(
                resourceId,
                userPrincipal.getId(),
                request != null ? request.getUrgencyLevel() : null
        );
        return ResponseEntity.ok(loan);
    }

    @PostMapping("/return/{loanId}")
    public ResponseEntity<LoanDTO> returnResource(
            @PathVariable String loanId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        LoanDTO loan = loanService.returnResource(loanId, userPrincipal.getId());
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<LoanDTO>> getMyLoans(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(loanService.getUserLoans(userPrincipal.getId()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<LoanDTO>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllActiveLoans());
    }
}
