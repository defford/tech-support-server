package com.techsupport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a type of service offered by the tech support system.
 * Service types define categories of support (e.g., "Hardware Issue", "Software Bug")
 * and include SLA information for calculating due dates.
 */
@Entity
@Table(name = "service_types")
public class ServiceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    @NotBlank(message = "Service type name is required")
    @Size(max = 100, message = "Service type name must not exceed 100 characters")
    private String name;

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Column(name = "sla_hours", nullable = false)
    @NotNull(message = "SLA hours is required")
    @Positive(message = "SLA hours must be positive")
    private Integer slaHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @NotNull(message = "Priority is required")
    private Priority priority = Priority.MEDIUM;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "serviceType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "serviceType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TechnicianSkill> technicianSkills = new ArrayList<>();

    // Constructors
    public ServiceType() {}
    public ServiceType(String name, Integer slaHours, Priority priority) {
        this.name = name;
        this.slaHours = slaHours;
        this.priority = priority;
    }

    public ServiceType(String name, String description, Integer slaHours, Priority priority) {
        this(name, slaHours, priority);
        this.description = description;
    }

    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSlaHours() {
        return slaHours;
    }

    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<TechnicianSkill> getTechnicianSkills() {
        return technicianSkills;
    }

    public void setTechnicianSkills(List<TechnicianSkill> technicianSkills) {
        this.technicianSkills = technicianSkills;
    }

    // Helper methods
    public int getActiveTicketCount() {
        return (int) tickets.stream()
                .filter(ticket -> ticket.getStatus() != TicketStatus.CLOSED)
                .count();
    }

    @Override
    public String toString() {
        return "ServiceType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", slaHours=" + slaHours +
                ", priority=" + priority +
                ", isActive=" + isActive +
                '}';
    }
} 