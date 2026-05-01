package com.lm.hospital.controller;

import com.lm.hospital.model.Staff;
import com.lm.hospital.repository.LMAddStaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lm/staff")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LMAddStaffController {

    @Autowired
    private LMAddStaffRepository repo;

    // =========================
    // CREATE STAFF (FIXED)
    // =========================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Staff> save(@RequestBody Staff staff) {

        // ROLE NORMALIZATION
        if (staff.getRole() != null) {
            String role = staff.getRole().toUpperCase();

            if (role.equals("ROLE_NURSE")) {
                role = "NURSE";
            } else if (role.equals("ROLE_RECEPTIONIST")) {
                role = "RECEPTIONIST";
            }

            staff.setRole(role);
        }

        // ✅ FIX 1: Handle specialization properly
        if ("RECEPTIONIST".equals(staff.getRole())) {
            staff.setSpecialization(null);
        }

        return ResponseEntity.ok(repo.save(staff));
    }

    // =========================
    // GET ALL STAFF
    // =========================
    @GetMapping
    public List<Staff> getAll() {
        return repo.findAll();
    }

    // =========================
    // GET BY ID
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<Staff> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // GET BY ROLE
    // =========================
    @GetMapping("/role/{role}")
    public List<Staff> getByRole(@PathVariable String role) {

        if (role != null) {
            role = role.toUpperCase();

            if (role.equals("ROLE_NURSE")) {
                role = "NURSE";
            } else if (role.equals("ROLE_RECEPTIONIST")) {
                role = "RECEPTIONIST";
            }
        }

        return repo.findByRoleIgnoreCase(role);
    }

    // =========================
    // UPDATE STAFF (FULL FIX)
    // =========================
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Staff> update(@PathVariable Long id, @RequestBody Staff staff) {

        return repo.findById(id).map(existing -> {

            // BASIC FIELDS
            existing.setFullName(staff.getFullName());
            existing.setEmail(staff.getEmail());
            existing.setPhone(staff.getPhone());
            existing.setDepartment(staff.getDepartment());
            existing.setActive(staff.isActive());

            // ✅ FIX 2: SAVE SPECIALIZATION
            existing.setSpecialization(staff.getSpecialization());

            // ROLE NORMALIZATION
            if (staff.getRole() != null) {
                String role = staff.getRole().toUpperCase();

                if (role.equals("ROLE_NURSE")) {
                    role = "NURSE";
                } else if (role.equals("ROLE_RECEPTIONIST")) {
                    role = "RECEPTIONIST";
                }

                existing.setRole(role);
            }

            // ✅ FIX 3: REMOVE specialization for receptionist
            if ("RECEPTIONIST".equals(existing.getRole())) {
                existing.setSpecialization(null);
            }

            return ResponseEntity.ok(repo.save(existing));

        }).orElse(ResponseEntity.notFound().build());
    }

    // =========================
    // DELETE STAFF
    // =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {

        return repo.findById(id).map(staff -> {
            repo.delete(staff);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}