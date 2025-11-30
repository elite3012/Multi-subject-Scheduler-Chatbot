# Multi-Subject Scheduler Chatbot

A Java-based chatbot application that automatically generates optimized study schedules for multiple subjects using a Domain-Specific Language (DSL) and intelligent priority-based scheduling algorithms.

---

## Table of Contents

1. [Overview](#overview)
2. [Project Structure](#project-structure)
3. [Architecture](#architecture)
4. [How It Works](#how-it-works)
5. [User Interaction Flow](#user-interaction-flow)
6. [DSL Syntax](#dsl-syntax)
7. [Scheduling Algorithm](#scheduling-algorithm)
8. [Data Persistence](#data-persistence)
9. [Setup and Installation](#setup-and-installation)
10. [Usage Examples](#usage-examples)
11. [API Reference](#api-reference)
12. [Development Guide](#development-guide)

---

## Overview

This application helps students plan their study time across multiple subjects by:

- Accepting study requirements via a simple text-based language (DSL)
- Validating input and checking for conflicts
- Generating optimized schedules based on priorities and availability
- Saving schedules for future reference
- Providing explanations for scheduling decisions

**Key Features:**
- Priority-based scheduling (HIGH, MEDIUM, LOW)
- Front-loading algorithm (prioritizes important subjects early)
- 90-minute study blocks with 15-minute breaks
- Automatic persistence (schedules saved as JSON files)
- Dual interface (Swing UI and REST API)

---

## Project Structure

```
Multi-subject-Scheduler-Chatbot/
│
├── grammar/
│   └── SchedulerDSL.g4                 # ANTLR4 grammar definition
│
├── src/main/java/com/scheduler/chatbot/
│   ├── model/                          # Data models
│   │   ├── PlanSpec.java              # Input IR (parsed plan)
│   │   ├── Schedule.java              # Output IR (generated schedule)
│   │   ├── Subject.java               # Subject/course representation
│   │   ├── StudyBlock.java            # Individual study block
│   │   ├── DaySchedule.java           # Daily schedule container
│   │   └── Priority.java              # Priority enumeration
│   │
│   ├── parser/                         # DSL parsing
│   │   └── DSLParser.java             # ANTLR4-based parser
│   │
│   ├── service/                        # Business logic
│   │   ├── SchedulerFacade.java       # Main orchestration layer
│   │   └── SchedulerService.java      # Core scheduling algorithm
│   │
│   ├── persistence/                    # Data storage
│   │   └── ScheduleRepository.java    # JSON file operations
│   │
│   ├── controller/                     # REST API
│   │   └── ChatbotController.java     # HTTP endpoints
│   │
│   ├── ui/                            # User interface
│   │   └── ChatbotUI.java             # Swing desktop UI
│   │
│   └── SchedulerChatbotApplication.java # Application entry point
│
├── src/test/java/                      # Unit tests
├── pom.xml                             # Maven configuration
└── README.md                           # This file
```

**Generated Files (by ANTLR4):**
```
target/generated-sources/antlr4/
├── SchedulerDSLLexer.java             # Tokenizer
├── SchedulerDSLParser.java            # Parser
├── SchedulerDSLVisitor.java           # Visitor interface
└── SchedulerDSLBaseVisitor.java       # Base visitor implementation
```

---

## Architecture

The application follows a layered MVC architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                     VIEW LAYER                              │
│  ┌──────────────────┐        ┌──────────────────┐          │
│  │  ChatbotUI       │        │ ChatbotController│          │
│  │  (Swing)         │        │  (REST API)      │          │
│  └────────┬─────────┘        └────────┬─────────┘          │
└───────────┼──────────────────────────┼────────────────────┘
            │                          │
            └──────────┬───────────────┘
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                  FACADE LAYER                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  SchedulerFacade                                      │  │
│  │  - Orchestrates complete workflow                    │  │
│  │  - Manages application state                         │  │
│  │  - Handles persistence operations                    │  │
│  └────────┬───────────────────────────────────┬─────────┘  │
└───────────┼───────────────────────────────────┼────────────┘
            │                                   │
            ▼                                   ▼
┌─────────────────────────┐      ┌────────────────────────────┐
│    PARSER LAYER         │      │   PERSISTENCE LAYER        │
│  ┌──────────────────┐  │      │  ┌──────────────────────┐  │
│  │  DSLParser       │  │      │  │  ScheduleRepository  │  │
│  │  - Parse DSL     │  │      │  │  - Save/Load JSON    │  │
│  │  - Build IR      │  │      │  │  - List history      │  │
│  └────────┬─────────┘  │      │  └──────────────────────┘  │
└───────────┼────────────┘      └────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│                  SERVICE LAYER                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  SchedulerService                                     │  │
│  │  - Core scheduling algorithm                         │  │
│  │  - Priority-based allocation                         │  │
│  │  - Constraint validation                             │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│                   MODEL LAYER                               │
│  PlanSpec, Schedule, Subject, StudyBlock, Priority         │
└─────────────────────────────────────────────────────────────┘
```

**Data Flow:**

```
User Input (DSL)
    │
    ▼
DSLParser → PlanSpec (IR)
    │
    ▼
Validation
    │
    ▼
SchedulerService → Schedule (IR)
    │
    ▼
Auto-save to JSON
    │
    ▼
Display to User
```

---

## How It Works

### 1. Parsing Phase

**Input:** Raw DSL text command

```
add subject "Mathematics" hours 20 priority HIGH
```

**Process:**
1. Text is fed to `SchedulerDSLLexer` (tokenization)
2. Tokens are parsed by `SchedulerDSLParser` (syntax validation)
3. Parse tree is traversed using Visitor pattern
4. Visitor builds `PlanSpec` object (Intermediate Representation)

**Output:** `PlanSpec` containing structured course data

### 2. Validation Phase

**Input:** `PlanSpec` from parser

**Checks:**
- Date conflicts and overlaps
- Availability vs. workload mismatch
- Invalid priority values
- Missing required fields

**Output:** Validation result (pass/fail with error messages)

### 3. Scheduling Phase

**Input:** Validated `PlanSpec`

**Algorithm Steps:**

1. **Sort courses by priority**
   ```
   HIGH priority courses first
   MEDIUM priority courses second
   LOW priority courses last
   ```

2. **Calculate front-load distribution**
   ```
   HIGH:   60% first half, 40% second half
   MEDIUM: 50% first half, 50% second half
   LOW:    40% first half, 60% second half
   ```

3. **Allocate study blocks**
   - Each block = 90 minutes
   - Break = 15 minutes between blocks
   - Respect daily capacity limits
   - Fill available time slots

4. **Generate explanations**
   - Why each block was scheduled at specific time
   - Priority-based reasoning
   - Constraint satisfaction details

**Output:** `Schedule` object with blocks and metadata

### 4. Persistence Phase

**Input:** Generated `Schedule`

**Process:**
1. Serialize to JSON format
2. Generate filename with timestamp
3. Save to `~/.scheduler-chatbot/schedules/`

**Output:** File path confirmation

---

## User Interaction Flow

### Scenario 1: Creating a New Schedule

```
User opens app
    │
    ▼
[ChatbotUI displays welcome message]
    │
    ▼
User types: "add subject 'Math' hours 20 priority HIGH"
    │
    ▼
DSLParser parses command → PlanSpec updated
    │
    ▼
Chatbot responds: "Subject 'Math' added successfully"
    │
    ▼
User types: "set availability on 2024-12-01 capacity 3 hours"
    │
    ▼
PlanSpec availability updated
    │
    ▼
Chatbot responds: "Availability set for 2024-12-01"
    │
    ▼
User types: "generate schedule"
    │
    ▼
SchedulerService generates schedule
    │
    ▼
Schedule saved to ~/.scheduler-chatbot/schedules/schedule_20241201_143022.json
    │
    ▼
Chatbot displays schedule + save confirmation
```

### Scenario 2: Loading Previous Schedule

```
User opens app (next day)
    │
    ▼
ChatbotUI.loadLatestScheduleOnStartup() executes
    │
    ▼
ScheduleRepository finds latest JSON file
    │
    ▼
Schedule loaded into memory
    │
    ▼
Chatbot displays: "Loaded previous schedule from 2024-12-01"
    │
    ▼
User can view/modify loaded schedule
```

### Scenario 3: Viewing History

```
User types: "show history"
    │
    ▼
SchedulerFacade.listSavedSchedules()
    │
    ▼
ScheduleRepository lists all JSON files
    │
    ▼
Chatbot displays:
  1. schedule_20241201_143022.json (2024-12-01 14:30:22)
  2. schedule_20241130_091015.json (2024-11-30 09:10:15)
  3. schedule_20241129_160045.json (2024-11-29 16:00:45)
    │
    ▼
User types: "load schedule schedule_20241130_091015.json"
    │
    ▼
Schedule loaded and displayed
```

---

## DSL Syntax

The Domain-Specific Language provides simple commands for interacting with the scheduler.

### Grammar Rules

```antlr
program         : statement+ EOF ;

statement       : addSubjectStatement
                | setAvailabilityStatement
                | generateScheduleStatement
                | showScheduleStatement
                | clearStatement ;

addSubjectStatement
    : 'add' 'subject' STRING 'hours' NUMBER 'priority' PRIORITY ;

setAvailabilityStatement
    : 'set' 'availability' 'on' DATE 'capacity' NUMBER 'hours' ;

generateScheduleStatement
    : 'generate' 'schedule' ;

showScheduleStatement
    : 'show' 'schedule' ;

clearStatement
    : 'clear' ('all' | 'subjects' | 'schedule') ;
```

### Command Reference

**Add Subject:**
```
add subject "Mathematics" hours 20 priority HIGH
add subject "Physics" hours 15 priority MEDIUM
add subject "Art" hours 10 priority LOW
```

**Set Availability:**
```
set availability on 2024-12-01 capacity 3 hours
set availability on 2024-12-02 capacity 4 hours
```

**Generate Schedule:**
```
generate schedule
```

**Show Current Schedule:**
```
show schedule
```

**Clear Data:**
```
clear all          # Clear everything
clear subjects     # Clear only subjects
clear schedule     # Clear only generated schedule
```

### Data Types

- `STRING`: Text in quotes (single or double)
  - Examples: `"Mathematics"`, `'Physics'`

- `NUMBER`: Integer or decimal
  - Examples: `20`, `15.5`, `3.0`

- `PRIORITY`: One of three values
  - Values: `HIGH`, `MEDIUM`, `MED`, `LOW`

- `DATE`: Two supported formats
  - YYYY-MM-DD: `2024-12-01`
  - DD/MM/YYYY: `01/12/2024`

---

## Scheduling Algorithm

### Priority System

Each priority level has two parameters:

| Priority | Weight | Front-Load Ratio |
|----------|--------|------------------|
| HIGH     | 1.5    | 60% / 40%       |
| MEDIUM   | 1.2    | 50% / 50%       |
| LOW      | 1.0    | 40% / 60%       |

**Weight:** Used for time allocation in scarce resources
**Front-Load Ratio:** Distribution between first half and second half of available period

### Block Allocation Rules

1. **Standard Block Duration:** 90 minutes (1.5 hours)
2. **Break Duration:** 15 minutes between blocks
3. **Maximum Daily Hours:** 8 hours (configurable)
4. **Maximum Continuous Study:** 3 hours without extended break

### Algorithm Pseudocode

```
function generateSchedule(planSpec):
    courses = planSpec.getCourses()
    availability = planSpec.getAvailability()
    
    # Step 1: Sort by priority
    sortedCourses = sort(courses, by=priority, descending)
    
    # Step 2: Split calendar into halves
    allDays = availability.getAllDays()
    firstHalf = allDays[0 : len(allDays)/2]
    secondHalf = allDays[len(allDays)/2 : end]
    
    # Step 3: Allocate each course
    schedule = new Schedule()
    for course in sortedCourses:
        # Calculate hours per phase
        totalHours = course.getWorkloadHours()
        ratio = course.getPriority().getFrontLoadRatio()
        
        firstHalfHours = totalHours * ratio.first
        secondHalfHours = totalHours * ratio.second
        
        # Schedule first half
        schedulePhase(schedule, course, firstHalf, firstHalfHours)
        
        # Schedule second half
        schedulePhase(schedule, course, secondHalf, secondHalfHours)
    
    return schedule

function schedulePhase(schedule, course, days, hoursNeeded):
    remainingHours = hoursNeeded
    
    for day in days:
        if remainingHours <= 0:
            break
            
        availableCapacity = day.getRemainingCapacity()
        
        while availableCapacity >= BLOCK_DURATION and remainingHours > 0:
            block = new StudyBlock(course, BLOCK_DURATION)
            schedule.addBlock(day, block)
            
            remainingHours -= BLOCK_DURATION
            availableCapacity -= (BLOCK_DURATION + BREAK_DURATION)
```

### Example Calculation

**Input:**
- Course: Mathematics
- Workload: 20 hours
- Priority: HIGH (60/40 split)
- Available days: 10 days (5 first half, 5 second half)
- Daily capacity: 3 hours

**Calculation:**
```
First half allocation:  20 hours × 60% = 12 hours
Second half allocation: 20 hours × 40% = 8 hours

Blocks needed:
- First half:  12 hours ÷ 1.5 hours/block = 8 blocks
- Second half: 8 hours ÷ 1.5 hours/block = 6 blocks (rounded)

Daily distribution (first half):
- 5 days × 3 hours = 15 hours available
- Need 12 hours → Use 4 days fully (12 hours)
- Days used: 4 days
- Remaining capacity: 3 hours on day 5

Schedule:
Day 1: [Math 09:00-10:30, Math 10:45-12:15]
Day 2: [Math 09:00-10:30, Math 10:45-12:15]
Day 3: [Math 09:00-10:30, Math 10:45-12:15]
Day 4: [Math 09:00-10:30, Math 10:45-12:15]
... (second half similar pattern)
```

---

## Data Persistence

### Storage Location

All schedules are saved as JSON files in:
```
Windows: C:\Users\<username>\.scheduler-chatbot\
Mac/Linux: /home/<username>/.scheduler-chatbot/
```

### Directory Structure

```
.scheduler-chatbot/
├── plans/
│   ├── plan_20241201_143022.json
│   └── plan_20241201_150135.json
└── schedules/
    ├── schedule_20241201_143025.json
    ├── schedule_20241201_150140.json
    └── schedule_20241202_091500.json
```

### File Format

**schedule_20241201_143025.json:**
```json
{
  "planName": "Final Exam Preparation",
  "timezone": "Asia/Ho_Chi_Minh",
  "startDate": "2024-12-01",
  "blocks": [
    {
      "date": "2024-12-01",
      "startTime": "09:00",
      "endTime": "10:30",
      "courseName": "Mathematics",
      "component": "Chapter 5 Review",
      "status": "scheduled",
      "locked": false
    },
    {
      "date": "2024-12-01",
      "startTime": "10:45",
      "endTime": "12:15",
      "courseName": "Mathematics",
      "component": "Practice Problems",
      "status": "scheduled",
      "locked": false
    }
  ],
  "score": {
    "peakHoursPerDay": 6.5,
    "bufferRisk": "LOW",
    "totalSoftScore": 85.5
  },
  "explanations": [
    "Mathematics scheduled early due to HIGH priority",
    "Front-loading applied: 60% in first half of period",
    "90-minute blocks with 15-minute breaks maintained",
    "Daily capacity limits respected (max 8 hours/day)"
  ]
}
```

### Automatic Operations

**On Schedule Generation:**
- Schedule is automatically saved with timestamp filename
- No manual save action required

**On App Startup:**
- Latest schedule is automatically loaded
- User sees previous session data immediately

**On History Request:**
- All saved schedules are listed chronologically
- User can select and load any previous schedule

---

## Setup and Installation

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.6 or higher
- Git (for cloning repository)

### Installation Steps

1. **Clone the repository:**
   ```bash
   git clone https://github.com/elite3012/Multi-subject-Scheduler-Chatbot.git
   cd Multi-subject-Scheduler-Chatbot
   ```

2. **Generate ANTLR4 parser:**
   ```bash
   mvn antlr4:antlr4
   ```
   This generates lexer and parser classes from the grammar file.

3. **Build the project:**
   ```bash
   mvn clean install
   ```
   This compiles all source code and runs tests.

4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
   Or run the JAR directly:
   ```bash
   java -jar target/chatbot-1.0.0.jar
   ```

### Verify Installation

After running, you should see:
- Spring Boot startup messages in console
- Swing UI window opens automatically
- Welcome message in chat area
- Data directory created at `~/.scheduler-chatbot/`

---

## Usage Examples

### Example 1: Simple Schedule

**Goal:** Create a study schedule for one subject

```
User: add subject "Mathematics" hours 10 priority HIGH
Bot:  Subject 'Mathematics' added successfully

User: set availability on 2024-12-01 capacity 4 hours
Bot:  Availability set for 2024-12-01

User: set availability on 2024-12-02 capacity 4 hours
Bot:  Availability set for 2024-12-02

User: set availability on 2024-12-03 capacity 4 hours
Bot:  Availability set for 2024-12-03

User: generate schedule
Bot:  Schedule generated successfully
      Saved to: ~/.scheduler-chatbot/schedules/schedule_20241201_143025.json
      
      Day 1 (2024-12-01):
        09:00-10:30 Mathematics
        10:45-12:15 Mathematics
        13:00-14:30 Mathematics (partial)
      
      Day 2 (2024-12-02):
        09:00-10:30 Mathematics
        10:45-12:15 Mathematics
      
      All 10 hours scheduled across 2 days
```

### Example 2: Multiple Subjects with Priorities

```
User: add subject "Mathematics" hours 20 priority HIGH
User: add subject "Physics" hours 15 priority MEDIUM
User: add subject "Literature" hours 10 priority LOW

User: set availability on 2024-12-01 capacity 6 hours
User: set availability on 2024-12-02 capacity 6 hours
User: set availability on 2024-12-03 capacity 6 hours
User: set availability on 2024-12-04 capacity 6 hours
User: set availability on 2024-12-05 capacity 6 hours

User: generate schedule
Bot:  Schedule generated successfully
      
      Mathematics (HIGH priority) scheduled first - front-loaded
      Physics (MEDIUM priority) scheduled second - balanced
      Literature (LOW priority) scheduled last - back-loaded
      
      Total: 45 hours across 5 days (9 hours/day average)
      Saved to: ~/.scheduler-chatbot/schedules/schedule_20241201_150330.json
```

### Example 3: Loading Previous Schedule

```
User: show history
Bot:  Saved schedules:
      1. schedule_20241201_150330.json (2024-12-01 15:03:30)
      2. schedule_20241201_143025.json (2024-12-01 14:30:25)
      3. schedule_20241130_091500.json (2024-11-30 09:15:00)

User: load schedule schedule_20241130_091500.json
Bot:  Schedule loaded successfully
      [displays schedule from Nov 30]
```

---

## API Reference

### REST Endpoints

**Base URL:** `http://localhost:8080/api/chatbot`

#### Execute Command

```http
POST /command
Content-Type: application/json

{
  "command": "add subject 'Math' hours 20 priority HIGH"
}

Response:
{
  "result": "Subject 'Math' added successfully"
}
```

#### Get Current Schedule

```http
GET /schedule

Response: (plain text)
Day 1 (2024-12-01):
  09:00-10:30 Mathematics
  10:45-12:15 Mathematics
...
```

#### List Schedule History

```http
GET /schedules/history

Response:
[
  {
    "path": "/home/user/.scheduler-chatbot/schedules/schedule_20241201_143025.json",
    "filename": "schedule_20241201_143025.json",
    "timestamp": 1701344825000,
    "formattedDate": "2024-12-01 14:30:25"
  }
]
```

#### Load Specific Schedule

```http
POST /schedules/load
Content-Type: application/json

{
  "filepath": "/home/user/.scheduler-chatbot/schedules/schedule_20241201_143025.json"
}

Response:
{
  "result": "Schedule loaded successfully"
}
```

---

## Development Guide

### Building from Source

```bash
# Generate ANTLR4 classes only
mvn antlr4:antlr4

# Compile without tests
mvn clean compile -DskipTests

# Run tests
mvn test

# Package as JAR
mvn package

# Clean all generated files
mvn clean
```

### Project Configuration

**Application Properties** (`src/main/resources/application.properties`):

```properties
# Server Configuration
server.port=8080

# Logging
logging.level.com.scheduler.chatbot=DEBUG

# Scheduler Configuration
scheduler.default.block.duration=90
scheduler.default.break.duration=15
scheduler.max.hours.per.day=8
scheduler.max.continuous.block=3

# Priority Weights
scheduler.priority.low.weight=1.0
scheduler.priority.medium.weight=1.2
scheduler.priority.high.weight=1.5
```

### Adding New DSL Commands

1. **Update grammar** (`grammar/SchedulerDSL.g4`):
   ```antlr
   deleteSubjectStatement
       : 'delete' 'subject' STRING ;
   ```

2. **Regenerate parser**:
   ```bash
   mvn antlr4:antlr4
   ```

3. **Implement visitor method** (`DSLParser.java`):
   ```java
   @Override
   public Object visitDeleteSubjectStatement(Context ctx) {
       String name = ctx.STRING().getText();
       planSpec.removeSubject(name);
       return null;
   }
   ```

### Testing

**Run all tests:**
```bash
mvn test
```

**Run specific test:**
```bash
mvn test -Dtest=SchedulerServiceTest
```

**Test coverage:**
```bash
mvn jacoco:report
# View: target/site/jacoco/index.html
```

---

## Key Design Decisions

### Why ANTLR4?
- Provides robust parsing with clear error messages
- Separates syntax from logic (grammar file is declarative)
- Generates efficient lexer and parser code
- Supports visitor pattern for AST traversal

### Why JSON Files Instead of Database?
- Single-user application (no concurrent access needed)
- Human-readable format (easy debugging)
- Portable (easy to share schedules)
- Zero configuration (no database setup)
- Fast for < 1000 records
- Simple backup (copy folder)

### Why Intermediate Representation (IR)?
- Decouples parsing from execution
- Enables validation before scheduling
- Supports multiple input formats (DSL, JSON, UI forms)
- Easier to test individual components
- Clear contract between layers

### Why Facade Pattern?
- Single entry point for both UIs
- Centralized state management
- Orchestrates complex workflows
- Hides internal complexity from UI layer
- Easier to modify implementation without affecting UI

---

## Troubleshooting

### ANTLR4 Classes Not Found
```
Error: Cannot find symbol SchedulerDSLLexer
```
**Solution:** Run `mvn antlr4:antlr4` to generate parser classes.

### Port Already in Use
```
Error: Port 8080 is already in use
```
**Solution:** Change port in `application.properties` or kill process using port 8080.

### JSON Parsing Error
```
Error: Failed to load schedule
```
**Solution:** Check JSON file format. Ensure all required fields are present.

### Directory Creation Failed
```
Error: Failed to create data directories
```
**Solution:** Check file permissions in home directory. Ensure write access.

---

## License

This project is developed as an educational application for study schedule management.

---

## Contact

For questions or issues, please contact the development team or create an issue in the repository.

---

**Last Updated:** November 30, 2024
**Version:** 1.0.0
