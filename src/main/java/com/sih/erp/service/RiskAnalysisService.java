// Replace the content of src/main/java/com/sih/erp/service/RiskAnalysisService.java

package com.sih.erp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.erp.dto.RiskProfileDto;
import com.sih.erp.entity.*;
import com.sih.erp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RiskAnalysisService {

    @Autowired private RestTemplate restTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private MarkRepository markRepository;
    @Autowired private AttendanceRecordRepository attendanceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String pythonServiceUrl = "http://127.0.0.1:5002";

    public List<RiskProfileDto> getRiskProfilesForMentor(String mentorEmail) {
        System.out.println("LOG: Fetching students for mentor: " + mentorEmail);
        User mentor = userRepository.findByEmail(mentorEmail).orElseThrow(() -> new RuntimeException("Mentor not found"));

        List<User> students = userRepository.findByMentor_UserId(mentor.getUserId());
        System.out.println("LOG: Found " + students.size() + " students to analyze.");

        return students.stream()
                .map(this::calculateRiskProfile)
                .collect(Collectors.toList());
    }

    // In src/main/java/com/sih/erp/service/RiskAnalysisService.java

    private RiskProfileDto calculateRiskProfile(User student) {
        System.out.println("\n--- Analyzing student: " + student.getFullName() + " ---");

        double attendancePercentage = calculateAttendance(student.getUserId());
        double lastExamScore = getLatestExamScore(student.getUserId());
        boolean feePaid = student.isFeePaid(); // Changed from int to boolean

        System.out.println("  - Data gathered: Attendance=" + attendancePercentage + "%, Score=" + lastExamScore + "%, Fee Paid=" + feePaid);

        double riskProbability = 0.0;
        try {
            System.out.println("  - Calling Python ML service...");
            riskProbability = callPythonRiskPredictor(attendancePercentage, lastExamScore, feePaid ? 1 : 0);
            System.out.println("  - ML Service responded with risk: " + riskProbability);
        } catch (Exception e) {
            System.err.println("  - !!! ML service call FAILED: " + e.getMessage());
            if (attendancePercentage < 75 || lastExamScore < 40 || !feePaid) riskProbability = 0.75;
            System.out.println("  - Using fallback risk: " + riskProbability);
        }

        // --- THIS IS THE FIX ---
        // We now pass all 6 required fields to the constructor
        return new RiskProfileDto(
                student.getUserId(),
                student.getFullName(),
                riskProbability,
                attendancePercentage,
                lastExamScore,
                feePaid
        );
    }
    private double calculateAttendance(UUID studentId) {
        List<AttendanceRecord> records = attendanceRepository.findByStudent_UserIdOrderByDateDesc(studentId);
        if (records.isEmpty()) return 100.0;
        long presentCount = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT || r.getStatus() == AttendanceStatus.LATE).count();
        return ((double) presentCount / records.size()) * 100.0;
    }

    private double getLatestExamScore(UUID studentId) {
        List<Mark> finalMarks = markRepository.findByStudent_UserIdAndExamType(studentId, ExamType.FINAL_EXAM);
        if (finalMarks.isEmpty()) return 0.0;
        double totalMarksObtained = finalMarks.stream().mapToDouble(Mark::getMarksObtained).sum();
        double totalMaxMarks = finalMarks.stream().mapToDouble(Mark::getTotalMarks).sum();
        return (totalMaxMarks > 0) ? (totalMarksObtained / totalMaxMarks) * 100.0 : 0.0;
    }

    private double callPythonRiskPredictor(double attendance, double score, int feePaid) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "attendancePercentage", attendance,
                "lastExamScore", score,
                "feePaid", feePaid
        );

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        String url = pythonServiceUrl + "/predict";

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        JsonNode responseJson = objectMapper.readTree(response.getBody());
        return responseJson.get("risk_probability").asDouble();
    }
    public RiskProfileDto getDetailedRiskProfile(UUID studentId) {
        System.out.println("LOG: Fetching detailed risk profile for studentId: " + studentId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // 1. Gather the same data points
        double attendancePercentage = calculateAttendance(student.getUserId());
        double lastExamScore = getLatestExamScore(student.getUserId());
        boolean feePaid = student.isFeePaid();

        // 2. Get the risk probability from the ML model
        double riskProbability = 0.0;
        try {
            riskProbability = callPythonRiskPredictor(attendancePercentage, lastExamScore, feePaid ? 1 : 0);
        } catch (Exception e) {
            System.err.println("  - !!! ML service call FAILED during detail fetch: " + e.getMessage());
            if (attendancePercentage < 75 || lastExamScore < 40 || !feePaid) riskProbability = 0.75;
        }

        // 3. Return the new, more detailed DTO
        return new RiskProfileDto(
                student.getUserId(),
                student.getFullName(),
                riskProbability,
                attendancePercentage,
                lastExamScore,
                feePaid
        );
    }

}