package com.scheduler.chatbot.service;

import com.scheduler.chatbot.model.PlanSpec;
import com.scheduler.chatbot.model.Schedule;
import com.scheduler.chatbot.parser.DSLParser;
import com.scheduler.chatbot.persistence.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private List<CommandHistoryEntry> commandHistory = new ArrayList<>();

    /**
     * Execute a DSL command and update the current plan
     * Merges new data from command into existing plan state
     */
    public CommandResult executeCommand(String dslCommand) {
        try {
<<<<<<< Updated upstream
            // Step 1: Parse DSL → PlanSpec (contains only data from this command)
            PlanSpec parsedPlan = dslParser.parseCommand(dslCommand);
            
=======
            System.out.println("=== EXECUTE COMMAND START ===");
            System.out.println("Command: " + dslCommand);
            
            // Step 1: Parse DSL → PlanSpec (contains only data from this command)
            PlanSpec parsedPlan = dslParser.parseCommand(dslCommand);
            
            System.out.println("Parsed command type: " + parsedPlan.getCommandType());
            System.out.println("Parsed courses: " + (parsedPlan.getCourses() != null ? parsedPlan.getCourses().size() : "null"));
            System.out.println("Parsed availability: " + (parsedPlan.getAvailability() != null ? parsedPlan.getAvailability().size() : "null"));
            
>>>>>>> Stashed changes
            // Step 1.5: Save command to history (except for SHOW_HISTORY itself)
            String commandType = parsedPlan.getCommandType();
            if (!"SHOW_HISTORY".equals(commandType)) {
                commandHistory.add(new CommandHistoryEntry(
                    LocalDateTime.now(),
                    dslCommand,
                    commandType
                ));
            }

            // Step 2: Initialize currentPlan if null
            if (this.currentPlan == null) {
                this.currentPlan = new PlanSpec();
<<<<<<< Updated upstream
            }

            // Step 3: Handle SHOW_HISTORY - return command history
            if ("SHOW_HISTORY".equals(commandType)) {
                return new CommandResult(true, "Command history retrieved", null, commandHistory);
            }
            
            // Step 4: Merge parsed data into current plan based on command type
=======
                System.out.println("Initialized new currentPlan");
            }
            
            System.out.println("BEFORE merge - currentPlan courses: " + (this.currentPlan.getCourses() != null ? this.currentPlan.getCourses().size() : "null"));
            System.out.println("BEFORE merge - currentPlan availability: " + (this.currentPlan.getAvailability() != null ? this.currentPlan.getAvailability().size() : "null"));

            // Step 3: Handle SHOW_HISTORY - return command history
            if ("SHOW_HISTORY".equals(commandType)) {
                return new CommandResult(true, "Command history retrieved", this.currentPlan, commandHistory);
            }
            
            // Step 4: Handle GENERATE_SCHEDULE
            if ("GENERATE_SCHEDULE".equals(commandType)) {
                System.out.println("Generating schedule...");
                ScheduleResult result = generateSchedule();
                return new CommandResult(result.isSuccess(), result.getMessage(), this.currentPlan, null);
            }
            
            // Step 5: Handle SHOW_SCHEDULE
            if ("SHOW_SCHEDULE".equals(commandType)) {
                String summary = getScheduleSummary();
                return new CommandResult(true, summary, this.currentPlan, null);
            }
            
            // Step 6: Handle LIST_SUBJECTS
            if ("LIST_SUBJECTS".equals(commandType)) {
                if (this.currentPlan.getCourses() == null || this.currentPlan.getCourses().isEmpty()) {
                    return new CommandResult(true, "No subjects added yet.", this.currentPlan, null);
                }
                StringBuilder sb = new StringBuilder("📚 Current Subjects:\n");
                for (PlanSpec.CourseSpec course : this.currentPlan.getCourses()) {
                    sb.append(String.format("- %s: %s priority, %.1f hours\n", 
                        course.getId(), course.getPriority(), course.getWorkloadHours()));
                }
                return new CommandResult(true, sb.toString(), this.currentPlan, null);
            }
            
            // Step 7: Merge parsed data into current plan based on command type
>>>>>>> Stashed changes
            if ("ADD_SUBJECT".equals(commandType)) {
                // Add all subjects from parsed plan
                for (PlanSpec.CourseSpec course : parsedPlan.getCourses()) {
                    this.currentPlan.addCourse(course);
<<<<<<< Updated upstream
                }
=======
                    System.out.println("Added course: " + course.getId() + ", hours: " + course.getWorkloadHours() + ", priority: " + course.getPriority());
                }
                System.out.println("AFTER add - currentPlan courses: " + this.currentPlan.getCourses().size());
                return new CommandResult(true, "Subject added successfully", this.currentPlan, null);
>>>>>>> Stashed changes
            } else if ("SET_AVAILABILITY".equals(commandType)) {
                // Add all availability from parsed plan
                for (Map.Entry<LocalDate, Double> entry : parsedPlan.getAvailability().entrySet()) {
                    this.currentPlan.setAvailability(entry.getKey(), entry.getValue());
<<<<<<< Updated upstream
                }
            } else if ("CLEAR".equals(commandType)) {
                this.currentPlan = new PlanSpec();
            } else {
                // For other commands (generate, show, list, etc.), use the parsed plan
                this.currentPlan = parsedPlan;
            }

            return new CommandResult(true, "Command executed successfully", null, null);
        } catch (Exception e) {
            return new CommandResult(false, "Error: " + e.getMessage(), null, null);
=======
                    System.out.println("Set availability: " + entry.getKey() + " -> " + entry.getValue() + " hours");
                }
                System.out.println("AFTER set - currentPlan availability: " + this.currentPlan.getAvailability().size());
                return new CommandResult(true, "Availability set successfully", this.currentPlan, null);
            } else if ("CLEAR".equals(commandType) || "CLEAR_ALL".equals(commandType) || "CLEAR_SUBJECTS".equals(commandType) || "CLEAR_SCHEDULE".equals(commandType)) {
                this.currentPlan = new PlanSpec();
                this.currentSchedule = null;
                this.commandHistory.clear();
                System.out.println("Cleared all data (plan, schedule, history)");
                return new CommandResult(true, "✅ All data cleared successfully", this.currentPlan, null);
            } else if ("SET_DATE_RANGE".equals(commandType)) {
                // Update date range from parsed plan
                if (parsedPlan.getStartDate() != null) {
                    this.currentPlan.setStartDate(parsedPlan.getStartDate());
                }
                if (parsedPlan.getEndDate() != null) {
                    this.currentPlan.setEndDate(parsedPlan.getEndDate());
                }
                return new CommandResult(true, "Date range set successfully", this.currentPlan, null);
            } else {
                // For other commands (GENERATE, SHOW, LIST, etc.), DON'T modify currentPlan
                // These are query/action commands, not data modification commands
                System.out.println("Query/action command, not modifying currentPlan");
                return new CommandResult(true, "Command executed successfully", this.currentPlan, null);
            }
        } catch (Exception e) {
            System.err.println("ERROR in executeCommand: " + e.getMessage());
            e.printStackTrace();
            return new CommandResult(false, "Error: " + e.getMessage(), this.currentPlan, null);
        } finally {
            System.out.println("=== EXECUTE COMMAND END ===\n");
>>>>>>> Stashed changes
        }
    }

    /**
     * Generate schedule from current plan
     * Auto-saves schedule to JSON file for persistence
     */
    public ScheduleResult generateSchedule() {
        try {
            if (currentPlan == null) {
                return new ScheduleResult(false, "No plan specified", null);
            }
            
            // Debug logging
            System.out.println("DEBUG: Generating schedule...");
            System.out.println("DEBUG: Courses count: " + (currentPlan.getCourses() != null ? currentPlan.getCourses().size() : "null"));
            System.out.println("DEBUG: Availability count: " + (currentPlan.getAvailability() != null ? currentPlan.getAvailability().size() : "null"));

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
        this.commandHistory.clear();
<<<<<<< Updated upstream
=======
    }
    
    /**
     * Get current plan
     */
    public PlanSpec getCurrentPlan() {
        return this.currentPlan;
>>>>>>> Stashed changes
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
        private List<CommandHistoryEntry> commandHistory;

        public CommandResult(boolean success, String message, PlanSpec updatedPlan, List<CommandHistoryEntry> commandHistory) {
            this.success = success;
            this.message = message;
            this.updatedPlan = updatedPlan;
            this.commandHistory = commandHistory;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public PlanSpec getUpdatedPlan() {
            return updatedPlan;
        }

        public List<CommandHistoryEntry> getCommandHistory() {
            return commandHistory;
        }

        public void setCommandHistory(List<CommandHistoryEntry> commandHistory) {
            this.commandHistory = commandHistory;
        }
    }

    /**
     * Represents a single command in the history
     */
    public static class CommandHistoryEntry {
        private LocalDateTime timestamp;
        private String command;
        private String commandType;

        public CommandHistoryEntry(LocalDateTime timestamp, String command, String commandType) {
            this.timestamp = timestamp;
            this.command = command;
            this.commandType = commandType;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getCommandType() {
            return commandType;
        }

        public void setCommandType(String commandType) {
            this.commandType = commandType;
        }

        public String getFormattedTimestamp() {
            return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
