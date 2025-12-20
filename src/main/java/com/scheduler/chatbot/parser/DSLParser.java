package com.scheduler.chatbot.parser;

import com.scheduler.chatbot.model.PlanSpec;
import com.scheduler.chatbot.model.Priority;
import org.antlr.v4.runtime.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for DSL commands using ANTLR4
 * Responsibility: Parse DSL text → PlanSpec (IR) only
 * Does NOT execute commands or hold state
 */
@Component
public class DSLParser {

    private static final DateTimeFormatter DATE_FORMAT_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMAT_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DSLParser() {
        // Stateless parser - no initialization needed
    }

    /**
     * Parse DSL command and return PlanSpec (IR)
     * Steps:
     * 1. Create CharStream from command string
     * 2. Create SchedulerDSLLexer
     * 3. Create CommonTokenStream
     * 4. Create SchedulerDSLParser
     * 5. Parse and build PlanSpec using Visitor pattern
     * 6. Return PlanSpec (do NOT execute)
     */
    public PlanSpec parseCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            throw new ParseException("Command cannot be empty");
        }

        try {
            // Step 1: Create CharStream from command string
            CharStream charStream = CharStreams.fromString(command);

            // Step 2: Create SchedulerDSLLexer
            SchedulerDSLLexer lexer = new SchedulerDSLLexer(charStream);

            // Remove default error listeners and add custom one
            lexer.removeErrorListeners();
            CustomErrorListener lexerErrorListener = new CustomErrorListener("Lexer");
            lexer.addErrorListener(lexerErrorListener);

            // Step 3: Create CommonTokenStream
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // Step 4: Create SchedulerDSLParser
            SchedulerDSLParser parser = new SchedulerDSLParser(tokens);

            // Remove default error listeners and add custom one
            parser.removeErrorListeners();
            CustomErrorListener parserErrorListener = new CustomErrorListener("Parser");
            parser.addErrorListener(parserErrorListener);

            // Step 5: Parse and build PlanSpec using Visitor pattern
            SchedulerDSLParser.ProgramContext programContext = parser.program();

            // Check for syntax errors
            if (lexerErrorListener.hasErrors() || parserErrorListener.hasErrors()) {
                List<String> allErrors = new ArrayList<>();
                allErrors.addAll(lexerErrorListener.getErrors());
                allErrors.addAll(parserErrorListener.getErrors());
                throw new ParseException("Syntax errors found:\n" + String.join("\n", allErrors));
            }

            // Step 6: Visit AST and build PlanSpec
            PlanSpecVisitor visitor = new PlanSpecVisitor();
            visitor.visit(programContext);

            PlanSpec planSpec = visitor.getPlanSpec();

            // DON'T validate here - DSLParser only parses individual commands
            // Validation happens in SchedulerFacade where we have the complete currentPlan
            // The parsed planSpec here may be incomplete (e.g., just commandType for "generate schedule")

            return planSpec;

        } catch (RecognitionException e) {
            throw new ParseException("Recognition error: " + e.getMessage(), e);
        } catch (ParseException | ValidationException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            throw new ParseException("Unexpected error parsing command: " + e.getMessage(), e);
        }
    }

    /**
     * Custom error listener for better error messages
     */
    private static class CustomErrorListener extends BaseErrorListener {
        private final List<String> errors = new ArrayList<>();
        private final String source;

        public CustomErrorListener(String source) {
            this.source = source;
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                int line, int charPositionInLine,
                String msg, RecognitionException e) {
            String error = String.format("[%s] Line %d:%d - %s",
                    source, line, charPositionInLine, msg);
            errors.add(error);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    /**
     * Visitor to build PlanSpec from AST
     * Traverses the parse tree and constructs a PlanSpec object
     */
    private static class PlanSpecVisitor extends SchedulerDSLBaseVisitor<Object> {
        private final PlanSpec planSpec = new PlanSpec();

        @Override
        public Object visitProgram(SchedulerDSLParser.ProgramContext ctx) {
            // Visit all statements in the program
            return visitChildren(ctx);
        }

        @Override
        public Object visitAddSubjectStatement(SchedulerDSLParser.AddSubjectStatementContext ctx) {
            try {
                String subjectName = stripQuotes(ctx.subjectName.getText());
                int hours = Integer.parseInt(ctx.hours.getText());
                Priority priority = parsePriority(ctx.priority.getText());

                // Validate inputs
                if (subjectName.isEmpty()) {
                    throw new ParseException("Subject name cannot be empty");
                }
                if (hours <= 0) {
                    throw new ParseException("Hours must be positive, got: " + hours);
                }

                // Create CourseSpec and add to PlanSpec
                PlanSpec.CourseSpec courseSpec = new PlanSpec.CourseSpec();
                courseSpec.setId(subjectName);
                courseSpec.setPriority(priority);
                courseSpec.setWorkloadHours(hours);

                planSpec.addCourse(courseSpec);
                planSpec.setCommandType("ADD_SUBJECT");

            } catch (NumberFormatException e) {
                throw new ParseException("Invalid number format for hours: " + ctx.hours.getText());
            } catch (IllegalArgumentException e) {
                throw new ParseException("Invalid priority: " + ctx.priority.getText() +
                        ". Valid values are: HIGH, MEDIUM, MED, LOW");
            }

            return null;
        }

        @Override
        public Object visitSetAvailabilityStatement(SchedulerDSLParser.SetAvailabilityStatementContext ctx) {
            try {
                LocalDate date = parseDate(ctx.date.getText());
                double capacity = Double.parseDouble(ctx.capacity.getText());

                if (capacity <= 0) {
                    throw new ParseException("Capacity must be positive, got: " + capacity);
                }

                // Add to availability map
                planSpec.setAvailability(date, capacity);
                planSpec.setCommandType("SET_AVAILABILITY");

            } catch (NumberFormatException e) {
                throw new ParseException("Invalid number format for capacity: " + ctx.capacity.getText());
            } catch (DateTimeParseException e) {
                throw new ParseException("Invalid date format: " + ctx.date.getText() +
                        ". Expected formats: YYYY-MM-DD or DD/MM/YYYY");
            }

            return null;
        }

        @Override
        public Object visitGenerateScheduleStatement(SchedulerDSLParser.GenerateScheduleStatementContext ctx) {
            planSpec.setCommandType("GENERATE_SCHEDULE");
            return null;
        }

        @Override
        public Object visitShowScheduleStatement(SchedulerDSLParser.ShowScheduleStatementContext ctx) {
            planSpec.setCommandType("SHOW_SCHEDULE");
            return null;
        }

        @Override
        public Object visitClearStatement(SchedulerDSLParser.ClearStatementContext ctx) {
            String clearType = ctx.getChild(1).getText(); // 'all', 'subjects', or 'schedule'
            planSpec.setCommandType("CLEAR_" + clearType.toUpperCase());
            return null;
        }

        @Override
        public Object visitDeleteSubjectStatement(SchedulerDSLParser.DeleteSubjectStatementContext ctx) {
            String targetSubject = stripQuotes(ctx.STRING().getText());
            if (targetSubject.isEmpty()) {
                throw new ParseException("Subject name cannot be empty");
            }
            planSpec.setTargetSubject(targetSubject);
            planSpec.setCommandType("DELETE_SUBJECT");
            return null;
        }

        @Override
        public Object visitListSubjectsStatement(SchedulerDSLParser.ListSubjectsStatementContext ctx) {
            planSpec.setCommandType("LIST_SUBJECTS");
            return null;
        }

        @Override
        public Object visitListAvailabilityStatement(SchedulerDSLParser.ListAvailabilityStatementContext ctx) {
            planSpec.setCommandType("LIST_AVAILABILITY");
            return null;
        }

        @Override
        public Object visitUpdateSubjectStatement(SchedulerDSLParser.UpdateSubjectStatementContext ctx) {
            try {
                String targetSubject = stripQuotes(ctx.STRING().getText());
                int updateHours = Integer.parseInt(ctx.NUMBER().getText());

                if (targetSubject.isEmpty()) {
                    throw new ParseException("Subject name cannot be empty");
                }
                if (updateHours <= 0) {
                    throw new ParseException("Hours must be positive, got: " + updateHours);
                }

                planSpec.setTargetSubject(targetSubject);
                planSpec.setUpdateHours(updateHours);
                planSpec.setCommandType("UPDATE_SUBJECT_HOURS");

            } catch (NumberFormatException e) {
                throw new ParseException("Invalid number format for hours: " + ctx.NUMBER().getText());
            }

            return null;
        }

        @Override
        public Object visitUpdatePriorityStatement(SchedulerDSLParser.UpdatePriorityStatementContext ctx) {
            try {
                String targetSubject = stripQuotes(ctx.STRING().getText());
                Priority updatePriority = parsePriority(ctx.PRIORITY().getText());

                if (targetSubject.isEmpty()) {
                    throw new ParseException("Subject name cannot be empty");
                }

                planSpec.setTargetSubject(targetSubject);
                planSpec.setUpdatePriority(updatePriority);
                planSpec.setCommandType("UPDATE_SUBJECT_PRIORITY");

            } catch (IllegalArgumentException e) {
                throw new ParseException("Invalid priority: " + ctx.PRIORITY().getText() +
                        ". Valid values are: HIGH, MEDIUM, MED, LOW");
            }

            return null;
        }

        @Override
        public Object visitShowHistoryStatement(SchedulerDSLParser.ShowHistoryStatementContext ctx) {
            planSpec.setCommandType("SHOW_HISTORY");
            return null;
        }

        @Override
        public Object visitLoadScheduleStatement(SchedulerDSLParser.LoadScheduleStatementContext ctx) {
            String targetSchedulePath = stripQuotes(ctx.STRING().getText());
            if (targetSchedulePath.isEmpty()) {
                throw new ParseException("Schedule file path cannot be empty");
            }
            planSpec.setTargetSchedulePath(targetSchedulePath);
            planSpec.setCommandType("LOAD_SCHEDULE");
            return null;
        }

        /**
         * Get the constructed PlanSpec with all metadata
         */
        public PlanSpec getPlanSpec() {
            return planSpec;
        }

        /**
         * Parse date from string, supporting both formats
         */
        private LocalDate parseDate(String dateStr) {
            try {
                // Try YYYY-MM-DD format first
                return LocalDate.parse(dateStr, DATE_FORMAT_YYYY_MM_DD);
            } catch (DateTimeParseException e1) {
                try {
                    // Try DD/MM/YYYY format
                    return LocalDate.parse(dateStr, DATE_FORMAT_DD_MM_YYYY);
                } catch (DateTimeParseException e2) {
                    throw new ParseException("Invalid date format: " + dateStr +
                            ". Expected YYYY-MM-DD or DD/MM/YYYY");
                }
            }
        }

        /**
         * Parse priority, handling MED as MEDIUM
         */
        private Priority parsePriority(String priorityStr) {
            if ("MED".equalsIgnoreCase(priorityStr)) {
                return Priority.MEDIUM;
            }
            return Priority.valueOf(priorityStr.toUpperCase());
        }

        /**
         * Remove surrounding quotes from string
         */
        private String stripQuotes(String str) {
            if (str == null) {
                return "";
            }
            String trimmed = str.trim();
            if (trimmed.length() >= 2 &&
                    trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                return trimmed.substring(1, trimmed.length() - 1);
            }
            return trimmed;
        }
    }

    /**
     * Custom exception for parse errors
     */
    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Custom exception for validation errors
     */
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}