// Create new file: src/main/java/com/sih/erp/service/PaymentService.java
package com.sih.erp.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.sih.erp.entity.*;
import com.sih.erp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private HostelRegistrationRepository hostelRepo;
    @Autowired private UserRepository userRepo;

    @Transactional
    public PaymentTransaction processHostelPayment(String studentEmail) {
        User student = userRepo.findByEmail(studentEmail).orElseThrow(() -> new RuntimeException("Student not found"));
        HostelRegistration registration = hostelRepo.findByStudent_UserId(student.getUserId())
                .orElseThrow(() -> new IllegalStateException("No active hostel registration found to pay for."));

        if (registration.getStatus() != RegistrationStatus.ACCEPTED_BY_STUDENT) {
            throw new IllegalStateException("You must accept the hostel offer before paying.");
        }

        // Create a new payment transaction record
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setStudent(student);
        transaction.setPurpose("HOSTEL_FEE");
        transaction.setAmount(registration.getAssignedRoom().getFee());
        transaction.setTransactionId("DUMMY-" + UUID.randomUUID().toString().substring(0, 8));
        paymentRepository.save(transaction);

        // Update the hostel registration to its final state
        registration.setFeePaid(true);
        registration.setStatus(RegistrationStatus.COMPLETED);
        hostelRepo.save(registration);

        return transaction;
    }

    public byte[] generateReceiptPdf(UUID transactionId) {
        PaymentTransaction tx = paymentRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // --- PDF Content ---
        document.add(new Paragraph("SmartCampus ERP").setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Official Fee Receipt").setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n")); // Spacer

        Table detailsTable = new Table(new float[]{1, 2});
        detailsTable.addCell("Receipt ID:");
        detailsTable.addCell(tx.getId().toString());
        detailsTable.addCell("Student Name:");
        detailsTable.addCell(tx.getStudent().getFullName());
        detailsTable.addCell("Payment Date:");
        detailsTable.addCell(tx.getPaymentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        detailsTable.addCell("Purpose:");
        detailsTable.addCell(tx.getPurpose().replace("_", " "));

        document.add(detailsTable);
        document.add(new Paragraph("\n"));

        Table amountTable = new Table(new float[]{1, 1});
        amountTable.addCell(new Paragraph("Total Amount Paid").setBold());
        amountTable.addCell(new Paragraph("₹" + String.format("%,.2f", tx.getAmount())).setBold().setTextAlignment(TextAlignment.RIGHT));
        document.add(amountTable);

        document.close();

        return baos.toByteArray();
    }

    @Transactional(readOnly = true)
    public List<PaymentTransaction> getPaymentHistory(String studentEmail) {
        User student = userRepo.findByEmail(studentEmail).orElseThrow(() -> new RuntimeException("Student not found"));
        return paymentRepository.findByStudent_UserIdOrderByPaymentDateDesc(student.getUserId());
    }




    @Transactional
    public PaymentTransaction processTuitionPayment(String studentEmail, Double amount) {
        User student = userRepo.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.isFeePaid()) {
            throw new IllegalStateException("Tuition fee has already been paid.");
        }

        // 1. Create a new payment transaction record
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setStudent(student);
        transaction.setPurpose("TUITION_FEE");
        transaction.setAmount(amount); // We can make the amount dynamic
        transaction.setTransactionId("DUMMY-" + UUID.randomUUID().toString().substring(0, 8));
        paymentRepository.save(transaction);

        // 2. Update the student's main fee status
        student.setFeePaid(true);
        userRepo.save(student);

        return transaction;
    }
}