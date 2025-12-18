package com.scheduler.chatbot;

import com.scheduler.chatbot.model.Schedule;
import com.scheduler.chatbot.service.ExportService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
//Generate .ics and .csv files to import in Google Calendar, Outlook, Excel, Google Sheets
//Run this class to get sample_schedule.ics and sample_schedule.csv
public class ExportSampleRunner {

    public static void main(String[] args) throws IOException {
        ExportService exportService = new ExportService();
        Schedule schedule = ScheduleFixture.validSchedule();

        // Export ICS
        String ics = exportService.exportToICS(schedule);
        Files.writeString(Path.of("sample_schedule.ics"), ics);

        // Export CSV
        String csv = exportService.exportToCSV(schedule);
        Files.writeString(Path.of("sample_schedule.csv"), csv);

        System.out.println("Sample export files generated:");
        System.out.println(" - sample_schedule.ics");
        System.out.println(" - sample_schedule.csv");
    }
}
