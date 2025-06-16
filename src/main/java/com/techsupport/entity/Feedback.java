package com.techsupport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * JPA entity representing client feedback on a completed ticket.
 * Feedback can only be submitted for closed tickets and includes rating and comments.
 */
@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rating", nullable = false)
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private Integer rating;

    @Column(name = "comments", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Comments must not exceed 2000 characters")
    private String comments;

    @Column(name = "is_satisfied", nullable = false)
    private boolean isSatisfied;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, unique = true)
    @NotNull(message = "Ticket is required")
    private Ticket ticket;

    // Constructors
    public Feedback() {}

    public Feedback(Ticket ticket, Integer rating) {
        this.ticket = ticket;
        this.rating = rating;
        this.isSatisfied = rating >= 4; // 4 or 5 star rating is considered satisfied
        this.createdAt = LocalDateTime.now();
    }

    public Feedback(Ticket ticket, Integer rating, String comments) {
        this(ticket, rating);
        this.comments = comments;
    }

    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        
        // Auto-set satisfaction based on rating
        if (this.rating != null) {
            this.isSatisfied = this.rating >= 4;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        
        // Auto-update satisfaction based on rating
        if (this.rating != null) {
            this.isSatisfied = this.rating >= 4;
        }
    }

    // Business methods
    public boolean isPositive() {
        return rating >= 4;
    }

    public boolean isNegative() {
        return rating <= 2;
    }

    public boolean isNeutral() {
        return rating == 3;
    }

    public String getRatingDescription() {
        return switch (rating) {
            case 1 -> "Very Poor";
            case 2 -> "Poor";
            case 3 -> "Average";
            case 4 -> "Good";
            case 5 -> "Excellent";
            default -> "Unknown";
        };
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
        if (rating != null) {
            this.isSatisfied = rating >= 4;
        }
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isSatisfied() {
        return isSatisfied;
    }

    public void setSatisfied(boolean satisfied) {
        isSatisfied = satisfied;
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

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", rating=" + rating +
                ", isSatisfied=" + isSatisfied +
                ", createdAt=" + createdAt +
                '}';
    }
} 