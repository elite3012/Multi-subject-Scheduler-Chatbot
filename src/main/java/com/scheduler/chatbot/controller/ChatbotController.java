package com.scheduler.chatbot.controller;

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
    public CommandResponse executeCommand(@RequestBody CommandRequest request) {
        SchedulerFacade.CommandResult result = schedulerFacade.executeCommand(request.getCommand());
        return new CommandResponse(result.getMessage());
    }

    /**
     * Get current schedule
     */
    @GetMapping("/schedule")
    public String getSchedule() {
        return schedulerFacade.getScheduleSummary();
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
    public CommandResponse loadSchedule(@RequestBody LoadScheduleRequest request) {
        SchedulerFacade.LoadResult result = schedulerFacade.loadSchedule(request.getFilepath());
        return new CommandResponse(result.getMessage());
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

    public static class CommandResponse {
        private String result;

        public CommandResponse(String result) {
            this.result = result;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
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
