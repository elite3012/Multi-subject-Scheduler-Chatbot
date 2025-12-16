package com.scheduler.chatbot.model;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PlanSpec class and its validation logic
 */
class PlanSpecTest {

    private PlanSpec planSpec;

    @BeforeEach
    void setUp() {
        planSpec = new PlanSpec("Test Plan");
    }

    // ==================== Basic Construction Tests ====================

    @Test
    @DisplayName("Should create PlanSpec with default values")
    void testDefaultConstructor() {
        PlanSpec plan = new PlanSpec();
        
        assertEquals("Untitled Plan", plan.getPlanName());
        assertNotNull(plan.getTimezone());
        assertNotNull(plan.getStartDate());
        assertNotNull(plan.getCourses());
        assertNotNull(plan.getAvailability());
        assertNotNull(plan.getRules());
        assertNotNull(plan.getSoftPrefs());
        assertTrue(plan.getCourses().isEmpty());
        assertTrue(plan.getAvailability().isEmpty());
    }

    @Test
    @DisplayName("Should create PlanSpec with custom name")
    void testNamedConstructor() {
        PlanSpec plan = new PlanSpec("Final Exam Prep");
        
        assertEquals("Final Exam Prep", plan.getPlanName());
    }

    // ==================== Course Management Tests ====================

    @Test
    @DisplayName("Should add course to plan")
    void testAddCourse() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 20.0);
        planSpec.addCourse(course);
        
        assertEquals(1, planSpec.getCourses().size());
        assertEquals("MATH101", planSpec.getCourses().get(0).getId());
    }

    @Test
    @DisplayName("Should remove course from plan")
    void testRemoveCourse() {
        PlanSpec.CourseSpec course1 = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 20.0);
        PlanSpec.CourseSpec course2 = new PlanSpec.CourseSpec("PHYS101", Priority.MEDIUM, 15.0);
        
        planSpec.addCourse(course1);
        planSpec.addCourse(course2);
        assertEquals(2, planSpec.getCourses().size());
        
        planSpec.removeCourse("MATH101");
        assertEquals(1, planSpec.getCourses().size());
        assertEquals("PHYS101", planSpec.getCourses().get(0).getId());
    }

    @Test
    @DisplayName("Should get course by ID")
    void testGetCourse() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 20.0);
        planSpec.addCourse(course);
        
        PlanSpec.CourseSpec retrieved = planSpec.getCourse("MATH101");
        assertNotNull(retrieved);
        assertEquals("MATH101", retrieved.getId());
        
        PlanSpec.CourseSpec notFound = planSpec.getCourse("INVALID");
        assertNull(notFound);
    }

    // ==================== Availability Tests ====================

    @Test
    @DisplayName("Should set and get availability")
    void testSetAvailability() {
        LocalDate date = LocalDate.of(2024, 12, 1);
        planSpec.setAvailability(date, 5.0);
        
        assertEquals(5.0, planSpec.getAvailability(date));
    }

    @Test
    @DisplayName("Should return 0 for dates with no availability")
    void testGetAvailabilityDefault() {
        LocalDate date = LocalDate.of(2024, 12, 1);
        assertEquals(0.0, planSpec.getAvailability(date));
    }

    // ==================== Validation Tests - Success Cases ====================

    @Test
    @DisplayName("Should validate valid plan successfully")
    void testValidPlan() {
        // Setup valid plan
        planSpec.setPlanName("Valid Plan");
        planSpec.setStartDate(LocalDate.of(2024, 12, 1));
        planSpec.setEndDate(LocalDate.of(2024, 12, 10));
        
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        
        // Set availability within max limit (8.0 hours per day)
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 5.0);
        planSpec.setAvailability(LocalDate.of(2024, 12, 2), 5.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        if (!result.isValid()) {
            System.out.println("Validation errors: " + result.getErrors());
        }
        
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    // ==================== Validation Tests - Plan Name ====================

    @Test
    @DisplayName("Should fail validation when plan name is null")
    void testValidateNullPlanName() {
        planSpec.setPlanName(null);
        
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 10.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Plan name cannot be empty")));
    }

    @Test
    @DisplayName("Should fail validation when plan name is empty")
    void testValidateEmptyPlanName() {
        planSpec.setPlanName("   ");
        
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 10.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Plan name cannot be empty")));
    }

    // ==================== Validation Tests - Courses ====================

    @Test
    @DisplayName("Should fail validation when no courses specified")
    void testValidateNoCourses() {
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 10.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("At least one course must be specified")));
    }

    @Test
    @DisplayName("Should fail validation when duplicate course IDs exist")
    void testValidateDuplicateCourseIds() {
        PlanSpec.CourseSpec course1 = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        PlanSpec.CourseSpec course2 = new PlanSpec.CourseSpec("MATH101", Priority.MEDIUM, 15.0);
        
        planSpec.addCourse(course1);
        planSpec.addCourse(course2);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 30.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Duplicate course IDs")));
    }

    @Test
    @DisplayName("Should fail validation when course ID is empty")
    void testValidateEmptyCourseId() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 10.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Course ID cannot be empty")));
    }

    @Test
    @DisplayName("Should fail validation when course has no priority")
    void testValidateNoPriority() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec();
        course.setId("MATH101");
        course.setWorkloadHours(10.0);
        // No priority set
        
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 10.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Priority must be specified")));
    }

    @Test
    @DisplayName("Should fail validation when course workload is zero")
    void testValidateZeroWorkload() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 0.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 10.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Workload hours must be positive")));
    }

    @Test
    @DisplayName("Should fail validation when course workload is negative")
    void testValidateNegativeWorkload() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, -5.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 10.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Workload hours must be positive")));
    }

    // ==================== Validation Tests - Components ====================

    @Test
    @DisplayName("Should fail validation when component name is empty")
    void testValidateEmptyComponentName() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        course.addComponent("", 5.0, LocalDate.of(2024, 12, 5));
        
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 10.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Component name cannot be empty")));
    }

    @Test
    @DisplayName("Should fail validation when component hours exceed course workload")
    void testValidateComponentHoursExceed() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        course.addComponent("Assignment 1", 6.0, LocalDate.of(2024, 12, 5));
        course.addComponent("Assignment 2", 6.0, LocalDate.of(2024, 12, 10));
        
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 20.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Component hours exceed total workload")));
    }

    // ==================== Validation Tests - Availability ====================

    @Test
    @DisplayName("Should fail validation when no availability specified")
    void testValidateNoAvailability() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("No availability specified")));
    }

    @Test
    @DisplayName("Should fail validation when availability is negative")
    void testValidateNegativeAvailability() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), -5.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Negative availability hours")));
    }

    @Test
    @DisplayName("Should fail validation when availability exceeds max hours per day")
    void testValidateAvailabilityExceedsMax() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 10.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("exceeds max hours per day")));
    }

    // ==================== Validation Tests - Date Range ====================

    @Test
    @DisplayName("Should fail validation when start date is after end date")
    void testValidateStartAfterEnd() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 5.0);
        
        planSpec.setStartDate(LocalDate.of(2024, 12, 10));
        planSpec.setEndDate(LocalDate.of(2024, 12, 1));
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Start date") && e.contains("after end date")));
    }

    // ==================== Validation Tests - Workload vs Availability ====================

    @Test
    @DisplayName("Should warn when workload exceeds availability")
    void testValidateWorkloadExceedsAvailability() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 20.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 5.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Total workload") && e.contains("exceeds total availability")));
    }

    @Test
    @DisplayName("Should pass validation when workload equals availability")
    void testValidateWorkloadEqualsAvailability() {
        planSpec.setStartDate(LocalDate.of(2024, 12, 1));
        planSpec.setEndDate(LocalDate.of(2024, 12, 10));
        
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        
        // Set availability within max limit (8.0 hours per day)
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 5.0);
        planSpec.setAvailability(LocalDate.of(2024, 12, 2), 5.0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        if (!result.isValid()) {
            System.out.println("Validation errors: " + result.getErrors());
        }
        
        assertTrue(result.isValid());
    }

    // ==================== Validation Tests - Scheduling Rules ====================

    @Test
    @DisplayName("Should fail validation when max hours per day is zero")
    void testValidateZeroMaxHours() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 10.0);
        
        planSpec.getRules().setMaxHoursPerDay(0);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Max hours per day must be positive")));
    }

    @Test
    @DisplayName("Should fail validation when block duration is negative")
    void testValidateNegativeBlockDuration() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 5.0);
        planSpec.setAvailability(LocalDate.of(2024, 12, 2), 5.0);
        
        planSpec.getRules().setBlockDurationMinutes(-90);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Block duration must be positive")));
    }

    @Test
    @DisplayName("Should fail validation when break duration is negative")
    void testValidateNegativeBreakDuration() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0);
        planSpec.addCourse(course);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 5.0);
        planSpec.setAvailability(LocalDate.of(2024, 12, 2), 5.0);
        
        planSpec.getRules().setBreakDurationMinutes(-15);
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.contains("Break duration cannot be negative")));
    }

    // ==================== Helper Method Tests ====================

    @Test
    @DisplayName("Should calculate total workload hours correctly")
    void testGetTotalWorkloadHours() {
        planSpec.addCourse(new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 20.0));
        planSpec.addCourse(new PlanSpec.CourseSpec("PHYS101", Priority.MEDIUM, 15.0));
        planSpec.addCourse(new PlanSpec.CourseSpec("ART101", Priority.LOW, 10.0));
        
        assertEquals(45.0, planSpec.getTotalWorkloadHours());
    }

    @Test
    @DisplayName("Should calculate total available hours correctly")
    void testGetTotalAvailableHours() {
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 5.0);
        planSpec.setAvailability(LocalDate.of(2024, 12, 2), 6.0);
        planSpec.setAvailability(LocalDate.of(2024, 12, 3), 4.0);
        
        assertEquals(15.0, planSpec.getTotalAvailableHours());
    }

    @Test
    @DisplayName("Should detect shortfall correctly")
    void testHasShortfall() {
        planSpec.addCourse(new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 20.0));
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 5.0);
        
        assertTrue(planSpec.hasShortfall());
        assertEquals(15.0, planSpec.getShortfallHours());
    }

    @Test
    @DisplayName("Should return no shortfall when capacity is sufficient")
    void testNoShortfall() {
        planSpec.addCourse(new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 10.0));
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 5.0);
        planSpec.setAvailability(LocalDate.of(2024, 12, 2), 5.0);
        
        assertFalse(planSpec.hasShortfall());
        assertEquals(0.0, planSpec.getShortfallHours());
    }

    @Test
    @DisplayName("Should get available dates in sorted order")
    void testGetAvailableDates() {
        planSpec.setAvailability(LocalDate.of(2024, 12, 3), 5.0);
        planSpec.setAvailability(LocalDate.of(2024, 12, 1), 5.0);
        planSpec.setAvailability(LocalDate.of(2024, 12, 2), 5.0);
        
        var dates = planSpec.getAvailableDates();
        
        assertEquals(3, dates.size());
        assertEquals(LocalDate.of(2024, 12, 1), dates.get(0));
        assertEquals(LocalDate.of(2024, 12, 2), dates.get(1));
        assertEquals(LocalDate.of(2024, 12, 3), dates.get(2));
    }

    // ==================== CourseSpec Tests ====================

    @Test
    @DisplayName("Should add components to course")
    void testAddComponentToCourse() {
        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 20.0);
        course.addComponent("Assignment 1", 5.0, LocalDate.of(2024, 12, 5));
        course.addComponent("Midterm Prep", 8.0, LocalDate.of(2024, 12, 15));
        
        assertEquals(2, course.getComponents().size());
        assertEquals(13.0, course.getComponentTotalHours());
    }

    // ==================== SchedulingRules Tests ====================

    @Test
    @DisplayName("Should calculate block duration in hours")
    void testBlockDurationHours() {
        PlanSpec.SchedulingRules rules = new PlanSpec.SchedulingRules();
        
        assertEquals(1.5, rules.getBlockDurationHours());
    }

    @Test
    @DisplayName("Should calculate break duration in hours")
    void testBreakDurationHours() {
        PlanSpec.SchedulingRules rules = new PlanSpec.SchedulingRules();
        
        assertEquals(0.25, rules.getBreakDurationHours());
    }

    @Test
    @DisplayName("Should calculate max blocks per day")
    void testMaxBlocksPerDay() {
        PlanSpec.SchedulingRules rules = new PlanSpec.SchedulingRules();
        rules.setMaxHoursPerDay(8.0);
        rules.setBlockDurationMinutes(90);
        rules.setBreakDurationMinutes(15);
        
        int maxBlocks = rules.getMaxBlocksPerDay();
        
        assertTrue(maxBlocks >= 4 && maxBlocks <= 5); // 8 hours / (1.5h + 0.25h) ≈ 4.57
    }

    // ==================== Complex Validation Scenarios ====================

    @Test
    @DisplayName("Should validate complex plan with multiple courses and components")
    void testComplexPlanValidation() {
        planSpec.setPlanName("Final Exam Preparation");
        planSpec.setStartDate(LocalDate.of(2024, 12, 1));
        planSpec.setEndDate(LocalDate.of(2024, 12, 20));
        
        // Math course with components
        PlanSpec.CourseSpec math = new PlanSpec.CourseSpec("MATH101", Priority.HIGH, 20.0);
        math.setExamDate(LocalDate.of(2024, 12, 18));
        math.addComponent("Chapter 1-5", 8.0, LocalDate.of(2024, 12, 10));
        math.addComponent("Chapter 6-10", 8.0, LocalDate.of(2024, 12, 15));
        math.addComponent("Practice Exam", 4.0, LocalDate.of(2024, 12, 17));
        planSpec.addCourse(math);
        
        // Physics course
        PlanSpec.CourseSpec physics = new PlanSpec.CourseSpec("PHYS101", Priority.MEDIUM, 15.0);
        physics.setExamDate(LocalDate.of(2024, 12, 19));
        planSpec.addCourse(physics);
        
        // Art course
        PlanSpec.CourseSpec art = new PlanSpec.CourseSpec("ART101", Priority.LOW, 10.0);
        planSpec.addCourse(art);
        
        // Set availability
        for (int day = 1; day <= 20; day++) {
            planSpec.setAvailability(LocalDate.of(2024, 12, day), 3.0);
        }
        
        PlanSpec.ValidationResult result = planSpec.validate();
        
        assertTrue(result.isValid(), "Complex plan should be valid: " + result.getErrors());
        assertEquals(45.0, planSpec.getTotalWorkloadHours());
        assertEquals(60.0, planSpec.getTotalAvailableHours());
    }
}
