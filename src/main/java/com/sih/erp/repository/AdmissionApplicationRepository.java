package com.sih.erp.repository;

import com.sih.erp.entity.AdmissionApplication;
import com.sih.erp.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdmissionApplicationRepository extends JpaRepository<AdmissionApplication, UUID> {

    List<AdmissionApplication> findByStatusOrderByAppliedAtDesc(ApplicationStatus status);

    // --- THIS IS THE FIX ---
    // The method name now correctly matches the 'applicantEmail' field in the entity.
    Optional<AdmissionApplication> findByApplicantEmail(String email);

}