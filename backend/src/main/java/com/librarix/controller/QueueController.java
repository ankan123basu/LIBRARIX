package com.librarix.controller;

import com.librarix.dto.QueueEntryDTO;
import com.librarix.dto.QueueJoinRequest;
import com.librarix.security.UserPrincipal;
import com.librarix.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @GetMapping("/{resourceId}")
    public ResponseEntity<List<QueueEntryDTO>> getQueueForResource(@PathVariable String resourceId) {
        return ResponseEntity.ok(queueService.getQueueForResource(resourceId));
    }

    @PostMapping("/join/{resourceId}")
    public ResponseEntity<QueueEntryDTO> joinQueue(
            @PathVariable String resourceId,
            @RequestBody(required = false) QueueJoinRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        QueueEntryDTO entry = queueService.joinQueue(
                resourceId,
                userPrincipal.getId(),
                request != null ? request.getUrgencyLevel() : null
        );
        return ResponseEntity.ok(entry);
    }
}
