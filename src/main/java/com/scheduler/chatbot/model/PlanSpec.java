package com.scheduler.chatbot.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Intermediate Representation (IR) for parsed DSL input
 * Represents the complete planning specification before scheduling
 */
public class PlanSpec {
    
    private String planName;
    private ZoneId timezone;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<CourseSpec> courses;
    private Map<LocalDate, Double> availability; // date -> capacity hours
    private SchedulingRules rules;
    private SoftPreferences softPrefs;
    
    public PlanSpec() {
        this.planName = "Untitled Plan";
        this.timezone = ZoneId.systemDefault();
        this.startDate = LocalDate.now();
        this.courses = new ArrayList<>();
        this.availability = new HashMap<>();
        this.rules = new SchedulingRules();
        this.softPrefs = new SoftPreferences();
    }
    
    public PlanSpec(String planName) {
        this();
        this.planName = planName;
    }
    
    // Getters and Setters
    
    public String getPlanName() {
        return planName;
    }
    
    public void setPlanName(String planName) {
        this.planName = planName;
    }
    
    public ZoneId getTimezone() {
        return timezone;
    }
    
    public void setTimezone(ZoneId timezone) {
        this.timezone = timezone;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public List<CourseSpec> getCourses() {
        return courses;
    }
    
    public void setCourses(List<CourseSpec> courses) {
        this.courses = courses;
    }
    
    public void addCourse(CourseSpec course) {
        this.courses.add(course);
    }
    
    public void removeCourse(String courseId) {
        this.courses.removeIf(c -> c.getId().equals(courseId));
    }
    
    public CourseSpec getCourse(String courseId) {
        return courses.stream()
                .filter(c -> c.getId().equals(courseId))
                .findFirst()
                .orElse(null);
    }
    
    public Map<LocalDate, Double> getAvailability() {
        return availability;
    }
    
    public void setAvailability(Map<LocalDate, Double> availability) {
        this.availability = availability;
    }
    
    public void setAvailability(LocalDate date, double hours) {
        this.availability.put(date, hours);
    }
    
    public Double getAvailability(LocalDate date) {
        return availability.getOrDefault(date, 0.0);
    }
    
    public SchedulingRules getRules() {
        return rules;
    }
    
    public void setRules(SchedulingRules rules) {
        this.rules = rules;
    }
    
    public SoftPreferences getSoftPrefs() {
        return softPrefs;
    }
    
    public void setSoftPrefs(SoftPreferences softPrefs) {
        this.softPrefs = softPrefs;
    }
    
    /**
     * Validate the plan specification
     * Performs comprehensive semantic validation
     */
    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();
        
        // 1. Validate plan name
        if (planName == null || planName.trim().isEmpty()) {
            errors.add("Plan name cannot be empty");
        }
        
        // 2. Validate courses
        if (courses == null || courses.isEmpty()) {
            errors.add("At least one course must be specified");
        } else {
            // Check for duplicate course IDs
            List<String> courseIds = courses.stream()
                    .map(CourseSpec::getId)
                    .collect(Collectors.toList());
            
            long uniqueIds = courseIds.stream().distinct().count();
            if (uniqueIds < courseIds.size()) {
                errors.add("Duplicate course IDs found");
            }
            
            // Validate each course
            for (int i = 0; i < courses.size(); i++) {
                CourseSpec course = courses.get(i);
                List<String> courseErrors = validateCourse(course);
                for (String error : courseErrors) {
                    errors.add("Course '" + course.getId() + "': " + error);
                }
            }
        }
        
        // 3. Validate availability
        if (availability == null || availability.isEmpty()) {
            errors.add("No availability specified");
        } else {
            // Check for negative hours
            for (Map.Entry<LocalDate, Double> entry : availability.entrySet()) {
                if (entry.getValue() < 0) {
                    errors.add("Negative availability hours on " + entry.getKey());
                }
                if (entry.getValue() > rules.getMaxHoursPerDay()) {
                    errors.add("Availability on " + entry.getKey() + 
                            " exceeds max hours per day (" + rules.getMaxHoursPerDay() + ")");
                }
            }
            
            // Determine date range from availability
            LocalDate minDate = availability.keySet().stream()
                    .min(LocalDate::compareTo)
                    .orElse(null);
            LocalDate maxDate = availability.keySet().stream()
                    .max(LocalDate::compareTo)
                    .orElse(null);
            
            if (minDate != null && maxDate != null) {
                if (startDate == null) {
                    startDate = minDate;
                }
                if (endDate == null) {
                    endDate = maxDate;
                }
            }
        }
        
        // 4. Validate date range
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                errors.add("Start date (" + startDate + ") is after end date (" + endDate + ")");
            }
        }
        
        // 5. Validate workload vs availability
        double totalWorkload = courses.stream()
                .mapToDouble(CourseSpec::getWorkloadHours)
                .sum();
        
        double totalAvailable = availability.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        
        if (totalWorkload > totalAvailable) {
            errors.add(String.format(
                    "Total workload (%.1f hours) exceeds total availability (%.1f hours). " +
                    "Shortfall: %.1f hours",
                    totalWorkload, totalAvailable, totalWorkload - totalAvailable
            ));
        }
        
        // 6. Validate scheduling rules
        if (rules.getMaxHoursPerDay() <= 0) {
            errors.add("Max hours per day must be positive");
        }
        
        if (rules.getBlockDurationMinutes() <= 0) {
            errors.add("Block duration must be positive");
        }
        
        if (rules.getBreakDurationMinutes() < 0) {
            errors.add("Break duration cannot be negative");
        }
        
        if (rules.getMaxContinuousBlockMinutes() < rules.getBlockDurationMinutes()) {
            errors.add("Max continuous block must be at least one block duration");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validate a single course specification
     */
    private List<String> validateCourse(CourseSpec course) {
        List<String> errors = new ArrayList<>();
        
        if (course.getId() == null || course.getId().trim().isEmpty()) {
            errors.add("Course ID cannot be empty");
        }
        
        if (course.getPriority() == null) {
            errors.add("Priority must be specified");
        }
        
        if (course.getWorkloadHours() <= 0) {
            errors.add("Workload hours must be positive");
        }
        
        // Validate components
        if (course.getComponents() != null && !course.getComponents().isEmpty()) {
            double componentHours = course.getComponents().stream()
                    .mapToDouble(ComponentSpec::getEstimatedHours)
                    .sum();
            
            // Component hours should not exceed total workload significantly
            if (componentHours > course.getWorkloadHours() * 1.1) {
                errors.add("Component hours exceed total workload");
            }
            
            // Validate each component
            for (ComponentSpec comp : course.getComponents()) {
                if (comp.getName() == null || comp.getName().trim().isEmpty()) {
                    errors.add("Component name cannot be empty");
                }
                if (comp.getEstimatedHours() <= 0) {
                    errors.add("Component '" + comp.getName() + "' hours must be positive");
                }
                if (comp.getDueDate() != null && endDate != null && comp.getDueDate().isAfter(endDate)) {
                    errors.add("Component '" + comp.getName() + "' due date is after plan end date");
                }
            }
        }
        
        // Validate exam date
        if (course.getExamDate() != null) {
            if (startDate != null && course.getExamDate().isBefore(startDate)) {
                errors.add("Exam date is before plan start date");
            }
            if (endDate != null && course.getExamDate().isAfter(endDate)) {
                errors.add("Exam date is after plan end date");
            }
        }
        
        return errors;
    }
    
    /**
     * Calculate total workload across all courses
     */
    public double getTotalWorkloadHours() {
        return courses.stream()
                .mapToDouble(CourseSpec::getWorkloadHours)
                .sum();
    }
    
    /**
     * Calculate total available hours
     */
    public double getTotalAvailableHours() {
        return availability.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }
    
    /**
     * Get all dates with availability in chronological order
     */
    public List<LocalDate> getAvailableDates() {
        return availability.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Check if there is capacity shortfall
     */
    public boolean hasShortfall() {
        return getTotalWorkloadHours() > getTotalAvailableHours();
    }
    
    /**
     * Get shortfall amount in hours
     */
    public double getShortfallHours() {
        double shortfall = getTotalWorkloadHours() - getTotalAvailableHours();
        return Math.max(0, shortfall);
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
        
        public CourseSpec(String id, Priority priority, double workloadHours) {
            this();
            this.id = id;
            this.priority = priority;
            this.workloadHours = workloadHours;
        }
        
        // Getters and Setters
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public Priority getPriority() {
            return priority;
        }
        
        public void setPriority(Priority priority) {
            this.priority = priority;
        }
        
        public double getWorkloadHours() {
            return workloadHours;
        }
        
        public void setWorkloadHours(double workloadHours) {
            this.workloadHours = workloadHours;
        }
        
        public LocalDate getExamDate() {
            return examDate;
        }
        
        public void setExamDate(LocalDate examDate) {
            this.examDate = examDate;
        }
        
        public List<ComponentSpec> getComponents() {
            return components;
        }
        
        public void setComponents(List<ComponentSpec> components) {
            this.components = components;
        }
        
        public void addComponent(ComponentSpec component) {
            this.components.add(component);
        }
        
        public void addComponent(String name, double hours, LocalDate dueDate) {
            this.components.add(new ComponentSpec(name, hours, dueDate));
        }
        
        /**
         * Get total hours from all components
         */
        public double getComponentTotalHours() {
            return components.stream()
                    .mapToDouble(ComponentSpec::getEstimatedHours)
                    .sum();
        }
    }
    
    /**
     * Component specification (assignment, project, etc.)
     */
    public static class ComponentSpec {
        private String name;
        private double estimatedHours;
        private LocalDate dueDate;
        
        public ComponentSpec() {
        }
        
        public ComponentSpec(String name, double estimatedHours, LocalDate dueDate) {
            this.name = name;
            this.estimatedHours = estimatedHours;
            this.dueDate = dueDate;
        }
        
        // Getters and Setters
        
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
            this.estimatedHours = estimatedHours;
        }
        
        public LocalDate getDueDate() {
            return dueDate;
        }
        
        public void setDueDate(LocalDate dueDate) {
            this.dueDate = dueDate;
        }
    }
    
    /**
     * Scheduling rules and constraints
     */
    public static class SchedulingRules {
        private double maxHoursPerDay = 8.0;
        private int maxContinuousBlockMinutes = 180; // 3 hours
        private int blockDurationMinutes = 90;
        private int breakDurationMinutes = 15;
        
        public SchedulingRules() {
        }
        
        public SchedulingRules(double maxHoursPerDay, int maxContinuousBlockMinutes,
                              int blockDurationMinutes, int breakDurationMinutes) {
            this.maxHoursPerDay = maxHoursPerDay;
            this.maxContinuousBlockMinutes = maxContinuousBlockMinutes;
            this.blockDurationMinutes = blockDurationMinutes;
            this.breakDurationMinutes = breakDurationMinutes;
        }
        
        // Getters and Setters
        
        public double getMaxHoursPerDay() {
            return maxHoursPerDay;
        }
        
        public void setMaxHoursPerDay(double maxHoursPerDay) {
            this.maxHoursPerDay = maxHoursPerDay;
        }
        
        public int getMaxContinuousBlockMinutes() {
            return maxContinuousBlockMinutes;
        }
        
        public void setMaxContinuousBlockMinutes(int maxContinuousBlockMinutes) {
            this.maxContinuousBlockMinutes = maxContinuousBlockMinutes;
        }
        
        public int getBlockDurationMinutes() {
            return blockDurationMinutes;
        }
        
        public void setBlockDurationMinutes(int blockDurationMinutes) {
            this.blockDurationMinutes = blockDurationMinutes;
        }
        
        public int getBreakDurationMinutes() {
            return breakDurationMinutes;
        }
        
        public void setBreakDurationMinutes(int breakDurationMinutes) {
            this.breakDurationMinutes = breakDurationMinutes;
        }
        
        /**
         * Get block duration in hours
         */
        public double getBlockDurationHours() {
            return blockDurationMinutes / 60.0;
        }
        
        /**
         * Get break duration in hours
         */
        public double getBreakDurationHours() {
            return breakDurationMinutes / 60.0;
        }
        
        /**
         * Calculate maximum blocks per day
         */
        public int getMaxBlocksPerDay() {
            double totalMinutesPerBlock = blockDurationMinutes + breakDurationMinutes;
            return (int) ((maxHoursPerDay * 60) / totalMinutesPerBlock);
        }
    }
    
    /**
     * Soft preferences for optimization
     */
    public static class SoftPreferences {
        private boolean preferSpreadness = true;
        private boolean preferBuffer = true;
        private boolean preferInterleave = true;
        private double spreadnessWeight = 1.0;
        private double bufferWeight = 1.0;
        private double interleaveWeight = 1.0;
        
        public SoftPreferences() {
        }
        
        public SoftPreferences(boolean preferSpreadness, boolean preferBuffer, boolean preferInterleave) {
            this.preferSpreadness = preferSpreadness;
            this.preferBuffer = preferBuffer;
            this.preferInterleave = preferInterleave;
        }
        
        // Getters and Setters
        
        public boolean isPreferSpreadness() {
            return preferSpreadness;
        }
        
        public void setPreferSpreadness(boolean preferSpreadness) {
            this.preferSpreadness = preferSpreadness;
        }
        
        public boolean isPreferBuffer() {
            return preferBuffer;
        }
        
        public void setPreferBuffer(boolean preferBuffer) {
            this.preferBuffer = preferBuffer;
        }
        
        public boolean isPreferInterleave() {
            return preferInterleave;
        }
        
        public void setPreferInterleave(boolean preferInterleave) {
            this.preferInterleave = preferInterleave;
        }
        
        public double getSpreadnessWeight() {
            return spreadnessWeight;
        }
        
        public void setSpreadnessWeight(double spreadnessWeight) {
            this.spreadnessWeight = spreadnessWeight;
        }
        
        public double getBufferWeight() {
            return bufferWeight;
        }
        
        public void setBufferWeight(double bufferWeight) {
            this.bufferWeight = bufferWeight;
        }
        
        public double getInterleaveWeight() {
            return interleaveWeight;
        }
        
        public void setInterleaveWeight(double interleaveWeight) {
            this.interleaveWeight = interleaveWeight;
        }
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
