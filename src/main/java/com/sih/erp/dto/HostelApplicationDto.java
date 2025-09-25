// In src/main/java/com/sih/erp/dto/HostelApplicationDto.java
package com.sih.erp.dto;

import com.sih.erp.entity.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class HostelApplicationDto {
    private UUID registrationId;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private RegistrationStatus status;
    private LocalDateTime requestedAt;
    private String roomNumber;
    private String roomType;
}