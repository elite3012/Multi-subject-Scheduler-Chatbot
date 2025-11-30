package com.scheduler.chatbot.model;

/**
 * Represents a subject/course to be scheduled
 * TODO: Implement subject properties and methods
 */
public class Subject {
    // TODO: Add fields (name, estimatedHours, priority, remainingHours)
    
    // TODO: Add constructors
    
    // TODO: Add getters/setters
    
    /**
     * Calculate hours for the first half based on front-load ratio
     * TODO: Implement this method
     */
    public double getFirstHalfHours() {
        // TODO: Implement front-load calculation
        return 0.0;
    }

    /**
     * Calculate hours for the second half
     * TODO: Implement this method
     */
    public double getSecondHalfHours() {
        // TODO: Implement second half calculation
        return 0.0;
    }

    /**
     * Calculate number of blocks needed (90 min = 1.5 hours per block)
     * TODO: Implement this method
     */
    public int getTotalBlocks() {
        // TODO: Implement block calculation
        return 0;
    }
}
