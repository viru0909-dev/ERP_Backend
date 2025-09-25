package com.sih.erp.controller;

import com.sih.erp.entity.PaymentTransaction;
import com.sih.erp.service.PaymentService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasAuthority('ROLE_STUDENT')") // Secures all endpoints for students
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // --- DTO for Tuition Fee Payment ---
    @Data
    public static class TuitionPaymentRequest {
        private Double amount;
    }

    // --- Endpoints ---
    @PostMapping("/hostel/pay")
    public ResponseEntity<?> payHostelFee(Principal principal) {
        paymentService.processHostelPayment(principal.getName());
        return ResponseEntity.ok("Payment successful!");
    }

    @PostMapping("/tuition/pay")
    public ResponseEntity<?> payTuitionFee(@RequestBody TuitionPaymentRequest request, Principal principal) {
        try {
            paymentService.processTuitionPayment(principal.getName(), request.getAmount());
            return ResponseEntity.ok("Tuition fee payment successful!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentTransaction>> getMyPayments(Principal principal) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(principal.getName()));
    }

    @GetMapping("/receipts/{transactionId}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable UUID transactionId) {
        try {
            byte[] pdfContents = paymentService.generateReceiptPdf(transactionId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "receipt-" + transactionId + ".pdf");
            return new ResponseEntity<>(pdfContents, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}