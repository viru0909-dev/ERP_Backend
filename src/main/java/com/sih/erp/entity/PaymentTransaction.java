// Create new file: src/main/java/com/sih/erp/entity/PaymentTransaction.java
package com.sih.erp.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String purpose; // e.g., "HOSTEL_FEE", "TUITION_FEE"

    @Column(nullable = false)
    private Double amount;

    @CreationTimestamp
    private LocalDateTime paymentDate;

    // In a real system, this would be a unique ID from Stripe, Razorpay, etc.
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;
}