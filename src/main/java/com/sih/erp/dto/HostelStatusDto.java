// Create new file: src/main/java/com/sih/erp/dto/HostelStatusDto.java
package com.sih.erp.dto;

import com.sih.erp.entity.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class HostelStatusDto {
    private UUID registrationId;
    private RegistrationStatus status;
    private String roomNumber;
    private Double feeAmount; // <-- THIS FIELD MUST BE PRESENT

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

}