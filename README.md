# Multi-Subject Scheduler Chatbot

An automated study schedule generator using Domain-Specific Language (DSL) for students to plan multiple subjects efficiently.

## Overview

This application generates optimized study schedules based on subject priorities, available time, and workload requirements. It uses ANTLR4 for parsing natural language commands and implements a priority-based front-loading algorithm to distribute study time effectively.

## Features

- Natural language DSL for schedule management
- Priority-based scheduling (HIGH, MEDIUM, LOW)
- Automatic 2-hour study blocks with 15-minute breaks
- JSON persistence for saving and loading schedules
- Web interface and REST API
- Constraint validation and conflict detection

## Requirements

- Java 17 or higher
- Maven 3.8+
- Spring Boot 3.2.0

## Installation

```bash
git clone https://github.com/yourusername/Multi-subject-Scheduler-Chatbot.git
cd Multi-subject-Scheduler-Chatbot
mvn clean install
```

## Running the Application

```bash
mvn spring-boot:run
```

Access the web interface at `http://localhost:8080`

## Usage

### DSL Commands

**Add a subject:**
```
add subject "Mathematics" hours 20 priority HIGH
add subject "Physics" hours 15 priority MEDIUM
```

**Set availability:**
```
set availability on 2025-01-10 capacity 8 hours
set availability on 2025-01-11 capacity 6 hours
```

**Generate schedule:**
```
generate schedule
```

**View and manage:**
```
list subjects
show schedule
delete subject "Mathematics"
update subject "Physics" hours 18
clear all
```

### Example Session

```
add subject "Calculus" hours 20 priority HIGH
add subject "Physics" hours 15 priority MEDIUM
add subject "Chemistry" hours 10 priority LOW
set availability on 2025-01-10 capacity 8 hours
set availability on 2025-01-11 capacity 8 hours
set availability on 2025-01-12 capacity 6 hours
generate schedule
```

### REST API

**Execute command:**
```bash
curl -X POST http://localhost:8080/api/chatbot/command \
  -H "Content-Type: application/json" \
  -d '{"command": "add subject \"Math\" hours 10 priority HIGH"}'
```

**Get schedule:**
```bash
curl http://localhost:8080/api/chatbot/schedule
```

## Project Structure

```
src/main/java/com/scheduler/chatbot/
├── model/          # Domain models (PlanSpec, Schedule, Subject)
├── parser/         # DSL parser using ANTLR4
├── service/        # Business logic and scheduling algorithm
├── controller/     # REST API endpoints
├── persistence/    # JSON file operations
└── ui/             # Swing desktop interface

grammar/
└── SchedulerDSL.g4 # ANTLR4 grammar definition
```

## Algorithm

The scheduler uses a priority-based front-loading strategy:

1. Sorts subjects by priority (HIGH > MEDIUM > LOW)
2. Splits schedule period into first and second halves
3. Allocates 60% of HIGH priority hours in first half
4. Allocates 50% of MEDIUM priority hours in first half
5. Allocates 40% of LOW priority hours in first half
6. Fills time slots greedily while respecting constraints

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- ANTLR4 4.13.1 (parser generator)
- Jackson (JSON serialization)
- JUnit 5 (testing)
- Maven (build tool)

## Data Persistence

Schedules are automatically saved as JSON files in `~/.scheduler-chatbot/schedules/`

## License

MIT License

## Authors

Developed as a Principles of Programming Languages course project.
