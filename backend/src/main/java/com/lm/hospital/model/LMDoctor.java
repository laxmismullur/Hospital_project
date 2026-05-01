package com.lm.hospital.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lm_doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LMDoctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String doctorCode;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String specialization;

    private String department;
    private String qualification;
    private String phone;

    @Column(unique = true)
    private String email;

    private String experience; // e.g. "5 years"
    private String consultationFee;
    private String availability; // e.g. "Mon-Fri 9AM-5PM"
    private String address;

    @Column(nullable = false)
    private boolean active = true;

    // Linked system user account (optional - doctor may have login)
    private Long userId;
    
    // Username for the doctor's login account (for easy reference)
    private String username;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
