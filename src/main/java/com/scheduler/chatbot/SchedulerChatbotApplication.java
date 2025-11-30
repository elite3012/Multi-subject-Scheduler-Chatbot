package com.scheduler.chatbot;

import com.scheduler.chatbot.ui.ChatbotUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main Spring Boot Application
 */
@SpringBootApplication
public class SchedulerChatbotApplication {

    public static void main(String[] args) {
        // Start Spring Boot application
        ConfigurableApplicationContext context = SpringApplication.run(SchedulerChatbotApplication.class, args);

        // Launch Swing UI
        ChatbotUI ui = context.getBean(ChatbotUI.class);
        ui.show();
    }
}
