// Create new file: src/main/java/com/sih/erp/repository/MarkRepository.java
package com.sih.erp.repository;

import com.sih.erp.entity.ExamType;
import com.sih.erp.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MarkRepository extends JpaRepository<Mark, UUID> {
    List<Mark> findByStudent_UserIdAndExamType(UUID studentId, ExamType examType);
}