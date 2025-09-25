// Replace the entire content of src/main/java/com/sih/erp/service/HostelService.java

package com.sih.erp.service;

import com.sih.erp.dto.HostelApplicationDto;
import com.sih.erp.dto.HostelStatusDto;
import com.sih.erp.entity.*;
import com.sih.erp.repository.HostelRegistrationRepository;
import com.sih.erp.repository.RoomRepository;
import com.sih.erp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HostelService {

    @Autowired private HostelRegistrationRepository hostelRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private RoomRepository roomRepo;

    @Transactional
    public void applyForHostel(String studentEmail) {
        User student = userRepo.findByEmail(studentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Student not found: " + studentEmail));

        if (hostelRepo.findByStudent_UserId(student.getUserId()).isPresent()) {
            throw new IllegalStateException("You have already submitted a hostel application.");
        }

        HostelRegistration newRegistration = new HostelRegistration();
        newRegistration.setStudent(student);
        newRegistration.setStatus(RegistrationStatus.PENDING);
        newRegistration.setRequestedAt(LocalDateTime.now());
        hostelRepo.save(newRegistration);
    }

    @Transactional(readOnly = true)
    public HostelStatusDto getStudentHostelStatus(String studentEmail) {
        User student = userRepo.findByEmail(studentEmail).orElseThrow(() -> new UsernameNotFoundException("Student not found"));
        return hostelRepo.findByStudent_UserId(student.getUserId())
                .map(reg -> {
                    Room room = reg.getAssignedRoom();
                    return new HostelStatusDto(
                            reg.getId(),
                            reg.getStatus(),
                            room != null ? room.getRoomNumber() : null,
                            room != null ? room.getFee() : null, // Get fee from the room
                            reg.getRequestedAt(),
                            reg.getApprovedAt()
                    );
                })
                .orElse(null);
    }

    // In src/main/java/com/sih/erp/service/HostelService.java
    @Transactional(readOnly = true)
    public List<HostelApplicationDto> getAllRegistrations() {
        return hostelRepo.findAll().stream()
                .map(reg -> {
                    User student = reg.getStudent();
                    Room room = reg.getAssignedRoom();

                    // --- THIS IS THE FIX ---
                    // Check if a room is assigned before getting its details
                    String roomNumber = (room != null) ? room.getRoomNumber() : null;
                    String roomType = (room != null) ? room.getRoomType().toString() : null;

                    return new HostelApplicationDto(
                            reg.getId(),
                            student.getUserId(),
                            student.getFullName(),
                            student.getEmail(),
                            reg.getStatus(),
                            reg.getRequestedAt(),
                            roomNumber,
                            roomType
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveRegistration(UUID registrationId, UUID roomId, String staffEmail) {
        User staff = userRepo.findByEmail(staffEmail).orElseThrow(() -> new UsernameNotFoundException("Staff not found"));
        HostelRegistration registration = hostelRepo.findById(registrationId).orElseThrow(() -> new RuntimeException("Registration not found"));
        Room room = roomRepo.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));

        registration.setAssignedRoom(room);
        registration.setStatus(RegistrationStatus.APPROVED);
        registration.setApprovedBy(staff);
        registration.setApprovedAt(LocalDateTime.now());
        hostelRepo.save(registration);
    }

    @Transactional
    public void studentAcceptOffer(UUID registrationId) {
        HostelRegistration registration = hostelRepo.findById(registrationId).orElseThrow(() -> new RuntimeException("Registration not found"));
        registration.setStatus(RegistrationStatus.ACCEPTED_BY_STUDENT);
        hostelRepo.save(registration);
    }

    @Transactional
    public void studentRejectOffer(UUID registrationId) {
        HostelRegistration registration = hostelRepo.findById(registrationId).orElseThrow(() -> new RuntimeException("Registration not found"));
        registration.setStatus(RegistrationStatus.REJECTED_BY_STUDENT);
        hostelRepo.save(registration);
    }

    @Transactional
    public void deleteHostelRegistration(UUID registrationId, String studentEmail) {
        User student = userRepo.findByEmail(studentEmail).orElseThrow(() -> new UsernameNotFoundException("Student not found"));
        HostelRegistration registration = hostelRepo.findById(registrationId).orElseThrow(() -> new RuntimeException("Registration not found"));

        if (!registration.getStudent().equals(student)) {
            throw new SecurityException("You are not authorized to delete this registration.");
        }
        hostelRepo.delete(registration);
    }

    @Transactional
    public void completeHostelPayment(UUID registrationId, String studentEmail) {
        User student = userRepo.findByEmail(studentEmail).orElseThrow(() -> new UsernameNotFoundException("Student not found"));
        HostelRegistration registration = hostelRepo.findById(registrationId).orElseThrow(() -> new RuntimeException("Registration not found"));

        if (!registration.getStudent().equals(student)) {
            throw new SecurityException("You are not authorized to perform this action.");
        }
        if (registration.getStatus() != RegistrationStatus.ACCEPTED_BY_STUDENT) {
            throw new IllegalStateException("You must accept the offer before paying.");
        }

        registration.setStatus(RegistrationStatus.COMPLETED);
        hostelRepo.save(registration);
    }

    @Transactional
    public void changeAssignedRoom(UUID registrationId, UUID newRoomId) {
        HostelRegistration registration = hostelRepo.findById(registrationId).orElseThrow(() -> new RuntimeException("Registration not found"));
        Room newRoom = roomRepo.findById(newRoomId).orElseThrow(() -> new RuntimeException("New room not found"));

        long currentOccupancy = hostelRepo.countByAssignedRoom_Id(newRoomId);
        if (currentOccupancy >= newRoom.getCapacity()) {
            throw new IllegalStateException("The selected room is already at full capacity.");
        }

        registration.setAssignedRoom(newRoom);
        hostelRepo.save(registration);
    }

    @Transactional
    public void markHostelFeeAsPaid(UUID registrationId) {
        HostelRegistration registration = hostelRepo.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        if (registration.getStatus() == RegistrationStatus.ACCEPTED_BY_STUDENT) {
            registration.setFeePaid(true);
            registration.setStatus(RegistrationStatus.COMPLETED); // Move to the final state
            hostelRepo.save(registration);
        } else {
            throw new IllegalStateException("Cannot mark fee as paid if the offer has not been accepted by the student.");
        }
    }
}