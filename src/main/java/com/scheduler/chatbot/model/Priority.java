package com.scheduler.chatbot.model;

/**
 * Enum representing the priority levels for subjects
 * TODO: Implement priority weights and front-load ratios
 */
public enum Priority {
    LOW(1.0, 0.40),
    MEDIUM(1.2, 0.50),
    HIGH(1.5, 0.60);

    // TODO: Add weight field
    private final double weight;
    // TODO: Add frontLoadRatio field
    private final double frontLoadRatio;

    Priority(double weight, double frontLoadRatio) {
        this.weight = weight;
        this.frontLoadRatio = frontLoadRatio;
    }

    // TODO: Implement getWeight() method
    public double getWeight() {
        return weight;
    }

    // TODO: Implement getFrontLoadRatio() method
    public double getFrontLoadRatio() {
        return frontLoadRatio;
    }

    // TODO: Implement fromString() method
    public static Priority fromString(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Priority string cannot be null");
        }
        String normalized = s.trim().toUpperCase();
        switch (normalized) {
            case "HIGH":
                return HIGH;
            case "MEDIUM":
                return MEDIUM;
            case "LOW":
                return LOW;
            default:
                throw new IllegalArgumentException("Unknown priority: " + s);
        }
    }
}
