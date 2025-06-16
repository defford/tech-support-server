package com.techsupport.entity;

/**
 * Enumeration representing priority levels for tickets and service types.
 * Used to determine urgency and SLA requirements.
 */
public enum Priority {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4);

    private final String displayName;
    private final int level;

    Priority(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Get priority by level (1-4).
     * @param level The priority level
     * @return The corresponding Priority enum
     * @throws IllegalArgumentException if level is not valid
     */
    public static Priority fromLevel(int level) {
        for (Priority priority : values()) {
            if (priority.level == level) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Invalid priority level: " + level);
    }

    @Override
    public String toString() {
        return displayName;
    }
} 