// Create new file: src/main/java/com/sih/erp/service/ExaminationService.java
package com.sih.erp.service;

import com.sih.erp.dto.ExaminationDtos.*;
import com.sih.erp.entity.*;
import com.sih.erp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExaminationService {

    @Autowired private MarkRepository markRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SubjectRepository subjectRepository;

    private static final double PASSING_PERCENTAGE = 40.0;

    @Transactional
    public void uploadMarks(MarksUploadRequestDto request, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail).orElseThrow(/* ... */);
        Subject subject = subjectRepository.findById(request.getSubjectId()).orElseThrow(/* ... */);

        List<Mark> marksToSave = new ArrayList<>();
        for (StudentMarkDto studentMark : request.getStudentMarks()) {
            User student = userRepository.findById(studentMark.getStudentId()).orElseThrow(/* ... */);
            Mark mark = new Mark();
            mark.setStudent(student);
            mark.setSubject(subject);
            mark.setMarksObtained(studentMark.getMarksObtained());
            mark.setExamType(request.getExamType());
            mark.setUploadedBy(teacher);
            marksToSave.add(mark);
        }
        markRepository.saveAll(marksToSave);

        // After saving final exam marks, update the students' pass/fail status
        if (request.getExamType() == ExamType.FINAL_EXAM) {
            for (StudentMarkDto studentMark : request.getStudentMarks()) {
                processAndSaveFinalStatus(studentMark.getStudentId());
            }
        }
    }

    @Transactional(readOnly = true)
    public ResultCardDto getStudentResultCard(String studentEmail, ExamType examType) {
        User student = userRepository.findByEmail(studentEmail).orElseThrow(/* ... */);
        List<Mark> marks = markRepository.findByStudent_UserIdAndExamType(student.getUserId(), examType);

        List<SubjectResultDto> subjectResults = marks.stream().map(mark -> {
            SubjectResultDto dto = new SubjectResultDto();
            dto.setSubjectName(mark.getSubject().getName());
            dto.setMarksObtained(mark.getMarksObtained());
            dto.setTotalMarks(mark.getTotalMarks());
            return dto;
        }).collect(Collectors.toList());

        double totalMarksObtained = subjectResults.stream().mapToDouble(SubjectResultDto::getMarksObtained).sum();
        double totalMaxMarks = subjectResults.stream().mapToDouble(SubjectResultDto::getTotalMarks).sum();
        double percentage = (totalMaxMarks > 0) ? (totalMarksObtained / totalMaxMarks) * 100 : 0;

        ResultCardDto resultCard = new ResultCardDto();
        resultCard.setSubjectResults(subjectResults);
        resultCard.setTotalMarksObtained(totalMarksObtained);
        resultCard.setTotalMaxMarks(totalMaxMarks);
        resultCard.setPercentage(percentage);
        resultCard.setFinalStatus(percentage >= PASSING_PERCENTAGE ? "PASS" : "FAIL");

        return resultCard;
    }

    @Transactional
    public void processAndSaveFinalStatus(UUID studentId) {
        User student = userRepository.findById(studentId).orElseThrow(/* ... */);
        // We re-calculate here to ensure we have the full picture
        List<Mark> finalMarks = markRepository.findByStudent_UserIdAndExamType(studentId, ExamType.FINAL_EXAM);

        double totalMarksObtained = finalMarks.stream().mapToDouble(Mark::getMarksObtained).sum();
        double totalMaxMarks = finalMarks.stream().mapToDouble(Mark::getTotalMarks).sum();
        double percentage = (totalMaxMarks > 0) ? (totalMarksObtained / totalMaxMarks) * 100 : 0;

        if (percentage >= PASSING_PERCENTAGE) {
            student.setAcademicStatus(AcademicStatus.PASS);
        } else {
            student.setAcademicStatus(AcademicStatus.FAIL);
        }
        userRepository.save(student);
    }
}