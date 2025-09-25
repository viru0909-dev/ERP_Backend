package com.sih.erp.dto;

import com.sih.erp.entity.AttendanceStatus;
import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
public class AttendanceSubmissionDto {
    private UUID timetableSlotId;
    // A map where the Key is the Student's UUID and the Value is their attendance status
    private Map<UUID, AttendanceStatus> studentStatuses;
}