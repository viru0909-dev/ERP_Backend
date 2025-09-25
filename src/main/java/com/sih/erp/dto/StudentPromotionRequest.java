// Create in src/main/java/com/sih/erp/dto/StudentPromotionRequest.java
package com.sih.erp.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class StudentPromotionRequest {
    private List<UUID> studentIds;
    private UUID nextClassId;
}