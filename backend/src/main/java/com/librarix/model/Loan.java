package com.librarix.model;

import com.librarix.model.enums.LoanStatus;
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
@Document(collection = "loans")
public class Loan {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String resourceId;

    private Instant borrowDate;

    private Instant dueDate;

    private Instant returnDate;

    @Builder.Default
    private LoanStatus status = LoanStatus.ACTIVE;
}
