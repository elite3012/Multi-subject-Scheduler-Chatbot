package com.scheduler.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Main Spring Boot Application
 * Web-based UI served from /static
 */
@SpringBootApplication
public class SchedulerChatbotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerChatbotApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("🚀 Multi-Subject Scheduler Chatbot");
        System.out.println("========================================");
        System.out.println("📝 Web UI: http://localhost:8081");
        System.out.println("🔌 API:    http://localhost:8081/api/chatbot");
        System.out.println("========================================\n");
    }

    /**
     * Configure CORS for API endpoints
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }
}
