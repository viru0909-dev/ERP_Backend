// Create new file: src/main/java/com/sih/erp/dto/CourseDto.java
package com.sih.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private UUID courseId;
    private UUID subjectId;
    private String subjectName;
    private UUID teacherId;
    private String teacherName;
}