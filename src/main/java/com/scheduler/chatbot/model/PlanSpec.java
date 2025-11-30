package com.scheduler.chatbot.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Intermediate Representation (IR) for parsed DSL input
 * Represents the complete planning specification before scheduling
 */
public class PlanSpec {
    
    private String planName;
    private ZoneId timezone;
    private LocalDate startDate;
    private List<CourseSpec> courses;
    private Map<LocalDate, Double> availability; // date -> capacity hours
    private SchedulingRules rules;
    private SoftPreferences softPrefs;
    
    public PlanSpec() {
        this.courses = new ArrayList<>();
        this.availability = new HashMap<>();
        this.rules = new SchedulingRules();
        this.softPrefs = new SoftPreferences();
    }
    
    // TODO: Add getters/setters
    
    /**
     * Validate the plan specification
     * TODO: Implement semantic validation
     */
    public ValidationResult validate() {
        // TODO: Check for conflicts, missing data, invalid dates, etc.
        return new ValidationResult(true, new ArrayList<>());
    }
    
    /**
     * Course specification within a plan
     */
    public static class CourseSpec {
        private String id;
        private Priority priority;
        private double workloadHours;
        private LocalDate examDate;
        private List<ComponentSpec> components;
        
        public CourseSpec() {
            this.components = new ArrayList<>();
        }
        
        // TODO: Add getters/setters
    }
    
    /**
     * Component specification (assignment, project, etc.)
     */
    public static class ComponentSpec {
        private String name;
        private double estimatedHours;
        private LocalDate dueDate;
        
        // TODO: Add getters/setters
    }
    
    /**
     * Scheduling rules and constraints
     */
    public static class SchedulingRules {
        private double maxHoursPerDay = 8.0;
        private int maxContinuousBlockMinutes = 180; // 3 hours
        private int blockDurationMinutes = 90;
        private int breakDurationMinutes = 15;
        
        // TODO: Add getters/setters
    }
    
    /**
     * Soft preferences for optimization
     */
    public static class SoftPreferences {
        private boolean preferSpreadness = true;
        private boolean preferBuffer = true;
        private boolean preferInterleave = true;
        
        // TODO: Add getters/setters
    }
    
    /**
     * Validation result
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
    }
}
