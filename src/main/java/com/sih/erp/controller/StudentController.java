package com.sih.erp.controller;

import com.sih.erp.dto.HostelStatusDto;
import com.sih.erp.dto.StudentCourseDto;
import com.sih.erp.service.CourseService;
import com.sih.erp.service.HostelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student")
//@PreAuthorize("hasAuthority('ROLE_STUDENT')")
public class StudentController {

    @Autowired private CourseService courseService;
    @Autowired private HostelService hostelService;

    @GetMapping("/my-courses")
    public ResponseEntity<List<StudentCourseDto>> getMyCourses(Principal principal) {
        List<StudentCourseDto> courses = courseService.findCoursesByStudent(principal.getName());
        return ResponseEntity.ok(courses);
    }

    // --- HOSTEL METHODS ---
    @PostMapping("/hostel/apply")
    public ResponseEntity<?> applyForHostel(Principal principal) {
        hostelService.applyForHostel(principal.getName());
        return ResponseEntity.ok("Hostel application submitted successfully.");
    }

    @GetMapping("/hostel/status")
    public ResponseEntity<HostelStatusDto> getHostelStatus(Principal principal) {
        HostelStatusDto status = hostelService.getStudentHostelStatus(principal.getName());
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }

    @PostMapping("/hostel/registrations/{id}/accept")
    public ResponseEntity<?> acceptOffer(@PathVariable UUID id) {
        hostelService.studentAcceptOffer(id);
        return ResponseEntity.ok("Offer accepted. Please proceed with payment.");
    }

    @PostMapping("/hostel/registrations/{id}/reject")
    public ResponseEntity<?> rejectOffer(@PathVariable UUID id) {
        hostelService.studentRejectOffer(id);
        return ResponseEntity.ok("Offer has been rejected.");
    }

    @DeleteMapping("/hostel/registrations/{id}")
    public ResponseEntity<?> deleteRegistration(@PathVariable UUID id, Principal principal) {
        hostelService.deleteHostelRegistration(id, principal.getName());
        return ResponseEntity.ok("Registration has been withdrawn. You may apply again.");
    }
    // In src/main/java/com/sih/erp/controller/StudentController.java

    @PostMapping("/hostel/registrations/{id}/complete-payment")
    public ResponseEntity<?> completePayment(@PathVariable UUID id, Principal principal) {
        hostelService.completeHostelPayment(id, principal.getName());
        return ResponseEntity.ok("Payment confirmed. Your hostel booking is complete!");
    }


}
