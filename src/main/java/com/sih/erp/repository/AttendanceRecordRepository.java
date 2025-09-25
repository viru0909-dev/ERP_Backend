package com.sih.erp.repository;

import com.sih.erp.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {
    // Check if attendance has already been taken for a specific class on a specific day
    boolean existsByDateAndTimetableSlot_SlotId(LocalDate date, UUID slotId);

    // Find all attendance records for a specific student
    List<AttendanceRecord> findByStudent_UserIdOrderByDateDesc(UUID studentId);
}