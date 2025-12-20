package com.scheduler.chatbot.model;

/**
 * Represents a subject/course to be scheduled
 * Contains subject properties including estimated hours, priority, and remaining hours
 */
public class Subject {
    private String name;
    private double estimatedHours;
    private Priority priority;
    private double remainingHours;

    /**
     * Default constructor
     */
    public Subject() {
    }

    /**
     * Constructor with all fields
     * @param name Subject name
     * @param estimatedHours Estimated hours needed
     * @param priority Priority level
     */
    public Subject(String name, double estimatedHours, Priority priority) {
        this.name = name;
        this.estimatedHours = Math.max(0.0, estimatedHours);
        this.priority = priority;
        this.remainingHours = this.estimatedHours;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = Math.max(0.0, estimatedHours);
        if (this.remainingHours > this.estimatedHours) {
            this.remainingHours = this.estimatedHours;
        }
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public double getRemainingHours() {
        return remainingHours;
    }

    public void setRemainingHours(double remainingHours) {
        this.remainingHours = Math.max(0.0, remainingHours);
    }

    /**
     * Calculate hours for the first half based on front-load ratio
     */
    public double getFirstHalfHours() {
        double ratio = (priority != null) ? priority.getFrontLoadRatio() : 0.5;
        double first = estimatedHours * ratio;
        return Math.round(first * 100.0) / 100.0;
    }

    /**
     * Calculate hours for the second half
     */
    public double getSecondHalfHours() {
        double second = estimatedHours - getFirstHalfHours();
        return Math.round(Math.max(0.0, second) * 100.0) / 100.0;
    }

    /**
     * Calculate number of blocks needed (90 min = 1.5 hours per block)
     */
    public int getTotalBlocks() {
        final double blockHours = 1.5;
        if (estimatedHours <= 0) return 0;
        return (int) Math.ceil(estimatedHours / blockHours);
    }

    @Override
    public String toString() {
        return "Subject{" +
                "name='" + name + '\'' +
                ", estimatedHours=" + estimatedHours +
                ", priority=" + priority +
                ", remainingHours=" + remainingHours +
                '}';
    }
}
