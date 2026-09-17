package com.librarix.dto;

import com.librarix.model.enums.FineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineDTO {
    private String id;
    private String loanId;
    private String resourceTitle;
    private String userId;
    private String userFullName;
    private double amount;
    private String reason;
    private FineStatus status;
    private Instant createdAt;
    private String waivedBy;
}
