package com.lm.hospital.controller;

import com.lm.hospital.model.*;
import com.lm.hospital.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/lm/appointments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LMAppointmentController {

    @Autowired private LMAppointmentRepository appointmentRepository;
    @Autowired private LMDoctorRepository doctorRepository;
    @Autowired private LMPatientRepository patientRepository;
    @Autowired private LMNotificationRepository notificationRepository;
    @Autowired private LMUserRepository userRepository;

    // ===================== VIEW =====================

    @GetMapping
    public List<LMAppointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LMAppointment> getById(@PathVariable Long id) {
        return appointmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/doctor/{doctorId}")
    public List<LMAppointment> getByDoctor(@PathVariable Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @GetMapping("/patient/{patientId}")
    public List<LMAppointment> getByPatient(@PathVariable Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @GetMapping("/today")
    public List<LMAppointment> getTodayAppointments() {
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return appointmentRepository.findByAppointmentDateBetween(start, end);
    }

    @GetMapping("/upcoming")
    public List<LMAppointment> getUpcoming() {
        return appointmentRepository.findByAppointmentDateAfterAndStatusNot(
                LocalDateTime.now(), LMAppointmentStatus.CANCELLED);
    }

    @GetMapping("/user")
    public List<LMAppointment> getMyAppointments(Authentication authentication) {
        if (authentication == null) return List.of();

        String username = authentication.getName();
        LMUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<LMPatient> patients = patientRepository.findByUserId(user.getId());
        if (patients.isEmpty()) return List.of();

        return appointmentRepository.findByPatientId(patients.get(0).getId());
    }

    // ===================== CREATE =====================

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> createAppointment(@RequestBody LMAppointment appointment,
                                               Authentication authentication) {

        // Extra backend safety
        String username = authentication.getName();
        LMUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().name().equals("PATIENT")) {
            return ResponseEntity.status(403).body("Only patient can book appointment");
        }

        // Auto doctor details
        if (appointment.getDoctorId() != null) {
            doctorRepository.findById(appointment.getDoctorId()).ifPresent(d -> {
                appointment.setDoctorName(d.getFullName());
                appointment.setDoctorCode(d.getDoctorCode());
                appointment.setDepartment(d.getDepartment());
                appointment.setSpecialization(d.getSpecialization());
            });
        }

        // Auto patient details
        if (appointment.getPatientId() != null) {
            patientRepository.findById(appointment.getPatientId()).ifPresent(p ->
                    appointment.setPatientName(p.getFullName()));
        }

        LMAppointment savedAppointment = appointmentRepository.save(appointment);

        // Notification
        sendAppointmentNotification(savedAppointment,
                LMNotificationType.APPOINTMENT_BOOKED,
                "Appointment Booked",
                authentication);

        return ResponseEntity.ok(savedAppointment);
    }

    // ===================== UPDATE (DOCTOR ONLY) =====================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<LMAppointment> updateAppointment(@PathVariable Long id,
                                                           @RequestBody LMAppointment appointment) {

        return appointmentRepository.findById(id).map(existing -> {

            appointment.setId(id);
            appointment.setCreatedAt(existing.getCreatedAt());

            if (appointment.getDoctorId() != null) {
                doctorRepository.findById(appointment.getDoctorId()).ifPresent(d -> {
                    appointment.setDoctorName(d.getFullName());
                    appointment.setDoctorCode(d.getDoctorCode());
                    appointment.setDepartment(d.getDepartment());
                    appointment.setSpecialization(d.getSpecialization());
                });
            }

            return ResponseEntity.ok(appointmentRepository.save(appointment));

        }).orElse(ResponseEntity.notFound().build());
    }

    // ===================== STATUS UPDATE (DOCTOR ONLY) =====================

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<LMAppointment> updateStatus(@PathVariable Long id,
                                                     @RequestParam LMAppointmentStatus status,
                                                     @RequestParam(required = false) String doctorNotes,
                                                     Authentication authentication) {

        return appointmentRepository.findById(id).map(a -> {

            LMAppointmentStatus oldStatus = a.getStatus();
            a.setStatus(status);

            if (doctorNotes != null) {
                a.setDoctorNotes(doctorNotes);
            }

            LMAppointment savedAppointment = appointmentRepository.save(a);

            // Notifications
            if (status == LMAppointmentStatus.CONFIRMED && oldStatus != LMAppointmentStatus.CONFIRMED) {
                sendAppointmentNotification(savedAppointment,
                        LMNotificationType.APPOINTMENT_CONFIRMED,
                        "Appointment Confirmed",
                        authentication);

            } else if (status == LMAppointmentStatus.CANCELLED && oldStatus != LMAppointmentStatus.CANCELLED) {
                sendAppointmentNotification(savedAppointment,
                        LMNotificationType.APPOINTMENT_CANCELLED,
                        "Appointment Cancelled",
                        authentication);
            }

            return ResponseEntity.ok(savedAppointment);

        }).orElse(ResponseEntity.notFound().build());
    }

    // ===================== DELETE =====================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {

        return appointmentRepository.findById(id).map(a -> {
            appointmentRepository.delete(a);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // ===================== NOTIFICATION =====================

    private void sendAppointmentNotification(LMAppointment appointment,
                                             LMNotificationType type,
                                             String title,
                                             Authentication authentication) {

        try {
            LMPatient patient = patientRepository.findById(appointment.getPatientId()).orElse(null);
            if (patient == null || patient.getUserId() == null) return;

            String senderUsername = authentication != null ? authentication.getName() : "system";
            LMUser sender = userRepository.findByUsername(senderUsername).orElse(null);
            if (sender == null) return;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
            String formattedDate = appointment.getAppointmentDate().format(formatter);

            String message;

            switch (type) {
                case APPOINTMENT_BOOKED:
                    message = "Your appointment with " + appointment.getDoctorName() +
                            " has been booked for " + formattedDate;
                    break;

                case APPOINTMENT_CONFIRMED:
                    message = "Your appointment with " + appointment.getDoctorName() +
                            " on " + formattedDate + " has been confirmed.";
                    break;

                case APPOINTMENT_CANCELLED:
                    message = "Your appointment with " + appointment.getDoctorName() +
                            " on " + formattedDate + " has been cancelled.";
                    break;

                default:
                    message = title;
            }

            LMNotification notification = LMNotification.builder()
                    .recipientId(patient.getUserId())
                    .recipientName(patient.getFullName())
                    .senderId(sender.getId())
                    .senderName(sender.getFullName())
                    .title(title)
                    .message(message)
                    .type(type)
                    .referenceId(appointment.getId())
                    .read(false)
                    .build();

            notificationRepository.save(notification);

        } catch (Exception e) {
            System.err.println("Notification error: " + e.getMessage());
        }
    }
}