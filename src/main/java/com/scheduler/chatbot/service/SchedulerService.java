package com.scheduler.chatbot.service;

import com.scheduler.chatbot.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating schedules based on the priority-based front-loading algorithm
 * Input: PlanSpec (IR) - Validated planning specification
 * Output: Schedule (IR) - Generated schedule with blocks and explanations
 */
@Service
public class SchedulerService {
    
    // Constants
    private static final double BLOCK_DURATION = 2.0; // 2 hours per block
    private static final double MAX_HOURS_PER_DAY = 8.0;
    private static final double MAX_CONTINUOUS_HOURS = 4.0;
    private static final double BREAK_DURATION = 0.25; // 15 minutes
    private static final LocalTime DEFAULT_START_TIME = LocalTime.of(8, 0);
    
    /**
     * Generate schedule from PlanSpec using priority-based front-loading algorithm
     * Algorithm steps:
     * 1. Validate PlanSpec
     * 2. Sort courses by priority (HIGH -> MEDIUM -> LOW)
     * 3. For each course, calculate front-load distribution (first half vs second half)
     * 4. Split calendar into first/second half
     * 5. Schedule blocks with priority-based allocation
     * 6. Generate explanations for each placement decision
     * 7. Calculate schedule score and metadata
     */
    public Schedule generateSchedule(PlanSpec planSpec) {
        // Validate input
        PlanSpec.ValidationResult validation = planSpec.validate();
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Invalid PlanSpec: " + validation.getErrors());
        }
        
        // Initialize schedule
        Schedule schedule = new Schedule(planSpec.getPlanName(), 
                                        planSpec.getStartDate(), 
                                        planSpec.getEndDate());
        
        // Sort courses by priority (HIGH -> MEDIUM -> LOW)
        List<PlanSpec.CourseSpec> sortedCourses = planSpec.getCourses().stream()
                .sorted(Comparator.comparing(PlanSpec.CourseSpec::getPriority).reversed())
                .collect(Collectors.toList());
        
        schedule.addExplanation("═══ SCHEDULING ALGORITHM START ═══");
        schedule.addExplanation("Total courses: " + sortedCourses.size());
        schedule.addExplanation("Priority order: " + 
                sortedCourses.stream()
                    .map(c -> c.getId() + " (" + c.getPriority() + ")")
                    .collect(Collectors.joining(", ")));
        
        // Calculate calendar split point (midpoint between start and end)
        LocalDate startDate = planSpec.getStartDate();
        LocalDate endDate = planSpec.getEndDate() != null ? planSpec.getEndDate() : 
                            startDate.plusDays(planSpec.getAvailability().size() - 1);
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate splitDate = startDate.plusDays(totalDays / 2);
        
        schedule.addExplanation("");
        schedule.addExplanation("═══ CALENDAR SPLIT ═══");
        schedule.addExplanation("Start date: " + startDate);
        schedule.addExplanation("End date: " + endDate);
        schedule.addExplanation("Split date: " + splitDate);
        schedule.addExplanation("First half: " + startDate + " to " + splitDate.minusDays(1));
        schedule.addExplanation("Second half: " + splitDate + " to " + endDate);
        
        // Track remaining hours per course
        Map<String, Double> remainingHours = new HashMap<>();
        Map<String, Double> firstHalfHours = new HashMap<>();
        Map<String, Double> secondHalfHours = new HashMap<>();
        
        for (PlanSpec.CourseSpec course : sortedCourses) {
            double totalHours = course.getWorkloadHours();
            remainingHours.put(course.getId(), totalHours);
            
            // Calculate front-load distribution based on priority
            Priority priority = course.getPriority();
            double frontLoadRatio = priority.getFrontLoadRatio();
            double firstHalf = totalHours * frontLoadRatio;
            double secondHalf = totalHours * (1.0 - frontLoadRatio);
            
            firstHalfHours.put(course.getId(), firstHalf);
            secondHalfHours.put(course.getId(), secondHalf);
            
            schedule.addExplanation("");
            schedule.addExplanation("Course: " + course.getId());
            schedule.addExplanation("  Priority: " + priority + " (weight: " + priority.getWeight() + ")");
            schedule.addExplanation("  Total hours: " + totalHours);
            schedule.addExplanation("  First half allocation: " + String.format("%.1f", firstHalf) + 
                                  " hours (" + (int)(frontLoadRatio * 100) + "%)");
            schedule.addExplanation("  Second half allocation: " + String.format("%.1f", secondHalf) + 
                                  " hours (" + (int)((1.0 - frontLoadRatio) * 100) + "%)" );
        }
        
        // Track daily usage
        Map<LocalDate, Double> dailyUsage = new HashMap<>();
        Map<LocalDate, LocalTime> dailyNextStart = new HashMap<>();
        
        schedule.addExplanation("");
        schedule.addExplanation("═══ BLOCK ALLOCATION - FIRST HALF ═══");
        
        // PHASE 1: Schedule first half (high priority courses get more blocks here)
        for (PlanSpec.CourseSpec course : sortedCourses) {
            double hoursToSchedule = firstHalfHours.get(course.getId());
            schedulePhase(schedule, planSpec, course, startDate, splitDate.minusDays(1), 
                         hoursToSchedule, remainingHours, dailyUsage, dailyNextStart, "FIRST HALF");
        }
        
        schedule.addExplanation("");
        schedule.addExplanation("═══ BLOCK ALLOCATION - SECOND HALF ═══");
        
        // PHASE 2: Schedule second half
        for (PlanSpec.CourseSpec course : sortedCourses) {
            double hoursToSchedule = secondHalfHours.get(course.getId());
            schedulePhase(schedule, planSpec, course, splitDate, endDate, 
                         hoursToSchedule, remainingHours, dailyUsage, dailyNextStart, "SECOND HALF");
        }
        
        // Handle any remaining unscheduled hours (shortfall)
        handleShortfall(schedule, remainingHours, sortedCourses);
        
        // Calculate final score
        calculateScore(schedule, planSpec, remainingHours);
        
        schedule.addExplanation("");
        schedule.addExplanation("═══ SCHEDULING COMPLETE ═══");
        
        return schedule;
    }
    
    /**
     * Schedule blocks for a course in a specific phase (first half or second half)
     */
    private void schedulePhase(Schedule schedule, PlanSpec planSpec, 
                              PlanSpec.CourseSpec course,
                              LocalDate phaseStart, LocalDate phaseEnd,
                              double hoursToSchedule,
                              Map<String, Double> remainingHours,
                              Map<LocalDate, Double> dailyUsage,
                              Map<LocalDate, LocalTime> dailyNextStart,
                              String phaseName) {
        
        if (hoursToSchedule <= 0) {
            return;
        }
        
        schedule.addExplanation("");
        schedule.addExplanation("Scheduling " + course.getId() + " in " + phaseName + ": " + 
                              String.format("%.1f", hoursToSchedule) + " hours");
        
        int blocksNeeded = calculateBlocksNeeded(hoursToSchedule);
        int blocksScheduled = 0;
        double hoursScheduled = 0.0;
        
        // Try to schedule blocks in available days
        LocalDate currentDate = phaseStart;
        while (currentDate.isBefore(phaseEnd.plusDays(1)) && blocksScheduled < blocksNeeded) {
            
            // Check if day has availability
            double dayCapacity = planSpec.getAvailability(currentDate);
            if (dayCapacity <= 0) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            double dayUsed = dailyUsage.getOrDefault(currentDate, 0.0);
            double dayRemaining = Math.min(dayCapacity, MAX_HOURS_PER_DAY) - dayUsed;
            
            // Check if we can fit a block
            if (dayRemaining >= BLOCK_DURATION) {
                // Find best time slot for this block
                LocalTime startTime = findBestTimeSlot(currentDate, dailyNextStart, dayUsed);
                LocalTime endTime = startTime.plusHours((long)BLOCK_DURATION);
                
                // Create block
                Schedule.ScheduledBlock block = new Schedule.ScheduledBlock();
                block.setCourseId(course.getId());
                block.setCourseName(course.getId());
                block.setDate(currentDate);
                block.setStartTime(startTime.toString());
                block.setEndTime(endTime.toString());
                block.setDurationMinutes((int)(BLOCK_DURATION * 60));
                block.setPriority(course.getPriority());
                block.setReason(String.format(
                    "Allocated %s block %d/%d for %s (Priority: %s) at %s %s-%s",
                    phaseName, blocksScheduled + 1, blocksNeeded, 
                    course.getId(), course.getPriority(),
                    currentDate, startTime, endTime
                ));
                
                schedule.addBlock(block);
                
                // Update tracking
                blocksScheduled++;
                hoursScheduled += BLOCK_DURATION;
                dailyUsage.put(currentDate, dayUsed + BLOCK_DURATION);
                dailyNextStart.put(currentDate, endTime.plusMinutes((long)(BREAK_DURATION * 60)));
                
                schedule.addExplanation("  ✓ Block " + blocksScheduled + ": " + currentDate + 
                                      " " + startTime + "-" + endTime + 
                                      " (remaining capacity: " + String.format("%.1f", dayRemaining - BLOCK_DURATION) + "h)");
                
                // Check if we can fit another block on the same day
                dayRemaining -= BLOCK_DURATION;
                if (dayRemaining < BLOCK_DURATION) {
                    currentDate = currentDate.plusDays(1);
                }
            } else {
                currentDate = currentDate.plusDays(1);
            }
        }
        
        // Update remaining hours
        double remaining = remainingHours.get(course.getId()) - hoursScheduled;
        remainingHours.put(course.getId(), Math.max(0, remaining));
        
        if (blocksScheduled < blocksNeeded) {
            schedule.addExplanation("  ⚠ Only scheduled " + blocksScheduled + "/" + blocksNeeded + 
                                  " blocks (" + String.format("%.1f", hoursScheduled) + "/" + 
                                  String.format("%.1f", hoursToSchedule) + " hours) - insufficient capacity");
        }
    }
    
    /**
     * Calculate number of blocks needed for given hours
     */
    private int calculateBlocksNeeded(double hours) {
        return (int) Math.ceil(hours / BLOCK_DURATION);
    }
    
    /**
     * Find best time slot for a block on a given day
     */
    private LocalTime findBestTimeSlot(LocalDate date, 
                                      Map<LocalDate, LocalTime> dailyNextStart,
                                      double dayUsed) {
        // If this is the first block of the day, start at default time
        if (!dailyNextStart.containsKey(date) || dayUsed == 0) {
            return DEFAULT_START_TIME;
        }
        
        // Otherwise, start at the next available slot (after previous block + break)
        return dailyNextStart.get(date);
    }
    
    /**
     * Handle courses with remaining unscheduled hours (shortfall)
     */
    private void handleShortfall(Schedule schedule, 
                                Map<String, Double> remainingHours,
                                List<PlanSpec.CourseSpec> courses) {
        
        boolean hasShortfall = remainingHours.values().stream().anyMatch(h -> h > 0.1);
        
        if (hasShortfall) {
            schedule.addExplanation("");
            schedule.addExplanation("═══ SHORTFALL ANALYSIS ═══");
            schedule.addExplanation("⚠ Insufficient capacity to schedule all hours");
            
            for (PlanSpec.CourseSpec course : courses) {
                double remaining = remainingHours.get(course.getId());
                if (remaining > 0.1) {
                    schedule.addExplanation("  • " + course.getId() + ": " + 
                                          String.format("%.1f", remaining) + 
                                          " hours unscheduled");
                }
            }
            
            schedule.addExplanation("");
            schedule.addExplanation("SUGGESTIONS:");
            schedule.addExplanation("  1. Add more available days with 'set availability' command");
            schedule.addExplanation("  2. Increase capacity on existing days");
            schedule.addExplanation("  3. Reduce estimated hours for some subjects");
            schedule.addExplanation("  4. Extend the study period");
        }
    }
    
    /**
     * Calculate final schedule score
     */
    private void calculateScore(Schedule schedule, PlanSpec planSpec, 
                               Map<String, Double> remainingHours) {
        
        Schedule.ScheduleScore score = schedule.getScore();
        
        // Total scheduled hours
        double totalHours = schedule.getBlocks().stream()
                .mapToDouble(Schedule.ScheduledBlock::getDurationHours)
                .sum();
        score.setTotalScheduledHours(totalHours);
        
        // Calculate utilization (spreadness score)
        double totalAvailableHours = planSpec.getAvailability().values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        double utilizationRate = totalAvailableHours > 0 ? totalHours / totalAvailableHours : 0.0;
        score.setSpreadnessScore(utilizationRate * 100.0);
        
        // Calculate completion rate (buffer score)
        double totalRequiredHours = planSpec.getCourses().stream()
                .mapToDouble(PlanSpec.CourseSpec::getWorkloadHours)
                .sum();
        double unscheduledHours = remainingHours.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        double completionRate = totalRequiredHours > 0 ? 
                (totalRequiredHours - unscheduledHours) / totalRequiredHours : 1.0;
        score.setBufferScore(completionRate * 100.0);
        
        // Interleave score - calculate based on priority distribution
        Map<Priority, Long> distribution = schedule.getBlocks().stream()
                .collect(Collectors.groupingBy(Schedule.ScheduledBlock::getPriority, 
                                              Collectors.counting()));
        long highCount = distribution.getOrDefault(Priority.HIGH, 0L);
        long mediumCount = distribution.getOrDefault(Priority.MEDIUM, 0L);
        long lowCount = distribution.getOrDefault(Priority.LOW, 0L);
        double totalBlocks = highCount + mediumCount + lowCount;
        double interleaveScore = totalBlocks > 0 ? 
                ((highCount * 1.5 + mediumCount * 1.2 + lowCount) / (totalBlocks * 1.5)) * 100.0 : 0.0;
        score.setInterleaveScore(interleaveScore);
        
        // Calculate overall score (average of three scores)
        double overallScore = (score.getSpreadnessScore() + score.getBufferScore() + score.getInterleaveScore()) / 3.0;
        score.setOverallScore(overallScore);
        
        // Build course hours map
        Map<String, Double> courseHours = new HashMap<>();
        for (PlanSpec.CourseSpec course : planSpec.getCourses()) {
            double scheduled = schedule.getBlocks().stream()
                    .filter(b -> b.getCourseId().equals(course.getId()))
                    .mapToDouble(Schedule.ScheduledBlock::getDurationHours)
                    .sum();
            courseHours.put(course.getId(), scheduled);
        }
        score.setCourseHours(courseHours);
        
        // Set metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("totalCourses", planSpec.getCourses().size());
        metadata.put("totalBlocks", schedule.getBlocks().size());
        metadata.put("totalAvailableHours", totalAvailableHours);
        metadata.put("completionRate", completionRate);
        metadata.put("utilizationRate", utilizationRate);
        metadata.put("studyPeriodDays", 
                ChronoUnit.DAYS.between(planSpec.getStartDate(), 
                        schedule.getBlocks().isEmpty() ? planSpec.getStartDate() :
                        schedule.getBlocks().stream()
                                .map(Schedule.ScheduledBlock::getDate)
                                .max(LocalDate::compareTo).orElse(planSpec.getStartDate())) + 1);
    }
    
    /**
     * Format schedule as human-readable string with tree structure
     */
    public String formatSchedule(Schedule schedule) {
        if (schedule == null || schedule.getBlocks().isEmpty()) {
            return "No schedule generated yet. Use 'generate schedule' command.";
        }
        
        StringBuilder sb = new StringBuilder();
        
        // Header
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║        📅 MULTI-SUBJECT STUDY SCHEDULE                     ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");
        
        sb.append("Plan: ").append(schedule.getPlanName()).append("\n");
        sb.append("Generated: ").append(schedule.getGeneratedAt()).append("\n");
        sb.append("Period: ").append(schedule.getStartDate()).append(" to ")
          .append(schedule.getEndDate()).append("\n\n");
        
        // Score summary
        Schedule.ScheduleScore score = schedule.getScore();
        Object totalBlocks = schedule.getMetadata("totalBlocks");
        Object totalAvailableHours = schedule.getMetadata("totalAvailableHours");
        Object completionRate = schedule.getMetadata("completionRate");
        Object utilizationRate = schedule.getMetadata("utilizationRate");
        
        sb.append("╭─ SCHEDULE SCORE ─────────────────────────────────────╮\n");
        sb.append("│ Overall Score:    ").append(String.format("%.1f", score.getOverallScore())).append("/100\n");
        sb.append("│ Completion Rate:  ").append(String.format("%.1f", 
                completionRate != null ? ((Double)completionRate) * 100 : 0.0)).append("%\n");
        sb.append("│ Utilization:      ").append(String.format("%.1f", 
                utilizationRate != null ? ((Double)utilizationRate) * 100 : 0.0)).append("%\n");
        sb.append("│ Total Blocks:     ").append(totalBlocks != null ? totalBlocks : 0).append("\n");
        sb.append("│ Total Hours:      ").append(String.format("%.1f", score.getTotalScheduledHours())).append("h / ")
          .append(String.format("%.1f", totalAvailableHours != null ? (Double)totalAvailableHours : 0.0)).append("h\n");
        sb.append("╰──────────────────────────────────────────────────────╯\n\n");
        
        // Blocks grouped by date
        sb.append("╭─ DAILY SCHEDULE ─────────────────────────────────────╮\n");
        
        Map<LocalDate, List<Schedule.ScheduledBlock>> blocksByDate = schedule.getBlocks().stream()
                .collect(Collectors.groupingBy(Schedule.ScheduledBlock::getDate, TreeMap::new,
                        Collectors.toList()));
        
        blocksByDate.forEach((date, blocks) -> {
            blocks.sort(Comparator.comparing(Schedule.ScheduledBlock::getStartTime));
            
            double dayTotal = blocks.stream().mapToDouble(Schedule.ScheduledBlock::getDurationHours).sum();
            
            sb.append("│\n");
            sb.append("├─ ").append(date).append(" (").append(String.format("%.1f", dayTotal)).append("h)\n");
            
            for (int i = 0; i < blocks.size(); i++) {
                Schedule.ScheduledBlock block = blocks.get(i);
                boolean isLast = (i == blocks.size() - 1);
                String prefix = isLast ? "   └─" : "   ├─";
                
                String priorityIcon = getPriorityIcon(block.getPriority());
                
                sb.append(prefix).append(" ")
                  .append(block.getStartTime()).append("-").append(block.getEndTime())
                  .append(" │ ").append(priorityIcon).append(" ")
                  .append(block.getCourseName())
                  .append(" (").append(String.format("%.1f", block.getDurationHours())).append("h)")
                  .append("\n");
            }
        });
        
        sb.append("╰──────────────────────────────────────────────────────╯\n\n");
        
        // Subject summary
        sb.append("╭─ SUBJECT SUMMARY ────────────────────────────────────╮\n");
        
        Map<String, List<Schedule.ScheduledBlock>> blocksByCourse = schedule.getBlocks().stream()
                .collect(Collectors.groupingBy(Schedule.ScheduledBlock::getCourseId));
        
        blocksByCourse.forEach((courseId, blocks) -> {
            double totalHours = blocks.stream().mapToDouble(Schedule.ScheduledBlock::getDurationHours).sum();
            Priority priority = blocks.get(0).getPriority();
            String icon = getPriorityIcon(priority);
            
            sb.append("│ ").append(icon).append(" ").append(courseId)
              .append(": ").append(blocks.size()).append(" blocks, ")
              .append(String.format("%.1f", totalHours)).append(" hours")
              .append(" (").append(priority).append(")\n");
        });
        
        sb.append("╰──────────────────────────────────────────────────────╯\n");
        
        return sb.toString();
    }
    
    /**
     * Get icon for priority level
     */
    private String getPriorityIcon(Priority priority) {
        return switch (priority) {
            case HIGH -> "🔴";
            case MEDIUM -> "🟡";
            case LOW -> "🟢";
        };
    }
}
