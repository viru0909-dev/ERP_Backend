// Create new file: src/main/java/com/sih/erp/repository/PaymentRepository.java
package com.sih.erp.repository;

import com.sih.erp.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentTransaction, UUID> {
    List<PaymentTransaction> findByStudent_UserIdOrderByPaymentDateDesc(UUID studentId);
}