package com.scheduler.chatbot.controller;

import com.scheduler.chatbot.model.PlanSpec;
import com.scheduler.chatbot.persistence.ScheduleRepository;
import com.scheduler.chatbot.service.SchedulerFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for web-based interaction
 * Delegates to SchedulerFacade for business logic
 * TODO: Implement REST endpoints
 */
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Autowired
    private SchedulerFacade schedulerFacade;

    /**
     * Execute a DSL command
     */
    @PostMapping("/command")
    public SchedulerFacade.CommandResult executeCommand(@RequestBody CommandRequest request) {
        return schedulerFacade.executeCommand(request.getCommand());
    }

    /**
     * Get current schedule
     */
    @GetMapping("/schedule")
    public String getSchedule() {
        return schedulerFacade.getScheduleSummary();
    }
    
    /**
     * Get current plan
     */
    @GetMapping("/plan")
    public PlanSpec getCurrentPlan() {
        PlanSpec plan = schedulerFacade.getCurrentPlan();
        // Return empty PlanSpec if null to avoid JSON parsing errors
        return plan != null ? plan : new PlanSpec();
    }
    
    /**
     * List all saved schedules
     */
    @GetMapping("/schedules/history")
    public List<ScheduleRepository.ScheduleFile> listScheduleHistory() {
        return schedulerFacade.listSavedSchedules();
    }
    
    /**
     * Load a specific schedule from file
     */
    @PostMapping("/schedules/load")
    public SchedulerFacade.LoadResult loadSchedule(@RequestBody LoadScheduleRequest request) {
        return schedulerFacade.loadSchedule(request.getFilepath());
    }

    // DTOs
    public static class CommandRequest {
        private String command;

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }
    }


    
    public static class LoadScheduleRequest {
        private String filepath;
        
        public String getFilepath() {
            return filepath;
        }
        
        public void setFilepath(String filepath) {
            this.filepath = filepath;
        }
    }
}
