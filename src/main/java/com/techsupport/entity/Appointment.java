package com.techsupport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * JPA entity representing an appointment scheduled for a ticket.
 * Appointments link tickets to technicians for specific time slots.
 */
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scheduled_at", nullable = false)
    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes", nullable = false)
    @NotNull(message = "Duration is required")
    private Integer durationMinutes = 60; // Default 1 hour

    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Status is required")
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    @NotNull(message = "Ticket is required")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    @NotNull(message = "Technician is required")
    private Technician technician;

    // Constructors
    public Appointment() {}

    public Appointment(Ticket ticket, Technician technician, LocalDateTime scheduledAt) {
        this.ticket = ticket;
        this.technician = technician;
        this.scheduledAt = scheduledAt;
        this.createdAt = LocalDateTime.now();
    }

    public Appointment(Ticket ticket, Technician technician, LocalDateTime scheduledAt, Integer durationMinutes) {
        this(ticket, technician, scheduledAt);
        this.durationMinutes = durationMinutes;
    }

    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        
        // Set completed timestamp when status changes to COMPLETED
        if (this.status == AppointmentStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    // Business methods
    public LocalDateTime getEndTime() {
        return scheduledAt.plusMinutes(durationMinutes);
    }

    public boolean overlaps(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime appointmentEnd = getEndTime();
        return scheduledAt.isBefore(endTime) && appointmentEnd.isAfter(startTime);
    }

    public boolean isInPast() {
        return scheduledAt.isBefore(LocalDateTime.now());
    }

    public boolean canBeModified() {
        return status == AppointmentStatus.SCHEDULED && !isInPast();
    }

    public void complete() {
        this.status = AppointmentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Technician getTechnician() {
        return technician;
    }

    public void setTechnician(Technician technician) {
        this.technician = technician;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", scheduledAt=" + scheduledAt +
                ", durationMinutes=" + durationMinutes +
                ", status=" + status +
                '}';
    }
} 