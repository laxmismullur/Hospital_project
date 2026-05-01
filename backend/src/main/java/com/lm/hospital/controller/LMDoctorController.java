package com.lm.hospital.controller;

import com.lm.hospital.model.LMDoctor;
import com.lm.hospital.model.LMUser;
import com.lm.hospital.model.LMRole;
import com.lm.hospital.dto.LMDoctorLoginCredentials;
import com.lm.hospital.repository.LMDoctorRepository;
import com.lm.hospital.repository.LMUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lm/doctors")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LMDoctorController {

    @Autowired
    private LMDoctorRepository doctorRepository;

    @Autowired
    private LMUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<LMDoctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @GetMapping("/active")
    public List<LMDoctor> getActiveDoctors() {
        return doctorRepository.findByActiveTrue();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LMDoctor> getDoctorById(@PathVariable Long id) {
        return doctorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<LMDoctor> searchDoctors(@RequestParam String name) {
        return doctorRepository.findByFullNameContainingIgnoreCase(name);
    }

    @GetMapping("/department/{dept}")
    public List<LMDoctor> getByDepartment(@PathVariable String dept) {
        return doctorRepository.findByDepartmentContainingIgnoreCase(dept);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDoctor(@RequestBody LMDoctor doctor) {
        if (doctor.getEmail() != null && doctorRepository.existsByEmail(doctor.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        
        // Generate unique doctor code
        doctor.setDoctorCode("LMD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        
        // Save doctor
        LMDoctor savedDoctor = doctorRepository.save(doctor);
        
        // Create a corresponding user account for the doctor
        String username = createDoctorUser(savedDoctor);
        
        // Return response with doctor info and login credentials
        Map<String, Object> response = new HashMap<>();
        response.put("doctor", savedDoctor);
        response.put("loginCredentials", new LMDoctorLoginCredentials(
                savedDoctor.getId(),
                savedDoctor.getFullName(),
                username,
                "doctor123",
                "Doctor account created successfully. Please share the login credentials with the doctor. " +
                "They can login using the username and default password, and should change the password on first login."
        ));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a user account for a doctor with a default password
     * Username: doctor_<doctorId>_<doctorCode>
     * Default Password: doctor123
     * Returns the generated username
     */
    private String createDoctorUser(LMDoctor doctor) {
        try {
            // Generate unique username for the doctor
            String username = "doctor_" + doctor.getId() + "_" + doctor.getDoctorCode().toLowerCase();
            
            // Check if user already exists
            if (userRepository.existsByUsername(username) || userRepository.existsByEmail(doctor.getEmail())) {
                return username;
            }
            
            // Create user account with default password
            LMUser doctorUser = LMUser.builder()
                    .username(username)
                    .password(passwordEncoder.encode("doctor123"))  // Default password
                    .fullName(doctor.getFullName())
                    .email(doctor.getEmail())
                    .phone(doctor.getPhone())
                    .role(LMRole.DOCTOR)
                    .active(true)
                    .build();
            
            LMUser savedUser = userRepository.save(doctorUser);
            
            // Update doctor record with userId and username
            doctor.setUserId(savedUser.getId());
            doctor.setUsername(username);
            doctorRepository.save(doctor);
            
            return username;
            
        } catch (Exception e) {
            // Log error but don't fail doctor creation if user creation fails
            System.err.println("Failed to create user account for doctor: " + e.getMessage());
            return null;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LMDoctor> updateDoctor(@PathVariable Long id, @RequestBody LMDoctor doctor) {
        return doctorRepository.findById(id).map(existing -> {
            doctor.setId(id);
            doctor.setDoctorCode(existing.getDoctorCode());
            doctor.setCreatedAt(existing.getCreatedAt());
            return ResponseEntity.ok(doctorRepository.save(doctor));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LMDoctor> toggleActive(@PathVariable Long id) {
        return doctorRepository.findById(id).map(d -> {
            d.setActive(!d.isActive());
            return ResponseEntity.ok(doctorRepository.save(d));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        return doctorRepository.findById(id).map(d -> {
            doctorRepository.delete(d);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
