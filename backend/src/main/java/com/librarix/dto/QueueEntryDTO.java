package com.librarix.dto;

import com.librarix.model.enums.QueueStatus;
import com.librarix.model.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueEntryDTO {
    private String id;
    private String resourceId;
    private String resourceTitle;
    private String userId;
    private String userFullName;
    private Instant requestedAt;
    private UrgencyLevel urgencyLevel;
    private double calculatedPriorityScore;
    private int queuePosition;
    private QueueStatus status;
}
