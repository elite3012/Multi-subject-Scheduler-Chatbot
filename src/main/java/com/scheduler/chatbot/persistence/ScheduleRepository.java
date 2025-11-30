package com.scheduler.chatbot.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scheduler.chatbot.model.PlanSpec;
import com.scheduler.chatbot.model.Schedule;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for persisting plans and schedules to JSON files
 * Data is stored in user's home directory: ~/.scheduler-chatbot/
 */
@Component
public class ScheduleRepository {
    
    private static final String DATA_DIR = System.getProperty("user.home") + "/.scheduler-chatbot";
    private static final String PLANS_DIR = DATA_DIR + "/plans";
    private static final String SCHEDULES_DIR = DATA_DIR + "/schedules";
    
    private final ObjectMapper objectMapper;
    
    public ScheduleRepository() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Create directories if not exist
        createDirectories();
    }
    
    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(PLANS_DIR));
            Files.createDirectories(Paths.get(SCHEDULES_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create data directories", e);
        }
    }
    
    /**
     * Save a plan to JSON file
     * Filename: plan_YYYYMMDD_HHmmss.json
     */
    public String savePlan(PlanSpec plan) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "plan_" + timestamp + ".json";
        Path filepath = Paths.get(PLANS_DIR, filename);
        
        objectMapper.writeValue(filepath.toFile(), plan);
        return filepath.toString();
    }
    
    /**
     * Save a schedule to JSON file
     * Filename: schedule_YYYYMMDD_HHmmss.json
     */
    public String saveSchedule(Schedule schedule) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "schedule_" + timestamp + ".json";
        Path filepath = Paths.get(SCHEDULES_DIR, filename);
        
        objectMapper.writeValue(filepath.toFile(), schedule);
        return filepath.toString();
    }
    
    /**
     * Load a plan from file
     */
    public PlanSpec loadPlan(String filepath) throws IOException {
        return objectMapper.readValue(new File(filepath), PlanSpec.class);
    }
    
    /**
     * Load a schedule from file
     */
    public Schedule loadSchedule(String filepath) throws IOException {
        return objectMapper.readValue(new File(filepath), Schedule.class);
    }
    
    /**
     * List all saved plans (sorted by date, newest first)
     */
    public List<ScheduleFile> listPlans() throws IOException {
        return listFiles(PLANS_DIR, "plan_");
    }
    
    /**
     * List all saved schedules (sorted by date, newest first)
     */
    public List<ScheduleFile> listSchedules() throws IOException {
        return listFiles(SCHEDULES_DIR, "schedule_");
    }
    
    /**
     * Get the most recent schedule
     */
    public Schedule getLatestSchedule() throws IOException {
        List<ScheduleFile> schedules = listSchedules();
        if (schedules.isEmpty()) {
            return null;
        }
        return loadSchedule(schedules.get(0).getPath());
    }
    
    /**
     * Delete a plan file
     */
    public boolean deletePlan(String filepath) {
        return new File(filepath).delete();
    }
    
    /**
     * Delete a schedule file
     */
    public boolean deleteSchedule(String filepath) {
        return new File(filepath).delete();
    }
    
    /**
     * Helper method to list files in a directory
     */
    private List<ScheduleFile> listFiles(String directory, String prefix) throws IOException {
        Path dir = Paths.get(directory);
        if (!Files.exists(dir)) {
            return new ArrayList<>();
        }
        
        return Files.list(dir)
                .filter(path -> path.getFileName().toString().startsWith(prefix))
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .map(path -> {
                    try {
                        return new ScheduleFile(
                            path.toString(),
                            path.getFileName().toString(),
                            Files.getLastModifiedTime(path).toMillis()
                        );
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(file -> file != null)
                .sorted(Comparator.comparing(ScheduleFile::getTimestamp).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * DTO for file metadata
     */
    public static class ScheduleFile {
        private String path;
        private String filename;
        private long timestamp;
        
        public ScheduleFile(String path, String filename, long timestamp) {
            this.path = path;
            this.filename = filename;
            this.timestamp = timestamp;
        }
        
        public String getPath() {
            return path;
        }
        
        public String getFilename() {
            return filename;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public String getFormattedDate() {
            return LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(timestamp),
                java.time.ZoneId.systemDefault()
            ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }
}
