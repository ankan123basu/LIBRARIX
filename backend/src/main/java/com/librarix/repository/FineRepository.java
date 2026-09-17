package com.librarix.repository;

import com.librarix.model.Fine;
import com.librarix.model.enums.FineStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineRepository extends MongoRepository<Fine, String> {
    List<Fine> findByUserId(String userId);
    List<Fine> findByUserIdAndStatus(String userId, FineStatus status);
    List<Fine> findByLoanId(String loanId);
    long countByUserIdAndStatus(String userId, FineStatus status);
}
