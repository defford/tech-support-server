package com.techsupport.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * JPA entity representing the history of changes made to a ticket.
 * Every significant change to a ticket should be logged as a history entry.
 */
@Entity
@Table(name = "ticket_history")
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    @NotNull(message = "Action type is required")
    private HistoryActionType actionType;

    @Column(name = "description", nullable = false, length = 500)
    @NotBlank(message = "Description is required")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Column(name = "old_value", length = 1000)
    @Size(max = 1000, message = "Old value must not exceed 1000 characters")
    private String oldValue;

    @Column(name = "new_value", length = 1000)
    @Size(max = 1000, message = "New value must not exceed 1000 characters")
    private String newValue;

    @Column(name = "changed_by", length = 100)
    @Size(max = 100, message = "Changed by must not exceed 100 characters")
    private String changedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    @NotNull(message = "Ticket is required")
    private Ticket ticket;

    // Constructors
    public TicketHistory() {}

    public TicketHistory(Ticket ticket, HistoryActionType actionType, String description) {
        this.ticket = ticket;
        this.actionType = actionType;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public TicketHistory(Ticket ticket, HistoryActionType actionType, String description, 
                        String oldValue, String newValue, String changedBy) {
        this(ticket, actionType, description);
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
    }

    // JPA lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Business methods
    public boolean isStatusChange() {
        return actionType == HistoryActionType.STATUS_CHANGED;
    }

    public boolean isAssignmentChange() {
        return actionType == HistoryActionType.ASSIGNED || actionType == HistoryActionType.UNASSIGNED;
    }

    public boolean isPriorityChange() {
        return actionType == HistoryActionType.PRIORITY_CHANGED;
    }

    public String getFormattedDescription() {
        if (oldValue != null && newValue != null) {
            return description + " (from '" + oldValue + "' to '" + newValue + "')";
        }
        return description;
    }

    // Static factory methods for common history entries
    public static TicketHistory created(Ticket ticket, String createdBy) {
        return new TicketHistory(ticket, HistoryActionType.CREATED, 
                                "Ticket created", null, null, createdBy);
    }

    public static TicketHistory statusChanged(Ticket ticket, TicketStatus oldStatus, 
                                            TicketStatus newStatus, String changedBy) {
        return new TicketHistory(ticket, HistoryActionType.STATUS_CHANGED, 
                                "Status changed", oldStatus.getDisplayName(), 
                                newStatus.getDisplayName(), changedBy);
    }

    public static TicketHistory assigned(Ticket ticket, Technician technician, String changedBy) {
        return new TicketHistory(ticket, HistoryActionType.ASSIGNED, 
                                "Ticket assigned", null, technician.getFullName(), changedBy);
    }

    public static TicketHistory priorityChanged(Ticket ticket, Priority oldPriority, 
                                              Priority newPriority, String changedBy) {
        return new TicketHistory(ticket, HistoryActionType.PRIORITY_CHANGED, 
                                "Priority changed", oldPriority.getDisplayName(), 
                                newPriority.getDisplayName(), changedBy);
    }

    public static TicketHistory commented(Ticket ticket, String comment, String commentedBy) {
        return new TicketHistory(ticket, HistoryActionType.COMMENTED, 
                                "Comment added: " + comment, null, null, commentedBy);
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HistoryActionType getActionType() {
        return actionType;
    }

    public void setActionType(HistoryActionType actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    @Override
    public String toString() {
        return "TicketHistory{" +
                "id=" + id +
                ", actionType=" + actionType +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", changedBy='" + changedBy + '\'' +
                '}';
    }
} 