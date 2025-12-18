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

    @Test
    public void testListAndDeleteSchedules() throws Exception {
        ScheduleRepository repo = new ScheduleRepository();

        Path scheduleDir = Path.of(System.getProperty("user.home"),
                ".scheduler-chatbot", "schedules");
        Files.createDirectories(scheduleDir);

        Files.writeString(scheduleDir.resolve("schedule_20250101_000000_1.json"),
                "{\"planName\":\"A\"}");
        Files.writeString(scheduleDir.resolve("schedule_20250102_000000_2.json"),
                "{\"planName\":\"B\"}");

        List<ScheduleRepository.ScheduleFile> schedules = repo.listSchedules();
        assertEquals(2, schedules.size());

        assertTrue(repo.deleteSchedule(schedules.get(0).getPath()));
        assertEquals(1, repo.listSchedules().size());
    }

    @Test
    public void testSaveScheduleCreatesFile() throws Exception {
        ScheduleRepository repo = new ScheduleRepository();

        Schedule schedule = new Schedule();
        schedule.setPlanName("test-plan");

        String path = repo.saveSchedule(schedule);
        assertTrue(Files.exists(Path.of(path)), "Saved schedule file should exist at returned path");
    }

    @Test
    public void testListSchedules() throws Exception {
        //remove existing files so test is deterministic
        Path schedulesDir = Path.of(System.getProperty("user.home"),
                ".scheduler-chatbot", "schedules");
        try (var stream = Files.list(schedulesDir)) {
        stream.forEach(p -> {
            try { Files.deleteIfExists(p); } catch (Exception ignored) {}
        });
    }
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
