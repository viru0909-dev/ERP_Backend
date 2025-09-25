// Create in src/main/java/com/sih/erp/dto/StudentStatusUpdateRequest.java
package com.sih.erp.dto;

import com.sih.erp.entity.AcademicStatus;
import lombok.Data;

@Data
public class StudentStatusUpdateRequest {
    private boolean feePaid;
    private AcademicStatus academicStatus;
}