package com.librarix.controller;

import com.librarix.dto.FineDTO;
import com.librarix.security.UserPrincipal;
import com.librarix.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    @GetMapping("/mine")
    public ResponseEntity<List<FineDTO>> getMyFines(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(fineService.getUserFines(userPrincipal.getId()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<List<FineDTO>> getAllFines() {
        return ResponseEntity.ok(fineService.getAllFines());
    }

    @PostMapping("/{fineId}/waive")
    @PreAuthorize("hasAnyRole('ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    public ResponseEntity<FineDTO> waiveFine(
            @PathVariable String fineId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok(fineService.waiveFine(fineId, userPrincipal.getId()));
    }
}
