// Replace the content of src/main/java/com/sih/erp/controller/HostelController.java

package com.sih.erp.controller;

// ... imports
import com.sih.erp.dto.HostelApplicationDto;
import com.sih.erp.dto.HostelDtos;
import com.sih.erp.entity.Room;
import com.sih.erp.service.HostelService;
import com.sih.erp.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sih.erp.dto.ChangeRoomRequestDto; // <-- ADD IMPORT


import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class HostelController {

    @Autowired
    private HostelService hostelService;


    @Autowired private RoomService roomService; // Assuming you create a RoomService


    // --- Hostel Staff Endpoints ---
    @GetMapping("/hostel-staff/registrations")
    @PreAuthorize("hasAuthority('ROLE_HOSTEL_ADMIN')")
    public ResponseEntity<List<HostelApplicationDto>> getAllRegistrations() {
        return ResponseEntity.ok(hostelService.getAllRegistrations());
    }

    @PostMapping("/hostel-staff/registrations/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_HOSTEL_ADMIN')")
    public ResponseEntity<?> approveRegistration(@PathVariable UUID id, @RequestBody HostelDtos.HostelApprovalRequestDto request, Principal principal) {
        // --- UPDATE THIS LINE TO PASS THE roomId ---
        hostelService.approveRegistration(id, request.getRoomId(), principal.getName());
        return ResponseEntity.ok("Offer sent to student successfully.");
    }

    @PostMapping("/hostel-staff/registrations/{id}/mark-paid")
    @PreAuthorize("hasAuthority('ROLE_HOSTEL_ADMIN')")
    public ResponseEntity<?> markFeeAsPaid(@PathVariable UUID id) {
        try {
            hostelService.markHostelFeeAsPaid(id);
            return ResponseEntity.ok("Fee status marked as paid and registration completed.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Add these endpoints to HostelController.java


    @GetMapping("/hostel-staff/rooms")
    @PreAuthorize("hasAuthority('ROLE_HOSTEL_ADMIN')")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.findAll());
    }

    @PostMapping("/hostel-staff/rooms")
    @PreAuthorize("hasAuthority('ROLE_HOSTEL_ADMIN')")
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.save(room));
    }

    @PutMapping("/hostel-staff/registrations/{id}/change-room")
    @PreAuthorize("hasAuthority('ROLE_HOSTEL_ADMIN')")
    public ResponseEntity<?> changeRoom(@PathVariable UUID id, @RequestBody ChangeRoomRequestDto request) {
        try {
            hostelService.changeAssignedRoom(id, request.getNewRoomId());
            return ResponseEntity.ok("Room changed successfully.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}