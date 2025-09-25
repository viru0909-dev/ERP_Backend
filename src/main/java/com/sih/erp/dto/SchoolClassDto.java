package com.sih.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolClassDto {
    private UUID classId;
    private String gradeLevel;
    private String section;
    private int sectionCapacity;
}
