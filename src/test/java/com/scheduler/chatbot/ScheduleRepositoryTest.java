package com.scheduler.chatbot;

import com.scheduler.chatbot.model.PlanSpec;
import com.scheduler.chatbot.model.Schedule;
import com.scheduler.chatbot.persistence.ScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleRepositoryTest {

    @TempDir
    static Path tempDir;

    static {
        // MUST be done before ScheduleRepository class loads
        System.setProperty("user.home", createTempHome());
    }

    private static String createTempHome() {
        try {
            return Files.createTempDirectory("scheduler-test-home").toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Tests for plan operations removed - plan persistence was removed from ScheduleRepository
    // Only schedule persistence is supported now

    @Test
    public void testListSchedules() throws Exception {
        ScheduleRepository repo = new ScheduleRepository();

        Schedule s1 = new Schedule();
        s1.setPlanName("s1");

        Schedule s2 = new Schedule();
        s2.setPlanName("s2");

        repo.saveSchedule(s1);
        repo.saveSchedule(s2);

        List<ScheduleRepository.ScheduleFile> schedules = repo.listSchedules();
        assertEquals(2, schedules.size());
    }

    @Test
    public void testDeleteSchedule() throws Exception {
        ScheduleRepository repo = new ScheduleRepository();

        Schedule s = new Schedule();
        s.setPlanName("del");

        String path = repo.saveSchedule(s);
        assertTrue(repo.deleteSchedule(path));
        assertFalse(Files.exists(Path.of(path)));
    }
}
