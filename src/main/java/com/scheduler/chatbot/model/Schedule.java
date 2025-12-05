package com.scheduler.chatbot.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Intermediate Representation (IR) for scheduler output
 * Represents the complete generated schedule with blocks, metrics, and explanations
 */
public class Schedule {
    
    @JsonProperty("planName")
    private String planName;
    
    @JsonProperty("generatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;
    
    @JsonProperty("startDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonProperty("endDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @JsonProperty("blocks")
    private List<ScheduledBlock> blocks;
    
    @JsonProperty("score")
    private ScheduleScore score;
    
    @JsonProperty("explanations")
    private List<String> explanations;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    public Schedule() {
        this.generatedAt = LocalDateTime.now();
        this.blocks = new ArrayList<>();
        this.score = new ScheduleScore();
        this.explanations = new ArrayList<>();
        this.metadata = new HashMap<>();
    }
    
    public Schedule(String planName, LocalDate startDate, LocalDate endDate) {
        this();
        this.planName = planName;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters and Setters
    
    public String getPlanName() {
        return planName;
    }
    
    public void setPlanName(String planName) {
        this.planName = planName;
    }
    
    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
    
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
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
    
    public List<ScheduledBlock> getBlocks() {
        return blocks;
    }
    
    public void setBlocks(List<ScheduledBlock> blocks) {
        this.blocks = blocks;
    }
    
    public ScheduleScore getScore() {
        return score;
    }
    
    public void setScore(ScheduleScore score) {
        this.score = score;
    }
    
    public List<String> getExplanations() {
        return explanations;
    }
    
    public void setExplanations(List<String> explanations) {
        this.explanations = explanations;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    // Block Management Methods
    
    /**
     * Add a scheduled block to the schedule
     */
    public void addBlock(ScheduledBlock block) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null");
        }
        this.blocks.add(block);
        // Recalculate score after adding block
        recalculateScore();
    }
    
    /**
     * Remove a scheduled block from the schedule
     */
    public boolean removeBlock(ScheduledBlock block) {
        boolean removed = this.blocks.remove(block);
        if (removed) {
            // Recalculate score after removing block
            recalculateScore();
        }
        return removed;
    }
    
    /**
     * Remove blocks by course ID
     */
    public int removeBlocksByCourse(String courseId) {
        List<ScheduledBlock> toRemove = blocks.stream()
                .filter(b -> b.getCourseId().equals(courseId))
                .collect(Collectors.toList());
        
        blocks.removeAll(toRemove);
        
        if (!toRemove.isEmpty()) {
            recalculateScore();
        }
        
        return toRemove.size();
    }
    
    /**
     * Get blocks for a specific date
     */
    public List<ScheduledBlock> getBlocksForDate(LocalDate date) {
        return blocks.stream()
                .filter(b -> b.getDate().equals(date))
                .sorted((b1, b2) -> b1.getStartTime().compareTo(b2.getStartTime()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get blocks for a specific course
     */
    public List<ScheduledBlock> getBlocksForCourse(String courseId) {
        return blocks.stream()
                .filter(b -> b.getCourseId().equals(courseId))
                .sorted((b1, b2) -> b1.getDate().compareTo(b2.getDate()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get total scheduled hours
     */
    public double getTotalScheduledHours() {
        return blocks.stream()
                .mapToDouble(ScheduledBlock::getDurationHours)
                .sum();
    }
    
    /**
     * Get scheduled hours for a specific course
     */
    public double getScheduledHoursForCourse(String courseId) {
        return blocks.stream()
                .filter(b -> b.getCourseId().equals(courseId))
                .mapToDouble(ScheduledBlock::getDurationHours)
                .sum();
    }
    
    /**
     * Get unique dates with scheduled blocks
     */
    public List<LocalDate> getScheduledDates() {
        return blocks.stream()
                .map(ScheduledBlock::getDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Get all unique course IDs in schedule
     */
    public List<String> getCourseIds() {
        return blocks.stream()
                .map(ScheduledBlock::getCourseId)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Check if schedule is empty
     */
    public boolean isEmpty() {
        return blocks.isEmpty();
    }
    
    /**
     * Clear all blocks
     */
    public void clearBlocks() {
        this.blocks.clear();
        recalculateScore();
    }
    
    // Explanation Management
    
    /**
     * Add an explanation for scheduling decisions
     */
    public void addExplanation(String explanation) {
        if (explanation != null && !explanation.trim().isEmpty()) {
            this.explanations.add(explanation);
        }
    }
    
    /**
     * Add multiple explanations
     */
    public void addExplanations(List<String> explanations) {
        if (explanations != null) {
            this.explanations.addAll(explanations);
        }
    }
    
    /**
     * Clear all explanations
     */
    public void clearExplanations() {
        this.explanations.clear();
    }
    
    // Score Calculation Methods
    
    /**
     * Recalculate schedule score based on current blocks
     */
    public void recalculateScore() {
        if (blocks.isEmpty()) {
            this.score = new ScheduleScore();
            return;
        }
        
        ScheduleScore newScore = new ScheduleScore();
        
        // Calculate utilization
        double totalScheduled = getTotalScheduledHours();
        newScore.setTotalScheduledHours(totalScheduled);
        
        // Calculate course-specific metrics
        Map<String, Double> courseHours = new HashMap<>();
        for (String courseId : getCourseIds()) {
            double hours = getScheduledHoursForCourse(courseId);
            courseHours.put(courseId, hours);
        }
        newScore.setCourseHours(courseHours);
        
        // Calculate spreadness score (0-100)
        newScore.setSpreadnessScore(calculateSpreadnessScore());
        
        // Calculate buffer score (0-100)
        newScore.setBufferScore(calculateBufferScore());
        
        // Calculate interleave score (0-100)
        newScore.setInterleaveScore(calculateInterleaveScore());
        
        // Calculate overall score (weighted average)
        double overall = (newScore.getSpreadnessScore() + 
                         newScore.getBufferScore() + 
                         newScore.getInterleaveScore()) / 3.0;
        newScore.setOverallScore(overall);
        
        this.score = newScore;
    }
    
    /**
     * Calculate spreadness score: how evenly distributed blocks are across days
     */
    private double calculateSpreadnessScore() {
        if (blocks.isEmpty()) {
            return 0.0;
        }
        
        Map<LocalDate, Double> dailyHours = new HashMap<>();
        for (ScheduledBlock block : blocks) {
            dailyHours.merge(block.getDate(), block.getDurationHours(), Double::sum);
        }
        
        if (dailyHours.size() <= 1) {
            return 50.0; // Neutral score if all on one day
        }
        
        // Calculate standard deviation of daily hours
        double mean = dailyHours.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        double variance = dailyHours.values().stream()
                .mapToDouble(h -> Math.pow(h - mean, 2))
                .average()
                .orElse(0.0);
        
        double stdDev = Math.sqrt(variance);
        
        // Lower stdDev = more spread = higher score
        // Normalize to 0-100 scale (assuming max stdDev of 4 hours)
        double score = Math.max(0, 100 - (stdDev * 25));
        return Math.min(100, score);
    }
    
    /**
     * Calculate buffer score: how much time buffer exists before deadlines
     */
    private double calculateBufferScore() {
        if (blocks.isEmpty()) {
            return 0.0;
        }
        
        // Count blocks with buffer (not on the last possible day)
        long blocksWithBuffer = blocks.stream()
                .filter(b -> {
                    LocalDate deadline = b.getDeadline();
                    if (deadline == null) return true; // No deadline = always has buffer
                    return b.getDate().isBefore(deadline.minusDays(1));
                })
                .count();
        
        double percentage = (blocksWithBuffer * 100.0) / blocks.size();
        return percentage;
    }
    
    /**
     * Calculate interleave score: how well different courses are interleaved
     */
    private double calculateInterleaveScore() {
        if (blocks.size() <= 1 || getCourseIds().size() <= 1) {
            return 50.0; // Neutral if only one course
        }
        
        // Count course transitions (switching between courses on consecutive blocks)
        int transitions = 0;
        for (int i = 1; i < blocks.size(); i++) {
            if (!blocks.get(i).getCourseId().equals(blocks.get(i-1).getCourseId())) {
                transitions++;
            }
        }
        
        // More transitions = better interleaving
        // Max possible transitions = blocks.size() - 1
        double maxTransitions = blocks.size() - 1;
        double score = (transitions / maxTransitions) * 100;
        
        return Math.min(100, score);
    }
    
    /**
     * Calculate completion percentage for a course
     */
    public double getCompletionPercentage(String courseId, double totalWorkloadHours) {
        if (totalWorkloadHours <= 0) {
            return 0.0;
        }
        double scheduled = getScheduledHoursForCourse(courseId);
        return (scheduled / totalWorkloadHours) * 100;
    }
    
    // Metadata Management
    
    /**
     * Add metadata entry
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    /**
     * Get metadata entry
     */
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }
    
    // Utility Methods
    
    /**
     * Get summary statistics
     */
    public ScheduleSummary getSummary() {
        ScheduleSummary summary = new ScheduleSummary();
        summary.setTotalBlocks(blocks.size());
        summary.setTotalHours(getTotalScheduledHours());
        summary.setScheduledDays(getScheduledDates().size());
        summary.setCoursesCount(getCourseIds().size());
        summary.setAverageHoursPerDay(
            summary.getScheduledDays() > 0 ? 
            summary.getTotalHours() / summary.getScheduledDays() : 0
        );
        return summary;
    }
    
    /**
     * Validate schedule integrity
     */
    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();
        
        // Check for overlapping blocks on same day
        Map<LocalDate, List<ScheduledBlock>> blocksByDate = blocks.stream()
                .collect(Collectors.groupingBy(ScheduledBlock::getDate));
        
        for (Map.Entry<LocalDate, List<ScheduledBlock>> entry : blocksByDate.entrySet()) {
            List<ScheduledBlock> dayBlocks = entry.getValue();
            dayBlocks.sort((b1, b2) -> b1.getStartTime().compareTo(b2.getStartTime()));
            
            for (int i = 1; i < dayBlocks.size(); i++) {
                ScheduledBlock prev = dayBlocks.get(i-1);
                ScheduledBlock curr = dayBlocks.get(i);
                
                // Compare times as strings (format: "HH:mm")
                if (curr.getStartTime().compareTo(prev.getEndTime()) < 0) {
                    errors.add(String.format(
                        "Overlapping blocks on %s: %s (%s-%s) and %s (%s-%s)",
                        entry.getKey(),
                        prev.getCourseId(), prev.getStartTime(), prev.getEndTime(),
                        curr.getCourseId(), curr.getStartTime(), curr.getEndTime()
                    ));
                }
            }
        }
        
        // Check for blocks outside date range
        for (ScheduledBlock block : blocks) {
            if (startDate != null && block.getDate().isBefore(startDate)) {
                errors.add(String.format(
                    "Block on %s is before schedule start date %s",
                    block.getDate(), startDate
                ));
            }
            if (endDate != null && block.getDate().isAfter(endDate)) {
                errors.add(String.format(
                    "Block on %s is after schedule end date %s",
                    block.getDate(), endDate
                ));
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Inner class representing a scheduled study block
     */
    public static class ScheduledBlock {
        @JsonProperty("courseId")
        private String courseId;
        
        @JsonProperty("courseName")
        private String courseName;
        
        @JsonProperty("priority")
        private Priority priority;
        
        @JsonProperty("date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;
        
        @JsonProperty("startTime")
        @JsonFormat(pattern = "HH:mm")
        private String startTime;
        
        @JsonProperty("endTime")
        @JsonFormat(pattern = "HH:mm")
        private String endTime;
        
        @JsonProperty("durationMinutes")
        private int durationMinutes;
        
        @JsonProperty("componentName")
        private String componentName;
        
        @JsonProperty("deadline")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate deadline;
        
        @JsonProperty("reason")
        private String reason;
        
        public ScheduledBlock() {
        }
        
        public ScheduledBlock(String courseId, LocalDate date, String startTime, 
                            String endTime, int durationMinutes) {
            this.courseId = courseId;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationMinutes = durationMinutes;
        }
        
        // Getters and Setters
        
        public String getCourseId() {
            return courseId;
        }
        
        public void setCourseId(String courseId) {
            this.courseId = courseId;
        }
        
        public String getCourseName() {
            return courseName;
        }
        
        public void setCourseName(String courseName) {
            this.courseName = courseName;
        }
        
        public Priority getPriority() {
            return priority;
        }
        
        public void setPriority(Priority priority) {
            this.priority = priority;
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public void setDate(LocalDate date) {
            this.date = date;
        }
        
        public String getStartTime() {
            return startTime;
        }
        
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
        
        public String getEndTime() {
            return endTime;
        }
        
        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
        
        public int getDurationMinutes() {
            return durationMinutes;
        }
        
        public void setDurationMinutes(int durationMinutes) {
            this.durationMinutes = durationMinutes;
        }
        
        public double getDurationHours() {
            return durationMinutes / 60.0;
        }
        
        public String getComponentName() {
            return componentName;
        }
        
        public void setComponentName(String componentName) {
            this.componentName = componentName;
        }
        
        public LocalDate getDeadline() {
            return deadline;
        }
        
        public void setDeadline(LocalDate deadline) {
            this.deadline = deadline;
        }
        
        public String getReason() {
            return reason;
        }
        
        public void setReason(String reason) {
            this.reason = reason;
        }
        
        @Override
        public String toString() {
            return String.format("%s on %s %s-%s (%d min)",
                courseId, date, startTime, endTime, durationMinutes);
        }
    }
    
    /**
     * Inner class for schedule scoring metrics
     */
    public static class ScheduleScore {
        @JsonProperty("overallScore")
        private double overallScore = 0.0;
        
        @JsonProperty("spreadnessScore")
        private double spreadnessScore = 0.0;
        
        @JsonProperty("bufferScore")
        private double bufferScore = 0.0;
        
        @JsonProperty("interleaveScore")
        private double interleaveScore = 0.0;
        
        @JsonProperty("totalScheduledHours")
        private double totalScheduledHours = 0.0;
        
        @JsonProperty("courseHours")
        private Map<String, Double> courseHours = new HashMap<>();
        
        public ScheduleScore() {
        }
        
        // Getters and Setters
        
        public double getOverallScore() {
            return overallScore;
        }
        
        public void setOverallScore(double overallScore) {
            this.overallScore = overallScore;
        }
        
        public double getSpreadnessScore() {
            return spreadnessScore;
        }
        
        public void setSpreadnessScore(double spreadnessScore) {
            this.spreadnessScore = spreadnessScore;
        }
        
        public double getBufferScore() {
            return bufferScore;
        }
        
        public void setBufferScore(double bufferScore) {
            this.bufferScore = bufferScore;
        }
        
        public double getInterleaveScore() {
            return interleaveScore;
        }
        
        public void setInterleaveScore(double interleaveScore) {
            this.interleaveScore = interleaveScore;
        }
        
        public double getTotalScheduledHours() {
            return totalScheduledHours;
        }
        
        public void setTotalScheduledHours(double totalScheduledHours) {
            this.totalScheduledHours = totalScheduledHours;
        }
        
        public Map<String, Double> getCourseHours() {
            return courseHours;
        }
        
        public void setCourseHours(Map<String, Double> courseHours) {
            this.courseHours = courseHours;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Overall: %.1f, Spreadness: %.1f, Buffer: %.1f, Interleave: %.1f",
                overallScore, spreadnessScore, bufferScore, interleaveScore
            );
        }
    }
    
    /**
     * Schedule summary statistics
     */
    public static class ScheduleSummary {
        private int totalBlocks;
        private double totalHours;
        private int scheduledDays;
        private int coursesCount;
        private double averageHoursPerDay;
        
        public ScheduleSummary() {
        }
        
        // Getters and Setters
        
        public int getTotalBlocks() {
            return totalBlocks;
        }
        
        public void setTotalBlocks(int totalBlocks) {
            this.totalBlocks = totalBlocks;
        }
        
        public double getTotalHours() {
            return totalHours;
        }
        
        public void setTotalHours(double totalHours) {
            this.totalHours = totalHours;
        }
        
        public int getScheduledDays() {
            return scheduledDays;
        }
        
        public void setScheduledDays(int scheduledDays) {
            this.scheduledDays = scheduledDays;
        }
        
        public int getCoursesCount() {
            return coursesCount;
        }
        
        public void setCoursesCount(int coursesCount) {
            this.coursesCount = coursesCount;
        }
        
        public double getAverageHoursPerDay() {
            return averageHoursPerDay;
        }
        
        public void setAverageHoursPerDay(double averageHoursPerDay) {
            this.averageHoursPerDay = averageHoursPerDay;
        }
        
        @Override
        public String toString() {
            return String.format(
                "%d blocks, %.1f hours, %d days, %d courses, avg %.1f hours/day",
                totalBlocks, totalHours, scheduledDays, coursesCount, averageHoursPerDay
            );
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
