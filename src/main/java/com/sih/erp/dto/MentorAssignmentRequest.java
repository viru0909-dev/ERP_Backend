// Create new file: src/main/java/com/sih/erp/dto/MentorAssignmentRequest.java
package com.sih.erp.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class MentorAssignmentRequest {
    private List<UUID> studentIds;
    private UUID mentorId;
}