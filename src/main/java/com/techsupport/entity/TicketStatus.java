package com.techsupport.entity;

/**
 * Enumeration representing the status of a support ticket.
 * Defines the workflow states that a ticket goes through.
 */
public enum TicketStatus {
    OPEN("Open", "Ticket has been created and is awaiting assignment"),
    ASSIGNED("Assigned", "Ticket has been assigned to a technician"),
    IN_PROGRESS("In Progress", "Technician is actively working on the ticket"),
    PENDING_CLIENT("Pending Client", "Waiting for client response or action"),
    RESOLVED("Resolved", "Issue has been resolved, awaiting client confirmation"),
    CLOSED("Closed", "Ticket has been completed and closed");

    private final String displayName;
    private final String description;

    TicketStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this status represents an open/active ticket.
     * @return true if ticket is still open/active
     */
    public boolean isOpen() {
        return this != CLOSED;
    }

    /**
     * Check if this status allows assignment to a technician.
     * @return true if ticket can be assigned
     */
    public boolean canBeAssigned() {
        return this == OPEN;
    }

    /**
     * Check if this status allows closing the ticket.
     * @return true if ticket can be closed
     */
    public boolean canBeClosed() {
        return this == RESOLVED || this == PENDING_CLIENT;
    }

    /**
     * Get the next logical status in the workflow.
     * @return the next status, or null if no standard next status
     */
    public TicketStatus getNextStatus() {
        return switch (this) {
            case OPEN -> ASSIGNED;
            case IN_PROGRESS -> PENDING_CLIENT;
            case PENDING_CLIENT -> RESOLVED;
            case RESOLVED -> CLOSED;
            default -> null;
        };
    }

    @Override
    public String toString() {
        return displayName;
    }
} 