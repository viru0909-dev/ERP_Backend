// Create in src/main/java/com/sih/erp/dto/CourseDesignDto.java
package com.sih.erp.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CourseDesignDto {
    private UUID subjectId;
    private UUID teacherId;
}