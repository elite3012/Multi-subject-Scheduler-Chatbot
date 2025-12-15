package com.scheduler.chatbot.service;

import com.scheduler.chatbot.model.PlanSpec;
import com.scheduler.chatbot.model.Schedule;
import com.scheduler.chatbot.parser.DSLParser;
import com.scheduler.chatbot.persistence.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Facade Service that orchestrates the complete flow:
 * DSL → Parse → Validate → Schedule → Export
 * Now with persistence support - auto-saves schedules to JSON files
 * 
 * This is the main entry point for both UI and REST API
 */
@Service
public class SchedulerFacade {

    @Autowired
    private DSLParser dslParser;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private ScheduleRepository repository;

    private PlanSpec currentPlan;
    private Schedule currentSchedule;

    /**
     * Execute a DSL command and update the current plan
     * TODO: Implement command execution flow
     */
    public CommandResult executeCommand(String dslCommand) {
        try {
            // Step 1: Parse DSL → PlanSpec
            PlanSpec updatedPlan = dslParser.parseCommand(dslCommand);

            // Step 2: Validate PlanSpec
            PlanSpec.ValidationResult validation = updatedPlan.validate();

            if (!validation.isValid()) {
                return new CommandResult(false, "Error: " + validation.getErrors(), null);
            }

            // Step 3: Update current plan
            this.currentPlan = updatedPlan;

            return new CommandResult(true, "Command executed successfully", null);
        } catch (Exception e) {
            return new CommandResult(false, "Error: " + e.getMessage(), null);
        }
    }

    /**
     * Generate schedule from current plan
     * Auto-saves schedule to JSON file for persistence
     * TODO: Implement scheduling flow
     */
    public ScheduleResult generateSchedule() {
        try {
            if (currentPlan == null) {
                return new ScheduleResult(false, "No plan specified", null);
            }

            // Validate plan
            Schedule schedule = schedulerService.generateSchedule(currentPlan);
            this.currentSchedule = schedule;

            // Auto-save schedule to file
            String filepath = repository.saveSchedule(schedule);
            return new ScheduleResult(true, "Schedule generated and saved to: " + filepath, schedule);
        } catch (Exception e) {
            return new ScheduleResult(false, "Error: " + e.getMessage(), null);
        }
    }

    /**
     * Get current schedule summary
     * TODO: Implement
     */
    public String getScheduleSummary() {
        if (currentSchedule == null) {
            return "No schedule generated yet.";
        }
        return schedulerService.formatSchedule(currentSchedule);
    }

    /**
     * Clear current plan and schedule
     */
    public void clear() {
        this.currentPlan = null;
        this.currentSchedule = null;
    }

    /**
     * List all saved schedules (newest first)
     */
    public List<ScheduleRepository.ScheduleFile> listSavedSchedules() {
        try {
            return repository.listSchedules();
        } catch (IOException e) {
            throw new RuntimeException("Failed to list schedules: " + e.getMessage(), e);
        }
    }

    /**
     * Load a saved schedule from file
     */
    public LoadResult loadSchedule(String filepath) {
        try {
            Schedule schedule = repository.loadSchedule(filepath);
            this.currentSchedule = schedule;
            return new LoadResult(true, "Schedule loaded successfully", schedule);
        } catch (IOException e) {
            return new LoadResult(false, "Failed to load schedule: " + e.getMessage(), null);
        }
    }

    /**
     * Load the most recent schedule automatically
     * Useful for app startup to restore last session
     */
    public LoadResult loadLatestSchedule() {
        try {
            Schedule schedule = repository.getLatestSchedule();
            if (schedule == null) {
                return new LoadResult(false, "No saved schedules found", null);
            }
            this.currentSchedule = schedule;
            return new LoadResult(true, "Latest schedule loaded", schedule);
        } catch (IOException e) {
            return new LoadResult(false, "Failed to load latest schedule: " + e.getMessage(), null);
        }
    }

    /**
     * Delete a saved schedule
     */
    public boolean deleteSchedule(String filepath) {
        try {
            return repository.deleteSchedule(filepath);
        } catch (Exception e) {
            System.err.println("Failed to delete the schedule: " + e.getMessage());
            return false;
        }
    }

    // Result DTOs

    public static class CommandResult {
        private boolean success;
        private String message;
        private PlanSpec updatedPlan;

        public CommandResult(boolean success, String message, PlanSpec updatedPlan) {
            this.success = success;
            this.message = message;
            this.updatedPlan = updatedPlan;
        }

        // TODO: Add getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public PlanSpec getUpdatedPlan() {
            return updatedPlan;
        }
    }

    public static class ScheduleResult {
        private boolean success;
        private String message;
        private Schedule schedule;

        public ScheduleResult(boolean success, String message, Schedule schedule) {
            this.success = success;
            this.message = message;
            this.schedule = schedule;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Schedule getSchedule() {
            return schedule;
        }
    }

    public static class LoadResult {
        private boolean success;
        private String message;
        private Schedule schedule;

        public LoadResult(boolean success, String message, Schedule schedule) {
            this.success = success;
            this.message = message;
            this.schedule = schedule;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Schedule getSchedule() {
            return schedule;
        }
    }
}
