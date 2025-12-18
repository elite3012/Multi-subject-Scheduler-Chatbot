package com.scheduler.chatbot.service;

import com.scheduler.chatbot.model.Priority;
import com.scheduler.chatbot.model.Schedule;
import com.scheduler.chatbot.model.Schedule.ScheduledBlock;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Export Schedule into external formats:
 *  - iCalendar (.ics)
 *  - CSV (.csv)
 * This implementation is aligned with the current model:
 *  - Schedule
 *  - Schedule.ScheduledBlock
 */
@Service
public class ExportService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    // RFC 5545 datetime format (no timezone suffix, TZID is used instead)
    private static final DateTimeFormatter ICS_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");


    /**
     * Export a Schedule into iCalendar (.ics) format.
     * Each ScheduledBlock becomes one VEVENT.
     */
    public String exportToICS(Schedule schedule) {
        StringBuilder sb = new StringBuilder();

        sb.append("BEGIN:VCALENDAR\n");
        sb.append("VERSION:2.0\n");
        sb.append("PRODID:-//Multi-Subject Scheduler Chatbot//EN\n");
        sb.append("CALSCALE:GREGORIAN\n");

        // Required timezone block (Google Calendar compatible)
        sb.append(buildVTimeZone());

        for (ScheduledBlock block : schedule.getBlocks()) {
            sb.append(buildVEvent(block));
        }

        sb.append("END:VCALENDAR\n");
        return sb.toString();
    }

    /**
     * Export a Schedule into CSV format (Excel / Google Sheets compatible).
     */
    public String exportToCSV(Schedule schedule) {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("Date,Course ID,Course Name,Start Time,End Time,Duration (minutes),Priority,Component,Deadline,Reason\n");

        for (ScheduledBlock block : schedule.getBlocks()) {
            sb.append(csv(block.getDate().toString())).append(",");
            sb.append(csv(block.getCourseId())).append(",");
            sb.append(csv(block.getCourseName())).append(",");
            sb.append(csv(block.getStartTime())).append(",");
            sb.append(csv(block.getEndTime())).append(",");
            sb.append(block.getDurationMinutes()).append(",");
            sb.append(csv(priority(block.getPriority()))).append(",");
            sb.append(csv(block.getComponentName())).append(",");
            sb.append(csv(block.getDeadline() != null ? block.getDeadline().toString() : "")).append(",");
            sb.append(csv(block.getReason())).append("\n");
        }

        return sb.toString();
    }

    /* iCalendar helpers*/

    private String buildVEvent(ScheduledBlock block) {
        ZonedDateTime start = toZonedDateTime(block.getDate(), block.getStartTime());
        ZonedDateTime end = toZonedDateTime(block.getDate(), block.getEndTime());

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VEVENT\n");
        sb.append("UID:")
                .append(buildUID(block))
                .append("\n");
        sb.append("DTSTAMP:")
                .append(ZonedDateTime.now(ZONE_ID).format(ICS_FORMAT))
                .append("Z\n");
        sb.append("DTSTART;TZID=Asia/Ho_Chi_Minh:")
                .append(start.format(ICS_FORMAT)).append("\n");
        sb.append("DTEND;TZID=Asia/Ho_Chi_Minh:")
                .append(end.format(ICS_FORMAT)).append("\n");

        sb.append("SUMMARY:")
                .append(escapeICS(buildSummary(block))).append("\n");

        sb.append("DESCRIPTION:")
                .append(escapeICS(buildDescription(block))).append("\n");

        sb.append("END:VEVENT\n");
        return sb.toString();
    }

    private String buildVTimeZone() {
        return "BEGIN:VTIMEZONE\n" +
                "TZID:Asia/Ho_Chi_Minh\n" +
                "BEGIN:STANDARD\n" +
                "DTSTART:19700101T000000\n" +
                "TZOFFSETFROM:+0700\n" +
                "TZOFFSETTO:+0700\n" +
                "TZNAME:ICT\n" +
                "END:STANDARD\n" +
                "END:VTIMEZONE\n";
    }

    private ZonedDateTime toZonedDateTime(LocalDate date, String time) {
        LocalTime lt = LocalTime.parse(time);
        return ZonedDateTime.of(date, lt, ZONE_ID);
    }

    private String buildUID(ScheduledBlock block) {
        return block.getCourseId() + "-" +
                block.getDate() + "-" +
                block.getStartTime() + "@scheduler-chatbot";
    }

    private String buildSummary(ScheduledBlock block) {
        if (block.getCourseName() != null) {
            return block.getCourseName();
        }
        return block.getCourseId();
    }

    private String buildDescription(ScheduledBlock block) {
        StringBuilder sb = new StringBuilder();
        sb.append("Course: ").append(block.getCourseId());
        if (block.getComponentName() != null) {
            sb.append("\nComponent: ").append(block.getComponentName());
        }
        sb.append("\nPriority: ").append(priority(block.getPriority()));
        if (block.getDeadline() != null) {
            sb.append("\nDeadline: ").append(block.getDeadline());
        }
        if (block.getReason() != null) {
            sb.append("\nReason: ").append(block.getReason());
        }
        return sb.toString();
    }

    private String escapeICS(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }

    /* CSV helpers */
    private String csv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    private String priority(Priority p) {
        return p != null ? p.name() : "";
    }
}
