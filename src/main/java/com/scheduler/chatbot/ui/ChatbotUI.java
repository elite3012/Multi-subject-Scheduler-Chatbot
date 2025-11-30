package com.scheduler.chatbot.ui;

import com.scheduler.chatbot.service.SchedulerFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;

/**
 * Swing-based UI for the chatbot
 * Delegates to SchedulerFacade for business logic
 * TODO: Implement Swing UI components and event handlers
 */
@Component
public class ChatbotUI extends JFrame {

    @Autowired
    private SchedulerFacade schedulerFacade;

    // TODO: Add UI components (chatArea, inputField, sendButton)

    public ChatbotUI() {
        initializeUI();
        loadLatestScheduleOnStartup();
    }
    
    /**
     * Load the latest schedule automatically when app starts
     * TODO: Implement this method
     */
    private void loadLatestScheduleOnStartup() {
        // TODO: Call schedulerFacade.loadLatestSchedule()
        // TODO: Display result in chat area
        // Example:
        // SchedulerFacade.LoadResult result = schedulerFacade.loadLatestSchedule();
        // if (result.isSuccess()) {
        //     appendToChatArea("✓ Loaded previous schedule\\n");
        // }
    }

    /**
     * Initialize UI components
     * TODO: Create and layout Swing components
     * Components needed:
     * - JTextArea for chat display
     * - JTextField for user input
     * - JButton for send action
     * - JScrollPane for scrolling
     */
    private void initializeUI() {
        // TODO: Set window properties (title, size, close operation)
        // TODO: Create main panel with BorderLayout
        // TODO: Create and configure chat area
        // TODO: Create and configure input panel
        // TODO: Add welcome message
        // TODO: Add action listeners
    }

    /**
     * Handle send message action
     * TODO: Implement message sending logic
     * Supports commands:
     * - "show history" - List all saved schedules
     * - "load schedule <filename>" - Load specific schedule
     */
    private void sendMessage() {
        // TODO: Get message from input field
        // TODO: Display user message
        // TODO: Handle special commands (show history, load schedule)
        // TODO: Parse and execute DSL command via facade
        // TODO: Display bot response
        // TODO: Clear input field
    }

    /**
     * Append text to chat area
     * TODO: Implement this helper method
     */
    private void appendToChatArea(String text) {
        // TODO: Append text and scroll to bottom
    }

    /**
     * Show the UI window
     * TODO: Make window visible
     */
    public void show() {
        // TODO: Use SwingUtilities.invokeLater to show window
    }
}
