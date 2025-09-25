package com.sih.erp.dto;

import com.sih.erp.entity.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class CreateTimetableSlotRequest {
    @NotNull
    private DayOfWeek dayOfWeek;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
    @NotNull
    private UUID classId;
    @NotNull
    private UUID subjectId;
    @NotNull
    private UUID teacherId;
}