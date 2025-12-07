package com.scheduler.chatbot.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a day in the calendar with available capacity
 * TODO: Implement day schedule properties and methods
 */
public class DaySchedule {
    // TODO: Add fields (date, capacity, studyBlocks)
    private LocalDate date;
    private double capacity; // hours available that day
    private final List<StudyBlock> studyBlocks = new ArrayList<>();

    // TODO: Add constructors
    public DaySchedule() {
    }

    public DaySchedule(LocalDate date, double capacity) {
        this.date = date;
        this.capacity = capacity;
    }

    // TODO: Add getters/setters
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public List<StudyBlock> getStudyBlocks() {
        return Collections.unmodifiableList(studyBlocks);
    }

    /**
     * Get remaining capacity for this day
     * TODO: Implement this method
     */
    public double getRemainingCapacity() {
        double remaining = capacity - getScheduledHours();
        return Math.max(0.0, Math.round(remaining * 100.0) / 100.0);
    }

    /**
     * Check if a block can be added to this day
     * TODO: Implement this method
     */
    public boolean canAddBlock(double duration) {
        if (duration <= 0) return false;
        return getRemainingCapacity() + 1e-9 >= duration;
    }

    /**
     * Add a study block to this day
     * TODO: Implement this method
     */
    public void addBlock(StudyBlock block) {
        if (block == null) throw new IllegalArgumentException("block required");
        if (!canAddBlock(block.getDuration())) {
            throw new IllegalArgumentException("Not enough capacity to add block");
        }
        studyBlocks.add(block);
    }

    /**
     * Get total scheduled hours for this day
     * TODO: Implement this method
     */
    public double getScheduledHours() {
        double sum = 0.0;
        for (StudyBlock b : studyBlocks) {
            sum += b.getDuration();
        }
        return Math.round(sum * 100.0) / 100.0;
    }

    public boolean removeBlock(StudyBlock block) {
        return studyBlocks.remove(block);
    }

    @Override
    public String toString() {
        return "DaySchedule{" +
                "date=" + date +
                ", capacity=" + capacity +
                ", scheduledHours=" + getScheduledHours() +
                ", remaining=" + getRemainingCapacity() +
                '}';
    }
}
