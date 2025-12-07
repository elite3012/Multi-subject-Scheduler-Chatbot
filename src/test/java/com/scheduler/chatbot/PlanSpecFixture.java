package com.scheduler.chatbot;

import com.scheduler.chatbot.model.PlanSpec;
// import com.scheduler.chatbot.model.PlanSpec.ComponentSpec;
import com.scheduler.chatbot.model.PlanSpec.CourseSpec;
import com.scheduler.chatbot.model.Priority;

import java.time.LocalDate;
import java.util.*;

/*
  Test fixture generator for PlanSpec.
  Produces valid and invalid objects that can be check by PlanSpec.validate().
 */
public class PlanSpecFixture {

    // VALID SAMPLE
    public static PlanSpec makeValidPlanSpec() {
        PlanSpec plan = new PlanSpec("Valid Plan");

        // Availability: 3 days, 5 hours each
        LocalDate today = LocalDate.now();
        plan.setAvailability(today, 5);
        plan.setAvailability(today.plusDays(1), 5);
        plan.setAvailability(today.plusDays(2), 5);

        plan.setStartDate(today);
        plan.setEndDate(today.plusDays(2));

        // Add valid courses
        CourseSpec c1 = new CourseSpec("CS101", Priority.HIGH, 8);
        c1.addComponent("Assignment1", 3, today.plusDays(1));
        c1.addComponent("Project", 5, today.plusDays(2));

        CourseSpec c2 = new CourseSpec("MATH202", Priority.MEDIUM, 7);

        plan.addCourse(c1);
        plan.addCourse(c2);

        // Use default valid scheduling rules and preferences
        return plan;
    }

    // INVALID SAMPLES, EACH VIOLATES 1 RULE

    // Missing plan name
    public static PlanSpec invalid_missingPlanName() {
        PlanSpec plan = makeValidPlanSpec();
        plan.setPlanName(""); // This should triggers: "Plan name cannot be empty"
        return plan;
    }

    /** No courses present */
    public static PlanSpec invalid_noCourses() {
        PlanSpec plan = makeValidPlanSpec();
        plan.setCourses(new ArrayList<>()); // This should triggers: "At least one course must be specified"
        return plan;
    }

    /** Duplicate course IDs */
    public static PlanSpec invalid_duplicateCourseId() {
        PlanSpec plan = makeValidPlanSpec();
        plan.addCourse(new CourseSpec("CS101", Priority.LOW, 3)); // same ID
        return plan; // This should triggers: "Duplicate course IDs found"
    }

    /** Course with empty ID */
    public static PlanSpec invalid_courseEmptyId() {
        PlanSpec plan = makeValidPlanSpec();
        plan.getCourses().get(0).setId(""); // This should triggers: "Course ID cannot be empty"
        return plan;
    }

    /** Course with null priority */
    public static PlanSpec invalid_courseMissingPriority() {
        PlanSpec plan = makeValidPlanSpec();
        plan.getCourses().get(0).setPriority(null); // This should triggers: "Priority must be specified"
        return plan;
    }

    /** Course with non-positive workload */
    public static PlanSpec invalid_courseZeroWorkload() {
        PlanSpec plan = makeValidPlanSpec();
        plan.getCourses().get(0).setWorkloadHours(0); // This should triggers: "Workload hours must be positive"
        return plan;
    }

    /** Availability negative hours */
    public static PlanSpec invalid_negativeAvailability() {
        PlanSpec plan = makeValidPlanSpec();
        LocalDate d = plan.getStartDate();
        plan.setAvailability(d, -3); // This should triggers: "Negative availability hours on <date>"
        return plan;
    }

    /** Availability > maxHoursPerDay (default max = 8) */
    public static PlanSpec invalid_exceedsMaxHoursPerDay() {
        PlanSpec plan = makeValidPlanSpec();
        LocalDate d = plan.getStartDate();
        plan.setAvailability(d, 20); // This should triggers: "Availability on <date> exceeds max hours per day"
        return plan;
    }

    /** Start date after end date */
    public static PlanSpec invalid_startDateAfterEndDate() {
        PlanSpec plan = makeValidPlanSpec();
        plan.setStartDate(LocalDate.now().plusDays(10));
        plan.setEndDate(LocalDate.now());
        return plan; // This should triggers: "Start date (...) is after end date (...)"
    }

    /** Workload > total available hours */
    public static PlanSpec invalid_workloadExceedsAvailability() {
        PlanSpec plan = makeValidPlanSpec();
        plan.getCourses().get(0).setWorkloadHours(1000); // definitely exceeds availability
        return plan;
    }

    /** Block duration <= 0 */
    public static PlanSpec invalid_blockDurationZero() {
        PlanSpec plan = makeValidPlanSpec();
        plan.getRules().setBlockDurationMinutes(0); // This should triggers: "Block duration must be positive"
        return plan;
    }

    /** Break duration negative */
    public static PlanSpec invalid_breakDurationNegative() {
        PlanSpec plan = makeValidPlanSpec();
        plan.getRules().setBreakDurationMinutes(-5); // This should triggers: "Break duration cannot be negative"
        return plan;
    }

    /** Max continuous block < block duration */
    public static PlanSpec invalid_maxContinuousLessThanBlock() {
        PlanSpec plan = makeValidPlanSpec();
        plan.getRules().setBlockDurationMinutes(90);
        plan.getRules().setMaxContinuousBlockMinutes(30);
        return plan;
    }

    /** Component hours exceed total course workload * 1.1 */
    public static PlanSpec invalid_componentHoursExceedWorkload() {
        PlanSpec plan = makeValidPlanSpec();
        CourseSpec c = plan.getCourses().get(0);

        c.getComponents().clear();
        c.addComponent("Overkill", 100, plan.getStartDate().plusDays(1));

        return plan; // This should triggers: "Component hours exceed total workload"
    }

    /** Component name empty */
    public static PlanSpec invalid_componentEmptyName() {
        PlanSpec plan = makeValidPlanSpec();
        CourseSpec c = plan.getCourses().get(0);

        c.getComponents().get(0).setName(""); // This should triggers: "Component name cannot be empty"
        return plan;
    }

    /** Component hours <= 0 */
    public static PlanSpec invalid_componentZeroHours() {
        PlanSpec plan = makeValidPlanSpec();
        CourseSpec c = plan.getCourses().get(0);

        c.getComponents().get(0).setEstimatedHours(0);
        return plan;
    }

    /** Component due date after plan end date */
    public static PlanSpec invalid_componentDueDateAfterEnd() {
        PlanSpec plan = makeValidPlanSpec();
        CourseSpec c = plan.getCourses().get(0);

        c.getComponents().get(0).setDueDate(plan.getEndDate().plusDays(5));
        return plan;
    }

    /** Exam date before start date */
    public static PlanSpec invalid_examBeforeStart() {
        PlanSpec plan = makeValidPlanSpec();
        CourseSpec c = plan.getCourses().get(0);

        c.setExamDate(plan.getStartDate().minusDays(2)); // This should triggers: "Exam date is before plan start date"
        return plan;
    }

    /** Exam date after end date */
    public static PlanSpec invalid_examAfterEnd() {
        PlanSpec plan = makeValidPlanSpec();
        CourseSpec c = plan.getCourses().get(0);

        c.setExamDate(plan.getEndDate().plusDays(3)); // This should triggers: "Exam date is after plan end date"
        return plan;
    }
}
