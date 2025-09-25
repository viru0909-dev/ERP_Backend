// Create new file: src/main/java/com/sih/erp/repository/HostelRegistrationRepository.java
package com.sih.erp.repository;
import com.sih.erp.entity.HostelRegistration;
import com.sih.erp.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface HostelRegistrationRepository extends JpaRepository<HostelRegistration, UUID> {
    Optional<HostelRegistration> findByStudent_UserId(UUID studentId);
    long countByAssignedRoom_Id(UUID roomId);
    long countByStatus(RegistrationStatus status);

}