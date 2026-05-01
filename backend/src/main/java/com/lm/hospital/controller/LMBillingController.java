package com.lm.hospital.controller;

import com.lm.hospital.model.LMBilling;
import com.lm.hospital.model.LMPaymentStatus;
import com.lm.hospital.repository.LMBillingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lm/billing")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LMBillingController {

    @Autowired
    private LMBillingRepository billingRepository;

    @GetMapping
    public List<LMBilling> getAllBills() {
        return billingRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LMBilling> getById(@PathVariable Long id) {
        return billingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    public List<LMBilling> getByPatient(@PathVariable Long patientId) {
        return billingRepository.findByPatientId(patientId);
    }

    @GetMapping("/pending")
    public List<LMBilling> getPendingBills() {
        return billingRepository.findByPaymentStatus(LMPaymentStatus.PENDING);
    }

    @PostMapping
    public ResponseEntity<LMBilling> createBill(@RequestBody LMBilling billing) {
        billing.setInvoiceNumber("LM-INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return ResponseEntity.ok(billingRepository.save(billing));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LMBilling> updateBill(@PathVariable Long id, @RequestBody LMBilling billing) {
        return billingRepository.findById(id).map(existing -> {
            billing.setId(id);
            billing.setInvoiceNumber(existing.getInvoiceNumber());
            billing.setCreatedAt(existing.getCreatedAt());
            return ResponseEntity.ok(billingRepository.save(billing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<LMBilling> markAsPaid(@PathVariable Long id) {
        return billingRepository.findById(id).map(b -> {
            b.setPaymentStatus(LMPaymentStatus.PAID);
            b.setPaidAmount(b.getTotalAmount());
            b.setPaidAt(LocalDateTime.now());
            return ResponseEntity.ok(billingRepository.save(b));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBill(@PathVariable Long id) {
        return billingRepository.findById(id).map(b -> {
            billingRepository.delete(b);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
