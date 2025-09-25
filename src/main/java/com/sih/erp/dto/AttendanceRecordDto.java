package com.sih.erp.dto;

import com.sih.erp.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDto {
    private UUID attendanceId;
    private LocalDate date;
    private AttendanceStatus status;
    private String subjectName;
}