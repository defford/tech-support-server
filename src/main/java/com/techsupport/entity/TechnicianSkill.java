package com.techsupport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * JPA entity representing a technician's skill in a particular service type.
 * This is a junction entity that links technicians to the service types they can handle,
 * with additional metadata about their expertise level.
 */
@Entity
@Table(name = "technician_skills", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"technician_id", "service_type_id"}))
public class TechnicianSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", nullable = false, length = 20)
    @NotNull(message = "Skill level is required")
    private SkillLevel skillLevel = SkillLevel.INTERMEDIATE;

    @Column(name = "years_experience")
    private Integer yearsExperience;

    @Column(name = "is_primary_skill", nullable = false)
    private boolean isPrimarySkill = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    @NotNull(message = "Technician is required")
    private Technician technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id", nullable = false)
    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    // Constructors
    public TechnicianSkill() {}

    public TechnicianSkill(Technician technician, ServiceType serviceType, SkillLevel skillLevel) {
        this.technician = technician;
        this.serviceType = serviceType;
        this.skillLevel = skillLevel;
        this.createdAt = LocalDateTime.now();
    }

    public TechnicianSkill(Technician technician, ServiceType serviceType, SkillLevel skillLevel, 
                          Integer yearsExperience, boolean isPrimarySkill) {
        this(technician, serviceType, skillLevel);
        this.yearsExperience = yearsExperience;
        this.isPrimarySkill = isPrimarySkill;
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
    }

    // Business methods
    public boolean isExperienced() {
        return skillLevel == SkillLevel.EXPERT || 
               (yearsExperience != null && yearsExperience >= 3);
    }

    public boolean canHandleCriticalTickets() {
        return skillLevel == SkillLevel.EXPERT || isPrimarySkill;
    }

    public int getSkillScore() {
        int score = skillLevel.getLevel() * 10;
        if (yearsExperience != null) {
            score += Math.min(yearsExperience, 10); // Cap at 10 years for scoring
        }
        if (isPrimarySkill) {
            score += 5;
        }
        return score;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SkillLevel getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(SkillLevel skillLevel) {
        this.skillLevel = skillLevel;
    }

    public Integer getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(Integer yearsExperience) {
        this.yearsExperience = yearsExperience;
    }

    public boolean isPrimarySkill() {
        return isPrimarySkill;
    }

    public void setPrimarySkill(boolean primarySkill) {
        isPrimarySkill = primarySkill;
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

    public Technician getTechnician() {
        return technician;
    }

    public void setTechnician(Technician technician) {
        this.technician = technician;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public String toString() {
        return "TechnicianSkill{" +
                "id=" + id +
                ", skillLevel=" + skillLevel +
                ", yearsExperience=" + yearsExperience +
                ", isPrimarySkill=" + isPrimarySkill +
                '}';
    }
} 