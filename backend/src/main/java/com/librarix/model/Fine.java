package com.librarix.model;

import com.librarix.model.enums.FineStatus;
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
@Document(collection = "fines")
public class Fine {

    @Id
    private String id;

    @Indexed
    private String loanId;

    @Indexed
    private String userId;

    private double amount;

    private String reason;

    @Builder.Default
    private FineStatus status = FineStatus.PENDING;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private String waivedBy;
}
