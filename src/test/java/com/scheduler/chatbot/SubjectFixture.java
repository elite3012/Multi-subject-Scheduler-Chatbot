package com.scheduler.chatbot;

import com.scheduler.chatbot.model.Subject;
import com.scheduler.chatbot.model.Priority;


/**
 * Test fixtures for Subject objects.
 * Produces only valid Subject objects(no validate method for Subject.java).
 */
public final class SubjectFixture {

    private SubjectFixture() { /* static helpers only */ }

    /** High-priority subject with 12 hours estimated. */
    public static Subject highPriorityMath() {
        return new Subject("MATH101", 12.0, Priority.HIGH);
    }

    /** Medium-priority subject with 8 hours estimated. */
    public static Subject mediumPriorityPhysics() {
        return new Subject("PHYS101", 8.0, Priority.MEDIUM);
    }

    /** Low-priority subject with 6 hours estimated. */
    public static Subject lowPriorityHistory() {
        return new Subject("HIST101", 6.0, Priority.LOW);
    }

    /** Subject with zero estimated hours. */
    public static Subject zeroHoursSubject() {
        return new Subject("TRIVIA", 0.0, Priority.LOW);
    }

    /** Small subject (1.5 hours -> exactly one block at 90 minutes). */
    public static Subject tinySubject() {
        return new Subject("LAB01", 1.5, Priority.MEDIUM);
    }

}
