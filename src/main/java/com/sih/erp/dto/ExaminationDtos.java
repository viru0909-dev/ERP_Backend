// Create new file: src/main/java/com/sih/erp/dto/ExaminationDtos.java
package com.sih.erp.dto;

import com.sih.erp.entity.ExamType;
import lombok.Data;
import java.util.List;
import java.util.UUID;

public class ExaminationDtos {
    // DTO for the teacher's entire upload request
    @Data
    public static class MarksUploadRequestDto {
        private UUID subjectId;
        private ExamType examType;
        private List<StudentMarkDto> studentMarks;
    }

    // Represents marks for a single student in the upload request
    @Data
    public static class StudentMarkDto {
        private UUID studentId;
        private Double marksObtained;
    }

    // DTO for sending the final result card to the student
    @Data
    public static class ResultCardDto {
        private List<SubjectResultDto> subjectResults;
        private Double totalMarksObtained;
        private Double totalMaxMarks;
        private Double percentage;
        private String finalStatus; // e.g., "PASS" or "FAIL"
    }

    // Represents a single subject's result within the result card
    @Data
    public static class SubjectResultDto {
        private String subjectName;
        private Double marksObtained;
        private Double totalMarks;
    }
}