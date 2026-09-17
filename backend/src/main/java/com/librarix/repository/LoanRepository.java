package com.librarix.repository;

import com.librarix.model.Loan;
import com.librarix.model.enums.LoanStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends MongoRepository<Loan, String> {
    List<Loan> findByUserId(String userId);
    List<Loan> findByUserIdAndStatus(String userId, LoanStatus status);
    List<Loan> findByResourceIdAndStatus(String resourceId, LoanStatus status);
    List<Loan> findByStatusAndDueDateBefore(LoanStatus status, Instant date);
    Optional<Loan> findByUserIdAndResourceIdAndStatus(String userId, String resourceId, LoanStatus status);
}
