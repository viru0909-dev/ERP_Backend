// Create in src/main/java/com/sih/erp/dto/AnnouncementRequestDto.java
package com.sih.erp.dto;

import lombok.Data;
import java.util.Set;
import java.util.UUID;

@Data
public class AnnouncementRequestDto {
    private String title;
    private String content;
    private Set<UUID> targetClassIds;
}