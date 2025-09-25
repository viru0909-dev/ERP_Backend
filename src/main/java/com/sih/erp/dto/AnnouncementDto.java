// Create in src/main/java/com/sih/erp/dto/AnnouncementDto.java
package com.sih.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDto {
    private UUID id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String createdBy; // The full name of the staff who posted it
}