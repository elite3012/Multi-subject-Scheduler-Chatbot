package com.scheduler.chatbot;

import com.scheduler.chatbot.model.PlanSpec;
import com.scheduler.chatbot.model.Priority;
import com.scheduler.chatbot.model.Schedule;
import com.scheduler.chatbot.service.SchedulerService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test cases for SchedulerService
 * TODO: Implement test cases for scheduler functionality
 */
@SpringBootTest
public class SchedulerServiceTest {

    @Autowired
    private SchedulerService schedulerService;

    private Schedule schedule;

    @BeforeEach
    public void setup() {
        // TODO: Initialize schedule before each test
        schedule = new Schedule();
    }

    /**
     * Test adding a subject
     * TODO: Implement test
     */
    @Test
    public void testAddSubject() {
        // TODO: Add subject and verify
        PlanSpec plan = new PlanSpec("Test Plan");

        PlanSpec.CourseSpec course = new PlanSpec.CourseSpec("Math", Priority.HIGH, 10);

        plan.addCourse(course);

        assertEquals(1, plan.getCourses().size());
        assertEquals("Math", plan.getCourse("Math").getId());
        assertEquals(Priority.HIGH, plan.getCourse("Math").getPriority());
        assertEquals(10, plan.getCourse("Math").getWorkloadHours());
    }

    /**
     * Test setting availability
     * TODO: Implement test
     */
    @Test
    public void testSetAvailability() {
        // TODO: Set availability and verify
        PlanSpec plan = new PlanSpec("Test Plan");

        LocalDate date = LocalDate.now();
        plan.setAvailability(date, 5.0);

        assertTrue(plan.getAvailability().containsKey(date));
        assertEquals(5.0, plan.getAvailability(date));
    }

    /**
     * Test generating schedule
     * TODO: Implement test
     */
    @Test
    public void testGenerateSchedule() {
        // TODO: Add subjects, set availability, generate schedule, and verify
    }

    /**
     * Test priority front-load calculation
     * TODO: Implement test
     */
    @Test
    public void testPriorityFrontLoad() {
        // TODO: Test that HIGH priority has 60% front-load
        assertEquals(0.60, Priority.HIGH.getFrontLoadRatio());
        // TODO: Test that LOW priority has 40% front-load
        assertEquals(0.40, Priority.LOW.getFrontLoadRatio());
    }
}
