package com.scheduler.chatbot;

import com.scheduler.chatbot.model.Schedule;
import com.scheduler.chatbot.service.ExportService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;



public class ExportServiceTest {
    private Schedule createSampleSchedule() {
        Schedule schedule = new Schedule("Test Plan",
                LocalDate.of(2025, 1, 10),
                LocalDate.of(2025, 1, 15));

        Schedule.ScheduledBlock block1 = new Schedule.ScheduledBlock();
        block1.setCourseId("CS101");
        block1.setCourseName("Intro to CS");
        block1.setDate(LocalDate.of(2025, 1, 10));
        block1.setStartTime("09:00");
        block1.setEndTime("10:30");
        block1.setDurationMinutes(90);

        Schedule.ScheduledBlock block2 = new Schedule.ScheduledBlock();
        block2.setCourseId("MATH202");
        block2.setCourseName("Linear Algebra");
        block2.setDate(LocalDate.of(2025, 1, 11));
        block2.setStartTime("14:00");
        block2.setEndTime("15:00");
        block2.setDurationMinutes(60);

        schedule.addBlock(block1);
        schedule.addBlock(block2);

        return schedule;
    }
    @Test
    void exportToICS_shouldGenerateValidICS() {
        ExportService exportService = new ExportService();
        Schedule schedule = createSampleSchedule();

        String ics = exportService.exportToICS(schedule);

        assertNotNull(ics);
        assertTrue(ics.contains("BEGIN:VCALENDAR"));
        assertTrue(ics.contains("BEGIN:VEVENT"));
        assertTrue(ics.contains("SUMMARY:Intro to CS"));
        assertTrue(ics.contains("END:VCALENDAR"));
    }
    @Test
    void exportToCSV_shouldGenerateValidCSV() {
        ExportService exportService = new ExportService();
        Schedule schedule = ScheduleFixture.validSchedule();
        String csv = exportService.exportToCSV(schedule);
        assertNotNull(csv);
        assertTrue(csv.startsWith("Date,Course ID,Course Name,Start Time,End Time,Duration (minutes),Priority,Component,Deadline,Reason"));
        assertTrue(csv.contains("2025-12-10,MATH101,Mathematics,09:00,10:00,60,HIGH,,,"));
    }

    @Test
    void exportToCSV_shouldEscapeSpecialCharacters() {
        ExportService exportService = new ExportService();
        Schedule schedule = new Schedule("Plan", LocalDate.now(), LocalDate.now());

        Schedule.ScheduledBlock block = new Schedule.ScheduledBlock();
        block.setCourseId("ENG101");
        block.setCourseName("English, Literature \"Advanced\"");
        block.setDate(LocalDate.now());
        block.setStartTime("10:00");
        block.setEndTime("11:00");
        block.setDurationMinutes(60);

        schedule.addBlock(block);

        String csv = exportService.exportToCSV(schedule);

        assertTrue(csv.contains("\"English, Literature \"\"Advanced\"\"\""));
    }
}
