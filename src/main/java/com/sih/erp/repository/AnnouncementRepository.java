// Create new file: src/main/java/com/sih/erp/repository/AnnouncementRepository.java
package com.sih.erp.repository;

import com.sih.erp.entity.Announcement;
import com.sih.erp.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    // Find announcements where the given class is in the announcement's set of target classes
    List<Announcement> findByTargetClassesContainsOrderByCreatedAtDesc(SchoolClass schoolClass);
}