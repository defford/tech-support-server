package com.techsupport.entity;

/**
 * Enumeration representing the types of actions that can be recorded in ticket history.
 * Used to categorize and filter history entries.
 */
public enum HistoryActionType {
    CREATED("Created", "Ticket was initially created"),
    STATUS_CHANGED("Status Changed", "Ticket status was modified"),
    PRIORITY_CHANGED("Priority Changed", "Ticket priority was modified"),
    ASSIGNED("Assigned", "Ticket was assigned to a technician"),
    UNASSIGNED("Unassigned", "Ticket was unassigned from a technician"),
    UPDATED("Updated", "Ticket details were updated"),
    COMMENTED("Commented", "A comment was added to the ticket"),
    APPOINTMENT_SCHEDULED("Appointment Scheduled", "An appointment was scheduled for the ticket"),
    APPOINTMENT_CANCELLED("Appointment Cancelled", "An appointment was cancelled"),
    APPOINTMENT_COMPLETED("Appointment Completed", "An appointment was completed"),
    FEEDBACK_SUBMITTED("Feedback Submitted", "Client feedback was submitted"),
    RESOLVED("Resolved", "Ticket was marked as resolved"),
    CLOSED("Closed", "Ticket was closed"),
    REOPENED("Reopened", "Ticket was reopened after being closed");

    private final String displayName;
    private final String description;

    HistoryActionType(String displayName, String description) {
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
     * Check if this action type represents a status-related change.
     * @return true if the action affects ticket status
     */
    public boolean isStatusRelated() {
        return this == STATUS_CHANGED || this == RESOLVED || this == CLOSED || this == REOPENED;
    }

    /**
     * Check if this action type represents an assignment-related change.
     * @return true if the action affects ticket assignment
     */
    public boolean isAssignmentRelated() {
        return this == ASSIGNED || this == UNASSIGNED;
    }

    /**
     * Check if this action type represents an appointment-related change.
     * @return true if the action affects appointments
     */
    public boolean isAppointmentRelated() {
        return this == APPOINTMENT_SCHEDULED || this == APPOINTMENT_CANCELLED || this == APPOINTMENT_COMPLETED;
    }

    /**
     * Check if this action type represents a system-generated event.
     * @return true if the action is typically system-generated
     */
    public boolean isSystemGenerated() {
        return this == CREATED || this == RESOLVED || this == CLOSED;
    }

    /**
     * Check if this action type represents a user-initiated event.
     * @return true if the action is typically user-initiated
     */
    public boolean isUserInitiated() {
        return this == COMMENTED || this == UPDATED || this == PRIORITY_CHANGED;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 