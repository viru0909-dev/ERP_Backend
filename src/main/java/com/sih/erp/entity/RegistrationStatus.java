// Create new file: src/main/java/com/sih/erp/entity/RegistrationStatus.java
package com.sih.erp.entity;
public enum RegistrationStatus {
    PENDING,
    APPROVED, // This now means "Offer Sent by Admin"
    REJECTED_BY_STAFF,
    ACCEPTED_BY_STUDENT, // Student has accepted the offer
    REJECTED_BY_STUDENT, // Student has rejected the offer
    COMPLETED
 }