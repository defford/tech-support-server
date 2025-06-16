package com.techsupport.entity;

/**
 * Enumeration representing the status of an appointment.
 * Defines the states an appointment can be in throughout its lifecycle.
 */
public enum AppointmentStatus {
    SCHEDULED("Scheduled", "Appointment is scheduled and confirmed"),
    IN_PROGRESS("In Progress", "Appointment is currently taking place"),
    COMPLETED("Completed", "Appointment has been completed successfully"),
    CANCELLED("Cancelled", "Appointment has been cancelled"),
    NO_SHOW("No Show", "Client did not show up for the appointment");

    private final String displayName;
    private final String description;

    AppointmentStatus(String displayName, String description) {
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
     * Check if this status represents an active appointment.
     * @return true if appointment is active
     */
    public boolean isActive() {
        return this == SCHEDULED || this == IN_PROGRESS;
    }

    /**
     * Check if this status allows modification of the appointment.
     * @return true if appointment can be modified
     */
    public boolean canBeModified() {
        return this == SCHEDULED;
    }

    /**
     * Check if this status represents a completed appointment (successful or not).
     * @return true if appointment is finished
     */
    public boolean isFinished() {
        return this == COMPLETED || this == CANCELLED || this == NO_SHOW;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 