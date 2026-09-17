package com.librarix.dto;

import com.librarix.model.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {
    private String id;
    private String userId;
    private String userFullName;
    private String resourceId;
    private String resourceTitle;
    private String resourceBarcode;
    private Instant borrowDate;
    private Instant dueDate;
    private Instant returnDate;
    private LoanStatus status;
    private double calculatedFine;
}
