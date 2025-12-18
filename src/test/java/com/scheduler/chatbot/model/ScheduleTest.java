package com.scheduler.chatbot.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for Schedule class and its nested classes
 */
class ScheduleTest {

    private Schedule schedule;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2024, 12, 1);
        endDate = LocalDate.of(2024, 12, 20);
        schedule = new Schedule("Final Exam Prep", startDate, endDate);
    }

    // ==================== Basic Construction Tests ====================

    @Test
    @DisplayName("Should create schedule with default constructor")
    void testDefaultConstructor() {
        Schedule defaultSchedule = new Schedule();
        
        assertNotNull(defaultSchedule.getGeneratedAt());
        assertNotNull(defaultSchedule.getBlocks());
        assertNotNull(defaultSchedule.getScore());
        assertNotNull(defaultSchedule.getExplanations());
        assertNotNull(defaultSchedule.getMetadata());
        assertTrue(defaultSchedule.getBlocks().isEmpty());
        assertTrue(defaultSchedule.getExplanations().isEmpty());
    }

    @Test
    @DisplayName("Should create schedule with parameters")
    void testParameterizedConstructor() {
        assertEquals("Final Exam Prep", schedule.getPlanName());
        assertEquals(startDate, schedule.getStartDate());
        assertEquals(endDate, schedule.getEndDate());
        assertNotNull(schedule.getGeneratedAt());
        assertTrue(schedule.isEmpty());
    }

    @Test
    @DisplayName("Should have generated timestamp")
    void testGeneratedAtTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime generated = schedule.getGeneratedAt();
        
        assertTrue(generated.isBefore(now.plusSeconds(1)));
        assertTrue(generated.isAfter(now.minusSeconds(5)));
    }

    // ==================== Block Management Tests ====================

    @Test
    @DisplayName("Should add block successfully")
    void testAddBlock() {
        Schedule.ScheduledBlock block = createBlock(
            "MATH101", 
            LocalDate.of(2024, 12, 1), 
            "09:00", "10:30", 90
        );
        
        schedule.addBlock(block);
        
        assertEquals(1, schedule.getBlocks().size());
        assertEquals("MATH101", schedule.getBlocks().get(0).getCourseId());
        assertFalse(schedule.isEmpty());
    }

    @Test
    @DisplayName("Should throw exception when adding null block")
    void testAddNullBlock() {
        assertThrows(IllegalArgumentException.class, () -> {
            schedule.addBlock(null);
        });
    }

    @Test
    @DisplayName("Should add multiple blocks")
    void testAddMultipleBlocks() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "11:00", "12:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 2), "09:00", "10:30", 90));
        
        assertEquals(3, schedule.getBlocks().size());
    }

    @Test
    @DisplayName("Should remove block successfully")
    void testRemoveBlock() {
        Schedule.ScheduledBlock block = createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90);
        schedule.addBlock(block);
        
        boolean removed = schedule.removeBlock(block);
        
        assertTrue(removed);
        assertEquals(0, schedule.getBlocks().size());
        assertTrue(schedule.isEmpty());
    }

    @Test
    @DisplayName("Should return false when removing non-existent block")
    void testRemoveNonExistentBlock() {
        Schedule.ScheduledBlock block = createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90);
        
        boolean removed = schedule.removeBlock(block);
        
        assertFalse(removed);
    }

    @Test
    @DisplayName("Should remove blocks by course ID")
    void testRemoveBlocksByCourse() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "11:00", "12:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 2), "09:00", "10:30", 90));
        
        int removed = schedule.removeBlocksByCourse("MATH101");
        
        assertEquals(2, removed);
        assertEquals(1, schedule.getBlocks().size());
        assertEquals("PHYS101", schedule.getBlocks().get(0).getCourseId());
    }

    @Test
    @DisplayName("Should clear all blocks")
    void testClearBlocks() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "11:00", "12:30", 90));
        
        schedule.clearBlocks();
        
        assertTrue(schedule.isEmpty());
        assertEquals(0, schedule.getBlocks().size());
    }

    // ==================== Query Methods Tests ====================

    @Test
    @DisplayName("Should get blocks for specific date")
    void testGetBlocksForDate() {
        LocalDate date1 = LocalDate.of(2024, 12, 1);
        LocalDate date2 = LocalDate.of(2024, 12, 2);
        
        schedule.addBlock(createBlock("MATH101", date1, "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", date1, "11:00", "12:30", 90));
        schedule.addBlock(createBlock("MATH101", date2, "09:00", "10:30", 90));
        
        List<Schedule.ScheduledBlock> date1Blocks = schedule.getBlocksForDate(date1);
        
        assertEquals(2, date1Blocks.size());
        assertTrue(date1Blocks.stream().allMatch(b -> b.getDate().equals(date1)));
        // Should be sorted by start time
        assertEquals("09:00", date1Blocks.get(0).getStartTime());
        assertEquals("11:00", date1Blocks.get(1).getStartTime());
    }

    @Test
    @DisplayName("Should get blocks for specific course")
    void testGetBlocksForCourse() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "11:00", "12:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 2), "09:00", "10:30", 90));
        
        List<Schedule.ScheduledBlock> mathBlocks = schedule.getBlocksForCourse("MATH101");
        
        assertEquals(2, mathBlocks.size());
        assertTrue(mathBlocks.stream().allMatch(b -> b.getCourseId().equals("MATH101")));
    }

    @Test
    @DisplayName("Should calculate total scheduled hours")
    void testGetTotalScheduledHours() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "11:00", "12:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 2), "09:00", "10:30", 90));
        
        double totalHours = schedule.getTotalScheduledHours();
        
        assertEquals(4.5, totalHours, 0.01);
    }

    @Test
    @DisplayName("Should calculate scheduled hours for specific course")
    void testGetScheduledHoursForCourse() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "11:00", "12:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 2), "09:00", "10:30", 90));
        
        double mathHours = schedule.getScheduledHoursForCourse("MATH101");
        
        assertEquals(3.0, mathHours, 0.01);
    }

    @Test
    @DisplayName("Should get unique scheduled dates")
    void testGetScheduledDates() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "11:00", "12:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 3), "09:00", "10:30", 90));
        
        List<LocalDate> dates = schedule.getScheduledDates();
        
        assertEquals(2, dates.size());
        assertTrue(dates.contains(LocalDate.of(2024, 12, 1)));
        assertTrue(dates.contains(LocalDate.of(2024, 12, 3)));
        // Should be sorted
        assertEquals(LocalDate.of(2024, 12, 1), dates.get(0));
    }

    @Test
    @DisplayName("Should get unique course IDs")
    void testGetCourseIds() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "11:00", "12:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 2), "09:00", "10:30", 90));
        
        List<String> courseIds = schedule.getCourseIds();
        
        assertEquals(2, courseIds.size());
        assertTrue(courseIds.contains("MATH101"));
        assertTrue(courseIds.contains("PHYS101"));
    }

    // ==================== Explanation Tests ====================

    @Test
    @DisplayName("Should add explanation")
    void testAddExplanation() {
        schedule.addExplanation("MATH101 scheduled early due to HIGH priority");
        
        assertEquals(1, schedule.getExplanations().size());
        assertEquals("MATH101 scheduled early due to HIGH priority", schedule.getExplanations().get(0));
    }

    @Test
    @DisplayName("Should not add null or empty explanation")
    void testAddEmptyExplanation() {
        schedule.addExplanation(null);
        schedule.addExplanation("");
        schedule.addExplanation("   ");
        
        assertEquals(0, schedule.getExplanations().size());
    }

    @Test
    @DisplayName("Should add multiple explanations")
    void testAddMultipleExplanations() {
        List<String> explanations = List.of(
            "MATH101: HIGH priority, front-loaded to first half",
            "PHYS101: MEDIUM priority, evenly distributed"
        );
        
        schedule.addExplanations(explanations);
        
        assertEquals(2, schedule.getExplanations().size());
    }

    @Test
    @DisplayName("Should clear explanations")
    void testClearExplanations() {
        schedule.addExplanation("Test explanation");
        schedule.clearExplanations();
        
        assertEquals(0, schedule.getExplanations().size());
    }

    // ==================== Score Calculation Tests ====================

    @Test
    @DisplayName("Should recalculate score when adding blocks")
    void testScoreRecalculationOnAdd() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        
        assertNotNull(schedule.getScore());
        assertEquals(1.5, schedule.getScore().getTotalScheduledHours(), 0.01);
    }

    @Test
    @DisplayName("Should recalculate score when removing blocks")
    void testScoreRecalculationOnRemove() {
        Schedule.ScheduledBlock block = createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90);
        schedule.addBlock(block);
        schedule.removeBlock(block);
        
        assertEquals(0.0, schedule.getScore().getTotalScheduledHours(), 0.01);
    }

    @Test
    @DisplayName("Should calculate spreadness score")
    void testSpreadnessScore() {
        // Add evenly distributed blocks
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 2), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 3), "09:00", "10:30", 90));
        
        double spreadness = schedule.getScore().getSpreadnessScore();
        
        assertTrue(spreadness >= 0 && spreadness <= 100);
        assertTrue(spreadness > 80); // Should be high for even distribution
    }

    @Test
    @DisplayName("Should calculate buffer score")
    void testBufferScore() {
        Schedule.ScheduledBlock block = createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90);
        block.setDeadline(LocalDate.of(2024, 12, 10)); // Deadline 9 days later
        schedule.addBlock(block);
        
        double buffer = schedule.getScore().getBufferScore();
        
        assertTrue(buffer >= 0 && buffer <= 100);
        assertTrue(buffer > 0); // Should have buffer
    }

    @Test
    @DisplayName("Should calculate interleave score")
    void testInterleaveScore() {
        // Interleaved courses
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "11:00", "12:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 2), "09:00", "10:30", 90));
        
        double interleave = schedule.getScore().getInterleaveScore();
        
        assertTrue(interleave >= 0 && interleave <= 100);
    }

    @Test
    @DisplayName("Should calculate overall score")
    void testOverallScore() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 2), "09:00", "10:30", 90));
        
        double overall = schedule.getScore().getOverallScore();
        
        assertTrue(overall >= 0 && overall <= 100);
    }

    @Test
    @DisplayName("Should calculate completion percentage")
    void testCompletionPercentage() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 2), "09:00", "10:30", 90));
        
        double completion = schedule.getCompletionPercentage("MATH101", 10.0);
        
        assertEquals(30.0, completion, 0.1); // 3 hours / 10 hours = 30%
    }

    // ==================== Metadata Tests ====================

    @Test
    @DisplayName("Should add and retrieve metadata")
    void testMetadata() {
        schedule.addMetadata("algorithm", "front-loading");
        schedule.addMetadata("version", "1.0");
        
        assertEquals("front-loading", schedule.getMetadata("algorithm"));
        assertEquals("1.0", schedule.getMetadata("version"));
    }

    @Test
    @DisplayName("Should return null for non-existent metadata")
    void testNonExistentMetadata() {
        assertNull(schedule.getMetadata("nonexistent"));
    }

    // ==================== Summary Tests ====================

    @Test
    @DisplayName("Should generate schedule summary")
    void testGetSummary() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "11:00", "12:30", 90));
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 2), "09:00", "10:30", 90));
        
        Schedule.ScheduleSummary summary = schedule.getSummary();
        
        assertEquals(3, summary.getTotalBlocks());
        assertEquals(4.5, summary.getTotalHours(), 0.01);
        assertEquals(2, summary.getScheduledDays());
        assertEquals(2, summary.getCoursesCount());
        assertEquals(2.25, summary.getAverageHoursPerDay(), 0.01);
    }

    @Test
    @DisplayName("Should handle empty schedule in summary")
    void testEmptySummary() {
        Schedule.ScheduleSummary summary = schedule.getSummary();
        
        assertEquals(0, summary.getTotalBlocks());
        assertEquals(0, summary.getTotalHours(), 0.01);
        assertEquals(0, summary.getScheduledDays());
        assertEquals(0, summary.getAverageHoursPerDay(), 0.01);
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("Should validate schedule without overlaps")
    void testValidateNoOverlaps() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "11:00", "12:30", 90));
        
        Schedule.ValidationResult result = schedule.validate();
        
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should detect overlapping blocks")
    void testValidateWithOverlaps() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90));
        schedule.addBlock(createBlock("PHYS101", LocalDate.of(2024, 12, 1), "10:00", "11:30", 90));
        
        Schedule.ValidationResult result = schedule.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() > 0);
        assertTrue(result.getErrors().get(0).contains("Overlapping blocks"));
    }

    @Test
    @DisplayName("Should detect blocks before start date")
    void testValidateBlockBeforeStart() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 11, 30), "09:00", "10:30", 90));
        
        Schedule.ValidationResult result = schedule.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("before schedule start date")));
    }

    @Test
    @DisplayName("Should detect blocks after end date")
    void testValidateBlockAfterEnd() {
        schedule.addBlock(createBlock("MATH101", LocalDate.of(2024, 12, 25), "09:00", "10:30", 90));
        
        Schedule.ValidationResult result = schedule.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("after schedule end date")));
    }

    // ==================== ScheduledBlock Tests ====================

    @Test
    @DisplayName("Should create scheduled block")
    void testScheduledBlockCreation() {
        Schedule.ScheduledBlock block = new Schedule.ScheduledBlock(
            "MATH101", 
            LocalDate.of(2024, 12, 1), 
            "09:00", "10:30", 90
        );
        
        assertEquals("MATH101", block.getCourseId());
        assertEquals(LocalDate.of(2024, 12, 1), block.getDate());
        assertEquals("09:00", block.getStartTime());
        assertEquals("10:30", block.getEndTime());
        assertEquals(90, block.getDurationMinutes());
        assertEquals(1.5, block.getDurationHours(), 0.01);
    }

    @Test
    @DisplayName("Should set block priority")
    void testScheduledBlockPriority() {
        Schedule.ScheduledBlock block = createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90);
        block.setPriority(Priority.HIGH);
        
        assertEquals(Priority.HIGH, block.getPriority());
    }

    @Test
    @DisplayName("Should set block component and deadline")
    void testScheduledBlockComponentAndDeadline() {
        Schedule.ScheduledBlock block = createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90);
        block.setComponentName("Assignment 1");
        block.setDeadline(LocalDate.of(2024, 12, 5));
        
        assertEquals("Assignment 1", block.getComponentName());
        assertEquals(LocalDate.of(2024, 12, 5), block.getDeadline());
    }

    @Test
    @DisplayName("Should format block as string")
    void testScheduledBlockToString() {
        Schedule.ScheduledBlock block = createBlock("MATH101", LocalDate.of(2024, 12, 1), "09:00", "10:30", 90);
        
        String str = block.toString();
        
        assertTrue(str.contains("MATH101"));
        assertTrue(str.contains("2024-12-01"));
        assertTrue(str.contains("09:00"));
        assertTrue(str.contains("10:30"));
    }

    // ==================== ScheduleScore Tests ====================

    @Test
    @DisplayName("Should create default score")
    void testDefaultScore() {
        Schedule.ScheduleScore score = new Schedule.ScheduleScore();
        
        assertEquals(0.0, score.getOverallScore(), 0.01);
        assertEquals(0.0, score.getSpreadnessScore(), 0.01);
        assertEquals(0.0, score.getBufferScore(), 0.01);
        assertEquals(0.0, score.getInterleaveScore(), 0.01);
        assertEquals(0.0, score.getTotalScheduledHours(), 0.01);
        assertNotNull(score.getCourseHours());
    }

    @Test
    @DisplayName("Should format score as string")
    void testScoreToString() {
        Schedule.ScheduleScore score = new Schedule.ScheduleScore();
        score.setOverallScore(85.5);
        score.setSpreadnessScore(90.0);
        score.setBufferScore(80.0);
        score.setInterleaveScore(86.5);
        
        String str = score.toString();
        
        assertTrue(str.contains("85.5"));
        assertTrue(str.contains("90.0"));
        assertTrue(str.contains("80.0"));
        assertTrue(str.contains("86.5"));
    }

    // ==================== Helper Methods ====================

    private Schedule.ScheduledBlock createBlock(String courseId, LocalDate date, 
                                               String startTime, String endTime, int durationMinutes) {
        return new Schedule.ScheduledBlock(courseId, date, startTime, endTime, durationMinutes);
    }
}
