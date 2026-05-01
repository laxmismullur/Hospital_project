package com.lm.hospital.controller;

import com.lm.hospital.model.LMMedicalRecord;
import com.lm.hospital.repository.LMMedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lm/medical-records")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LMMedicalRecordController {

    @Autowired
    private LMMedicalRecordRepository medicalRecordRepository;

    @GetMapping
    public List<LMMedicalRecord> getAllRecords() {
        return medicalRecordRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LMMedicalRecord> getById(@PathVariable Long id) {
        return medicalRecordRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    public List<LMMedicalRecord> getByPatient(@PathVariable Long patientId) {
        return medicalRecordRepository.findByPatientIdOrderByRecordDateDesc(patientId);
    }

    @GetMapping("/doctor/{doctorId}")
    public List<LMMedicalRecord> getByDoctor(@PathVariable Long doctorId) {
        return medicalRecordRepository.findByDoctorId(doctorId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public ResponseEntity<LMMedicalRecord> createRecord(@RequestBody LMMedicalRecord record) {
        return ResponseEntity.ok(medicalRecordRepository.save(record));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public ResponseEntity<LMMedicalRecord> updateRecord(@PathVariable Long id, @RequestBody LMMedicalRecord record) {
        return medicalRecordRepository.findById(id).map(existing -> {
            record.setId(id);
            record.setRecordDate(existing.getRecordDate());
            return ResponseEntity.ok(medicalRecordRepository.save(record));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public ResponseEntity<?> deleteRecord(@PathVariable Long id) {
        return medicalRecordRepository.findById(id).map(r -> {
            medicalRecordRepository.delete(r);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
