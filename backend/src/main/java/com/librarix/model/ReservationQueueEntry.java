package com.librarix.model;

import com.librarix.model.enums.QueueStatus;
import com.librarix.model.enums.UrgencyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "reservation_queues")
public class ReservationQueueEntry {

    @Id
    private String id;

    @Indexed
    private String resourceId;

    @Indexed
    private String userId;

    private Instant requestedAt;

    @Builder.Default
    private UrgencyLevel urgencyLevel = UrgencyLevel.STANDARD;

    private double calculatedPriorityScore;

    private int queuePosition;

    @Builder.Default
    private QueueStatus status = QueueStatus.WAITING;
}
