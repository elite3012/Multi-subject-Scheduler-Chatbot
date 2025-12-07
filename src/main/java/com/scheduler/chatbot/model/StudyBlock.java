package com.scheduler.chatbot.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a study block for a subject
 * TODO: Implement study block properties and methods
 */
public class StudyBlock {
    // TODO: Add fields (subject, duration, startTime, endTime, isBreakIncluded)
    private Subject subject;
    private double duration; // hours
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isBreakIncluded;

    // TODO: Add constructors
    public StudyBlock(Subject subject, double duration, LocalTime startTime, boolean isBreakIncluded) {
        if (subject == null) throw new IllegalArgumentException("subject required");
        if (duration <= 0) throw new IllegalArgumentException("duration must be positive");
        if (startTime == null) throw new IllegalArgumentException("startTime required");
        this.subject = subject;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = startTime.plusMinutes((long) (duration * 60));
        this.isBreakIncluded = isBreakIncluded;
    }

    public StudyBlock(Subject subject, double duration, LocalTime startTime, LocalTime endTime, boolean isBreakIncluded) {
        if (subject == null) throw new IllegalArgumentException("subject required");
        if (duration <= 0) throw new IllegalArgumentException("duration must be positive");
        if (startTime == null || endTime == null) throw new IllegalArgumentException("start and end required");
        long minutes = Duration.between(startTime, endTime).toMinutes();
        if (minutes <= 0) throw new IllegalArgumentException("endTime must be after startTime");
        double computedHours = minutes / 60.0;
        if (Math.abs(computedHours - duration) > 0.01) {
            throw new IllegalArgumentException("duration does not match start/end times");
        }
        this.subject = subject;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isBreakIncluded = isBreakIncluded;
    }

    // TODO: Add getters/setters
    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        if (subject == null) throw new IllegalArgumentException("subject required");
        this.subject = subject;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        if (duration <= 0) throw new IllegalArgumentException("duration must be positive");
        this.duration = duration;
        this.endTime = this.startTime.plusMinutes((long) (duration * 60));
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        if (startTime == null) throw new IllegalArgumentException("startTime required");
        this.startTime = startTime;
        this.endTime = startTime.plusMinutes((long) (duration * 60));
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        if (endTime == null) throw new IllegalArgumentException("endTime required");
        long minutes = Duration.between(this.startTime, endTime).toMinutes();
        if (minutes <= 0) throw new IllegalArgumentException("endTime must be after startTime");
        this.endTime = endTime;
        this.duration = minutes / 60.0;
    }

    public boolean isBreakIncluded() {
        return isBreakIncluded;
    }

    public void setBreakIncluded(boolean breakIncluded) {
        isBreakIncluded = breakIncluded;
    }

    // TODO: Override toString() method
    @Override
    public String toString() {
        return "StudyBlock{" +
                "subject=" + (subject != null ? subject.getName() : "null") +
                ", duration=" + duration +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", isBreakIncluded=" + isBreakIncluded +
                '}';
    }

    // equals() and hashCode() for comparison
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StudyBlock that = (StudyBlock) o;
        return Double.compare(that.duration, duration) == 0 &&
                isBreakIncluded == that.isBreakIncluded &&
                Objects.equals(subject != null ? subject.getName() : null, that.subject != null ? that.subject.getName() : null) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject != null ? subject.getName() : null, duration, startTime, endTime, isBreakIncluded);
    }
}
