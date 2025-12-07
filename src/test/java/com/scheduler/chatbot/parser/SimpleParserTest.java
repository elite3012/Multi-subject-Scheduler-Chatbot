package com.scheduler.chatbot.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Simple DSL Parser Test")
public class SimpleParserTest {

    private DSLParser parser;

    @BeforeEach
    public void setUp() {
        parser = new DSLParser();
    }

    @Test
    @DisplayName("Test Valid and Invalid Commands")
    public void testCommands() {
        System.out.println("\n=== Testing Valid Commands ===");

        // Valid commands - should parse successfully
        testCommand("add subject \"Math\" hours 10 priority HIGH", true);
        testCommand("set availability on 2025-12-08 capacity 5 hours", true);
        testCommand("list subjects", true);

        System.out.println("\n=== Testing Invalid Commands ===");

        // Invalid commands - should throw exceptions
        testCommand("", false);
        testCommand("add subject Math hours 10 priority HIGH", false);
        testCommand("add subject \"Math\" hours -5 priority HIGH", false);

        System.out.println("\nAll tests passed!\n");
    }

    private void testCommand(String command, boolean expectedValid) {
        try {
            parser.parseCommand(command);
            if (expectedValid) {
                System.out.println("VALID: " + command);
            } else {
                fail("Should be invalid but parsed successfully: " + command);
            }
        } catch (Exception e) {
            if (!expectedValid) {
                System.out.println("INVALID (as expected): " +
                        (command.isEmpty() ? "<empty>" : command));
            } else {
                fail("Should be valid but failed: " + command +
                        "\n  Error: " + e.getMessage());
            }
        }
    }
}