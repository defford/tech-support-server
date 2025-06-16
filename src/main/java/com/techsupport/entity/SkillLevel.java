package com.techsupport.entity;

/**
 * Enumeration representing the skill level of a technician in a particular service type.
 * Used to determine assignment priority and capability.
 */
public enum SkillLevel {
    BEGINNER("Beginner", 1, "Basic understanding, requires supervision"),
    INTERMEDIATE("Intermediate", 2, "Good understanding, can work independently"),
    ADVANCED("Advanced", 3, "Strong expertise, can handle complex issues"),
    EXPERT("Expert", 4, "Deep expertise, can handle any issue and mentor others");

    private final String displayName;
    private final int level;
    private final String description;

    SkillLevel(String displayName, int level, String description) {
        this.displayName = displayName;
        this.level = level;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get skill level by numeric level (1-4).
     * @param level The skill level number
     * @return The corresponding SkillLevel enum
     * @throws IllegalArgumentException if level is not valid
     */
    public static SkillLevel fromLevel(int level) {
        for (SkillLevel skillLevel : values()) {
            if (skillLevel.level == level) {
                return skillLevel;
            }
        }
        throw new IllegalArgumentException("Invalid skill level: " + level);
    }

    /**
     * Check if this skill level is considered experienced.
     * @return true if level is Advanced or Expert
     */
    public boolean isExperienced() {
        return this == ADVANCED || this == EXPERT;
    }

    /**
     * Check if this skill level can handle critical issues.
     * @return true if level is Expert
     */
    public boolean canHandleCritical() {
        return this == EXPERT;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 