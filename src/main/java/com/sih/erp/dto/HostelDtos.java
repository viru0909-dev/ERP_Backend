// Create new file: src/main/java/com/sih/erp/dto/HostelDtos.java
package com.sih.erp.dto;
import com.sih.erp.entity.RegistrationStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

public class HostelDtos {
    @Data public static class HostelStatusDto {
        private UUID registrationId;
        private RegistrationStatus status;
        private Double feeAmount; // <-- ADD THIS


        private String roomNumber;
        private LocalDateTime requestedAt;
    }
    @Data public static class HostelApprovalRequestDto {
        private String roomNumber;
        private Double feeAmount;
        private UUID roomId;

    }
}