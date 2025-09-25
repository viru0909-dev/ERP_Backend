package com.sih.erp.repository;

import com.sih.erp.entity.TimetableSlot;
import com.sih.erp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, UUID> {
    // Find all slots for a specific class, ordered by day and then start time
    List<TimetableSlot> findBySchoolClass_ClassIdOrderByDayOfWeekAscStartTimeAsc(UUID classId);

    // Find all slots for a specific teacher, ordered by day and then start time
    List<TimetableSlot> findByTeacher_UserIdOrderByDayOfWeekAscStartTimeAsc(UUID teacherId);

    List<TimetableSlot> findByCreatedBy(User user);
    List<TimetableSlot> findByTeacher(User user);

    boolean existsByClassroom_ClassroomId(UUID classroomId);

}