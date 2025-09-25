// Create new file: src/main/java/com/sih/erp/service/AnalyticsService.java
package com.sih.erp.service;

import com.sih.erp.dto.AnalyticsDto;
import com.sih.erp.entity.RegistrationStatus;
import com.sih.erp.entity.Role;
import com.sih.erp.repository.HostelRegistrationRepository;
import com.sih.erp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HostelRegistrationRepository hostelRegistrationRepository;

    @Transactional(readOnly = true)
    public AnalyticsDto getDashboardAnalytics() {
        AnalyticsDto dto = new AnalyticsDto();

        // 1. Get total student and teacher counts
        dto.setTotalStudents(userRepository.countByRole(Role.ROLE_STUDENT));
        dto.setTotalTeachers(userRepository.countByRole(Role.ROLE_TEACHER));

        // 2. Get student count per class
        List<Object[]> results = userRepository.countStudentsPerClass();
        Map<String, Long> studentsPerClass = results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0], // Key: Class Grade Level
                        result -> (Long) result[1]    // Value: Student Count
                ));
        dto.setStudentsPerClass(studentsPerClass);

        // 3. Get total hostel resident count
        dto.setHostelResidents(hostelRegistrationRepository.countByStatus(RegistrationStatus.COMPLETED));

        // We can add more analytics here in the future (e.g., fee collection stats)

        return dto;
    }
}