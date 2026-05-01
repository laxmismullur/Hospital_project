package com.lm.hospital.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lm_patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LMPatient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String patientId;

    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String fullName;

    private LocalDate dateOfBirth;
    private String gender;
    private String bloodGroup;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(?:\\+91[-\\s]?)?[6-9]\\d{9}$", message = "Use a valid Indian phone number")
    private String phone;

    @Pattern(regexp = "(^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$)", message = "Enter a valid email address")
    private String email;
    private String address;

    @Pattern(regexp = "(^$|^(?:\\+91[-\\s]?)?[6-9]\\d{9}$)", message = "Use a valid Indian phone number")
    private String emergencyContact;
    private String allergies;

    @Column(length = 2000)
    private String medicalHistory;

    @Enumerated(EnumType.STRING)
    private LMPatientStatus status;

    // Link to user account (for patient self-management)
    private Long userId;

    // Link to a doctor from lm_doctors table
    private Long assignedDoctorId;
    private String assignedDoctorName;
    private String assignedDoctorCode;

    // Insurance / admin info
    private String insuranceProvider;
    private String insuranceNumber;

    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        registeredAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = LMPatientStatus.ACTIVE;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
