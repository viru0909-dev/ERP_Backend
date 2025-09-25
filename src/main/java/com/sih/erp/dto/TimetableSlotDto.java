package com.sih.erp.dto;

import com.sih.erp.entity.DayOfWeek;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableSlotDto {
    private UUID slotId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private SchoolClassDto schoolClass;
    private SubjectDto subject;
    private UserProfileDto teacher; // Using UserProfileDto to represent the teacher
}