package com.scheduler.chatbot;

import com.scheduler.chatbot.model.Schedule;
import com.scheduler.chatbot.model.Priority;
import java.time.LocalDate;

/*
  Test fixtures for Schedule objects.
  Produces valid and invalid objects that can be check by Schedule.validate().
 */
public final class ScheduleFixture {

    private ScheduleFixture() {
        /* static helpers only */ }

    /*
     * Returns a valid schedule:
     * - startDate = 2025-12-10, endDate = 2025-12-12
     * - blocks are within the date range and non-overlapping
     */
    public static Schedule validSchedule() {
        LocalDate start = LocalDate.of(2025, 12, 10);
        LocalDate end = LocalDate.of(2025, 12, 12);

        Schedule s = new Schedule("Valid Plan", start, end);

        // Day 1 blocks
        Schedule.ScheduledBlock a1 = new Schedule.ScheduledBlock("MATH101", start, "09:00", "10:00", 60);
        a1.setCourseName("Mathematics");
        a1.setPriority(Priority.HIGH);

        Schedule.ScheduledBlock a2 = new Schedule.ScheduledBlock("PHYS101", start, "10:30", "12:00", 90);
        a2.setCourseName("Physics");
        a2.setPriority(Priority.MEDIUM);

        // Day 2 block
        Schedule.ScheduledBlock b1 = new Schedule.ScheduledBlock("CS101", start.plusDays(1), "08:00", "09:30", 90);
        b1.setCourseName("Computer Science");
        b1.setPriority(Priority.LOW);

        // Add blocks
        s.addBlock(a1);
        s.addBlock(a2);
        s.addBlock(b1);

        // Add a random metadata / explanation
        s.addExplanation("Valid schedule: non-overlapping blocks within date range.");
        s.addMetadata("fixture", "validSchedule");

        return s;
    }

    /*
     * Returns a schedule that has overlapping blocks on the same day.
     * - startDate = 2025-12-10, endDate = 2025-12-11
     * - two blocks on 2025-12-10 overlap (09:00-10:30 and 10:00-11:00 => overlap)
     * 
     * Running s.validate() should return a ValidationResult with at least one
     * overlapping-block error.
     */
    public static Schedule overlappingSchedule() {
        LocalDate start = LocalDate.of(2025, 12, 10);
        LocalDate end = LocalDate.of(2025, 12, 11);

        Schedule s = new Schedule("Overlap Plan", start, end);

        // Overlapping blocks on the same date
        Schedule.ScheduledBlock b1 = new Schedule.ScheduledBlock("MATH101", start, "09:00", "10:30", 90);
        b1.setCourseName("Mathematics");
        b1.setPriority(Priority.HIGH);

        // Overlaps with b1 because it starts at 10:00 (< b1.endTime 10:30)
        Schedule.ScheduledBlock b2 = new Schedule.ScheduledBlock("PHYS101", start, "10:00", "11:00", 60);
        b2.setCourseName("Physics");
        b2.setPriority(Priority.MEDIUM);

        s.addBlock(b1);
        s.addBlock(b2);

        s.addExplanation("This schedule intentionally contains overlapping blocks (for validation test).");
        s.addMetadata("fixture", "overlappingSchedule");

        return s;
    }

    /**
     * Returns a schedule with blocks outside the schedule date range.
     * - startDate = 2025-12-10, endDate = 2025-12-12
     * - one block before startDate, one block after endDate
     *
     * Running s.validate() should report blocks before/after schedule date errors.
     */
    public static Schedule outsideRangeSchedule() {
        LocalDate start = LocalDate.of(2025, 12, 10);
        LocalDate end = LocalDate.of(2025, 12, 12);

        Schedule s = new Schedule("OutOfRange Plan", start, end);

        // Block before startDate
        Schedule.ScheduledBlock before = new Schedule.ScheduledBlock("HIST101", start.minusDays(1), "09:00", "10:30",
                90);
        before.setCourseName("History");
        before.setPriority(Priority.LOW);

        // Block after endDate
        Schedule.ScheduledBlock after = new Schedule.ScheduledBlock("CHEM101", end.plusDays(1), "13:00", "14:30", 90);
        after.setCourseName("Chemistry");
        after.setPriority(Priority.MEDIUM);

        s.addBlock(before);
        s.addBlock(after);

        s.addExplanation("This schedule intentionally contains blocks outside the plan date range.");
        s.addMetadata("fixture", "outsideRangeSchedule");

        return s;
    }

    /**
     * Returns a schedule that combines multiple validation problems:
     * - overlapping blocks (same day)
     * - a block outside the date range
     *
     * Useful to confirm validate() aggregates multiple errors.
     */
    public static Schedule combineErrorsSchedule() {
        LocalDate start = LocalDate.of(2025, 12, 10);
        LocalDate end = LocalDate.of(2025, 12, 11);

        Schedule s = new Schedule("MultipleErrors Plan", start, end);

        // Overlap on start date
        Schedule.ScheduledBlock b1 = new Schedule.ScheduledBlock("MATH101", start, "09:00", "10:30", 90);
        b1.setCourseName("Mathematics");
        b1.setPriority(Priority.HIGH);

        Schedule.ScheduledBlock b2 = new Schedule.ScheduledBlock("PHYS101", start, "10:00", "11:00", 60);
        b2.setCourseName("Physics");
        b2.setPriority(Priority.MEDIUM);

        // A block before the start date (outside range)
        Schedule.ScheduledBlock before = new Schedule.ScheduledBlock("ENG101", start.minusDays(2), "12:00", "13:00",
                60);
        before.setCourseName("English");
        before.setPriority(Priority.LOW);

        s.addBlock(b1);
        s.addBlock(b2);
        s.addBlock(before);

        s.addExplanation("This schedule contains multiple validation issues: overlap + block outside date range.");
        s.addMetadata("fixture", "combineErrorsSchedule");

        return s;
    }
}
