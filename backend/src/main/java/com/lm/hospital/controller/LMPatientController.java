package com.lm.hospital.controller;

import com.lm.hospital.model.LMDoctor;
import com.lm.hospital.model.LMPatient;
import com.lm.hospital.model.LMPatientStatus;
import com.lm.hospital.model.LMUser;
import com.lm.hospital.repository.LMDoctorRepository;
import com.lm.hospital.repository.LMPatientRepository;
import com.lm.hospital.repository.LMUserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lm/patients")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LMPatientController {

    @Autowired
    private LMPatientRepository patientRepository;

    @Autowired
    private LMDoctorRepository doctorRepository;

    @Autowired
    private LMUserRepository userRepository;

    @GetMapping
    public List<LMPatient> getAllPatients() {
        return patientRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LMPatient> getPatientById(@PathVariable Long id) {
        return patientRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<LMPatient> searchPatients(@RequestParam String name) {
        return patientRepository.findByFullNameContainingIgnoreCase(name);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<LMPatient> getPatientsByDoctor(@PathVariable Long doctorId) {
        return patientRepository.findByAssignedDoctorId(doctorId);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('PATIENT')")
    public List<LMPatient> getPatientsByCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        LMUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return patientRepository.findByUserId(user.getId());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public ResponseEntity<LMPatient> createPatient(@Valid @RequestBody LMPatient patient, Authentication authentication) {
        patient.setPatientId("LMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Link patient to current user if they are a patient
        if (authentication != null) {
            String username = authentication.getName();
            LMUser user = userRepository.findByUsername(username).orElse(null);
            if (user != null && "PATIENT".equals(user.getRole().name())) {
                patient.setUserId(user.getId());
            }
        }

        // Auto-resolve doctor name from doctor table
        if (patient.getAssignedDoctorId() != null) {
            doctorRepository.findById(patient.getAssignedDoctorId()).ifPresent(d -> {
                patient.setAssignedDoctorName(d.getFullName());
                patient.setAssignedDoctorCode(d.getDoctorCode());
            });
        }
        return ResponseEntity.ok(patientRepository.save(patient));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<LMPatient> updatePatient(@PathVariable Long id, @Valid @RequestBody LMPatient patient) {
        return patientRepository.findById(id).map(existing -> {
            patient.setId(id);
            patient.setPatientId(existing.getPatientId());
            patient.setRegisteredAt(existing.getRegisteredAt());
            if (patient.getAssignedDoctorId() != null) {
                doctorRepository.findById(patient.getAssignedDoctorId()).ifPresent(d -> {
                    patient.setAssignedDoctorName(d.getFullName());
                    patient.setAssignedDoctorCode(d.getDoctorCode());
                });
            }
            return ResponseEntity.ok(patientRepository.save(patient));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<LMPatient> updateStatus(@PathVariable Long id, @RequestParam LMPatientStatus status) {
        return patientRepository.findById(id).map(p -> {
            p.setStatus(status);
            return ResponseEntity.ok(patientRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        return patientRepository.findById(id).map(p -> {
            patientRepository.delete(p);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
