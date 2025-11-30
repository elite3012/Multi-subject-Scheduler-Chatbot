# Work Distribution Plan - Multi-Subject Scheduler Chatbot

## Team Members

1. **Q** - Team Leader (Expert level, all-around skills)
2. **M** - Grammar Specialist (Strong logic, ANTLR4 expert)
3. **D** - Full-stack Developer (Good level, AI-assisted)
4. **K** - Junior Developer (Average level)

---

## Project Timeline: 3 Weeks

### Week 1: Foundation & Core Models
**Goal:** Setup project structure, implement data models, and basic DSL parsing

### Week 2: Business Logic & Scheduling Algorithm
**Goal:** Implement scheduling service, persistence layer, and backend integration

### Week 3: UI, Integration & Testing
**Goal:** Build user interfaces, connect all components, testing and documentation

---

## Detailed Work Distribution

---

## WEEK 1: Foundation & Core Models (Nov 30 - Dec 6)

### Q (Leader) - Week 1 Tasks

**1. Project Setup & Architecture (Day 1-2)**
- [x] Initialize Maven project structure
- [x] Configure pom.xml with all dependencies
- [x] Setup Spring Boot configuration
- [x] Create package structure (model, service, parser, controller, ui, persistence)
- [x] Setup Git repository and .gitignore
- [x] Create ARCHITECTURE.md documentation

**2. Model Layer - PlanSpec.java (Day 3-4)**
- [ ] Implement PlanSpec class (IR for input)
  - CourseSpec inner class
  - ComponentSpec inner class
  - SchedulingRules inner class
  - SoftPreferences inner class
- [ ] Implement validate() method with comprehensive validation logic
- [ ] Add getters/setters with proper encapsulation
- [ ] Write unit tests for validation logic

**3. Model Layer - Schedule.java (Day 5-6)**
- [ ] Enhance Schedule class (IR for output)
  - blocks[] list
  - score{} object with metrics
  - explanations[] list
- [ ] Implement addBlock(), removeBlock() methods
- [ ] Implement score calculation helpers
- [ ] Add JSON serialization annotations
- [ ] Write unit tests for Schedule operations

**4. Integration & Support (Day 7)**
- [ ] Review all team members' code
- [ ] Fix integration issues
- [ ] Setup CI/CD pipeline (optional)
- [ ] Prepare for Week 2 tasks

**Estimated Effort:** 40 hours

---

### M (Grammar Specialist) - Week 1 Tasks

**1. ANTLR4 Grammar Design (Day 1-3)**
- [ ] Design comprehensive DSL grammar in SchedulerDSL.g4
  - Extend current grammar with more commands
  - Add error productions for better error messages
  - Add comments and documentation in grammar
- [ ] Define lexer rules for all token types
- [ ] Define parser rules for all statement types
- [ ] Test grammar with ANTLR4 TestRig

**New Commands to Add:**
```antlr
// Delete operations
deleteSubjectStatement : 'delete' 'subject' STRING ;

// List operations
listSubjectsStatement : 'list' 'subjects' ;
listAvailabilityStatement : 'list' 'availability' ;

// Update operations
updateSubjectStatement : 'update' 'subject' STRING 'hours' NUMBER ;
updatePriorityStatement : 'update' 'subject' STRING 'priority' PRIORITY ;

// History operations
showHistoryStatement : 'show' 'history' ;
loadScheduleStatement : 'load' 'schedule' STRING ;
```

**2. DSLParser Implementation (Day 4-6)**
- [ ] Implement PlanSpecVisitor class
  - Override all visit methods
  - Build PlanSpec from AST
  - Handle error cases gracefully
- [ ] Implement DSLParser.parseCommand() method
  - Create CharStream, Lexer, Parser pipeline
  - Integrate visitor to build PlanSpec
  - Add error handling with clear messages
- [ ] Write comprehensive parser tests
  - Test each command type
  - Test error cases
  - Test edge cases (empty input, malformed syntax)

**3. Error Handling (Day 7)**
- [ ] Implement custom error listener
- [ ] Add line/column error reporting
- [ ] Create user-friendly error messages
- [ ] Test with various invalid inputs

**Estimated Effort:** 40 hours

---

### D (Full-stack Developer) - Week 1 Tasks

**1. Model Layer - Basic Domain Models (Day 1-3)**
- [ ] Implement Priority.java enum
  - Add weight field
  - Add frontLoadRatio field
  - Implement getWeight() method
  - Implement getFrontLoadRatio() method
  - Implement fromString() method
  
**Priority Values:**
```java
LOW(1.0, 0.4, 0.6)     // weight, firstHalfRatio, secondHalfRatio
MEDIUM(1.2, 0.5, 0.5)
HIGH(1.5, 0.6, 0.4)
```

- [ ] Implement Subject.java
  - Fields: name, estimatedHours, priority, remainingHours
  - Constructor and getters/setters
  - getFirstHalfHours() method
  - getSecondHalfHours() method
  - getTotalBlocks() method
- [ ] Write unit tests for Priority and Subject

**2. Model Layer - StudyBlock.java (Day 4-5)**
- [ ] Implement StudyBlock class
  - Fields: subject, duration, startTime, endTime, isBreakIncluded
  - Constructor with validation
  - Getters/setters
  - toString() method for display
  - equals() and hashCode() for comparison
- [ ] Write unit tests for StudyBlock

**3. Model Layer - DaySchedule.java (Day 6-7)**
- [ ] Implement DaySchedule class
  - Fields: date, capacity, studyBlocks list
  - getRemainingCapacity() method
  - canAddBlock(duration) method
  - addBlock(block) method
  - getScheduledHours() method
  - removeBlock(block) method
- [ ] Write unit tests for DaySchedule
- [ ] Integration testing with StudyBlock

**Estimated Effort:** 35 hours

---

### K (Junior Developer) - Week 1 Tasks

**1. Project Documentation (Day 1-2)**
- [ ] Read and understand PROJECT_README.md
- [ ] Read and understand ARCHITECTURE.md
- [ ] Create team presentation slides (Part 1-3 of report)
  - Team member information
  - Project name and description
  - Member contributions table
- [ ] Setup development environment
  - Install Java 17
  - Install Maven
  - Install IntelliJ IDEA or Eclipse
  - Clone repository

**2. Test Data Creation (Day 3-4)**
- [ ] Create sample DSL commands file (examples.dsl)
  - 10 different command examples
  - Various priorities and subjects
  - Different date formats
- [ ] Create test fixtures for unit tests
  - Sample PlanSpec objects
  - Sample Schedule objects
  - Sample Subject data
- [ ] Document test scenarios

**3. SchedulerServiceTest.java (Day 5-7)**
- [ ] Setup test class with Spring Boot Test
- [ ] Write test: testAddSubject()
- [ ] Write test: testSetAvailability()
- [ ] Write test: testPriorityFrontLoad()
- [ ] Write helper methods for creating test data
- [ ] Document test cases

**Estimated Effort:** 30 hours

---

## WEEK 2: Business Logic & Backend (Dec 7 - Dec 13)

### Q (Leader) - Week 2 Tasks

**1. SchedulerService.java - Core Algorithm (Day 1-4)**
- [ ] Implement generateSchedule(PlanSpec) method
  - Sort courses by priority
  - Calculate front-load distribution
  - Split calendar into halves
  - Allocate blocks with algorithm
- [ ] Implement helper methods:
  - schedulePhase(schedule, course, days, hours)
  - calculateBlocksNeeded(hours)
  - findBestTimeSlot(day, duration)
- [ ] Implement constraint validation
  - Max hours per day
  - Max continuous blocks
  - Availability checking
- [ ] Implement explanation generation
  - Why each block placed
  - Priority reasoning
  - Constraint satisfaction notes

**2. SchedulerService.java - Additional Features (Day 5-6)**
- [ ] Implement formatSchedule(Schedule) method
  - Day-by-day breakdown
  - Subject summary
  - Statistics (total hours, blocks, etc.)
- [ ] Implement shortfall handling
  - Detect insufficient capacity
  - Suggest solutions
  - Partial allocation logic
- [ ] Write comprehensive tests for SchedulerService
  - Test with various PlanSpec inputs
  - Test edge cases (no availability, too many subjects)
  - Performance testing

**3. Code Review & Integration (Day 7)**
- [ ] Review SchedulerFacade implementation (by M)
- [ ] Review ScheduleRepository implementation (by D)
- [ ] Integration testing across all services
- [ ] Fix bugs and issues

**Estimated Effort:** 42 hours

---

### M (Grammar Specialist) - Week 2 Tasks

**1. SchedulerFacade.java Implementation (Day 1-3)**
- [ ] Implement executeCommand(dslCommand) method
  - Call DSLParser.parseCommand()
  - Validate PlanSpec
  - Update currentPlan
  - Return CommandResult
- [ ] Implement generateSchedule() method
  - Call SchedulerService.generateSchedule()
  - Auto-save via ScheduleRepository
  - Update currentSchedule
  - Return ScheduleResult
- [ ] Implement getScheduleSummary() method
- [ ] Implement clear() method

**2. SchedulerFacade.java - Persistence Integration (Day 4-5)**
- [ ] Implement listSavedSchedules()
- [ ] Implement loadSchedule(filepath)
- [ ] Implement loadLatestSchedule()
- [ ] Implement deleteSchedule(filepath)
- [ ] Add error handling for all methods

**3. REST Controller - ChatbotController.java (Day 6-7)**
- [ ] Implement POST /command endpoint
  - Parse request body
  - Call facade.executeCommand()
  - Return response
- [ ] Implement GET /schedule endpoint
- [ ] Implement GET /schedules/history endpoint
- [ ] Implement POST /schedules/load endpoint
- [ ] Add request/response DTOs
- [ ] Write integration tests for REST API
- [ ] Test with Postman/curl

**Estimated Effort:** 40 hours

---

### D (Full-stack Developer) - Week 2 Tasks

**1. ScheduleRepository.java Implementation (Day 1-4)**
- [ ] Setup ObjectMapper with configurations
  - JavaTimeModule for date/time
  - Pretty printing
  - Proper serialization settings
- [ ] Implement createDirectories()
- [ ] Implement savePlan(plan) method
- [ ] Implement saveSchedule(schedule) method
- [ ] Implement loadPlan(filepath) method
- [ ] Implement loadSchedule(filepath) method

**2. ScheduleRepository.java - Advanced Features (Day 5-6)**
- [ ] Implement listPlans() method
- [ ] Implement listSchedules() method
- [ ] Implement getLatestSchedule() method
- [ ] Implement deletePlan(filepath) method
- [ ] Implement deleteSchedule(filepath) method
- [ ] Implement ScheduleFile inner class
- [ ] Add error handling for IOException

**3. Testing & Documentation (Day 7)**
- [ ] Write unit tests for ScheduleRepository
  - Test save operations
  - Test load operations
  - Test list operations
  - Test delete operations
- [ ] Test with actual JSON files
- [ ] Create sample schedule JSON files
- [ ] Update PERSISTENCE.md if needed

**Estimated Effort:** 38 hours

---

### K (Junior Developer) - Week 2 Tasks

**1. Application Configuration (Day 1-2)**
- [ ] Review and update application.properties
- [ ] Add configuration comments
- [ ] Document each property's purpose
- [ ] Test different configurations

**2. Export Functionality Helper (Day 3-5)**
- [ ] Create ExportService.java
  - exportToICS(schedule) method
  - exportToCSV(schedule) method
- [ ] Research iCal format
- [ ] Implement ICS export logic
  - Create VEVENT for each block
  - Add VTIMEZONE
  - Format properly
- [ ] Implement CSV export logic
  - Create header row
  - Format schedule data
  - Handle special characters

**3. Testing & Sample Data (Day 6-7)**
- [ ] Write tests for ExportService
- [ ] Create sample exported files (.ics, .csv)
- [ ] Test ICS files with calendar apps (Google Calendar, Outlook)
- [ ] Test CSV files with Excel/Google Sheets
- [ ] Document export formats

**Estimated Effort:** 32 hours

---

## WEEK 3: UI, Integration & Finalization (Dec 14 - Dec 20)

### Q (Leader) - Week 3 Tasks

**1. Final Integration & Bug Fixing (Day 1-2)**
- [ ] Integration test: Complete workflow
  - Parse DSL → Validate → Schedule → Save → Load
- [ ] Fix all critical bugs
- [ ] Performance optimization
- [ ] Code cleanup and refactoring

**2. Project Documentation (Day 3-4)**
- [ ] Finalize PROJECT_README.md
- [ ] Update ARCHITECTURE.md
- [ ] Update PERSISTENCE.md
- [ ] Create DEVELOPER_GUIDE.md
- [ ] Add JavaDoc comments to all public methods

**3. Report Preparation (Day 5-6)**
- [ ] Create framework/workflow diagram (Part 5 of report)
  - Use draw.io or similar tool
  - Show complete data flow
  - Include all components
- [ ] Write technique description (Part 6 of report)
  - ANTLR4 for parsing
  - Spring Boot for framework
  - Jackson for JSON
  - Swing for UI
- [ ] Write PPL concept application (Part 7 of report)
  - Lexer syntax explanation
  - Grammar syntax explanation
  - Parser tree explanation
  - Visitor pattern usage

**4. Final Review & Demo Prep (Day 7)**
- [ ] Review entire codebase
- [ ] Prepare demo script
- [ ] Test demo scenarios
- [ ] Backup all code

**Estimated Effort:** 38 hours

---

### M (Grammar Specialist) - Week 3 Tasks

**1. ChatbotUI.java Implementation (Day 1-5)**
- [ ] Design UI layout
  - Main window with BorderLayout
  - Chat area (JTextArea with JScrollPane)
  - Input field (JTextField)
  - Send button (JButton)
  - Menu bar with File, Edit, Help menus
  
- [ ] Implement initializeUI()
  - Setup window properties (title, size, close operation)
  - Create and layout components
  - Style components (fonts, colors)
  - Add welcome message
  
- [ ] Implement sendMessage()
  - Handle special commands (show history, load schedule)
  - Call facade.executeCommand()
  - Display response
  - Auto-scroll chat area
  
- [ ] Implement loadLatestScheduleOnStartup()
  - Call facade.loadLatestSchedule()
  - Display loaded schedule
  - Handle no previous schedule case
  
- [ ] Implement displaySchedule(schedule)
  - Format schedule for display
  - Color coding by priority
  - Show statistics
  
- [ ] Implement displayScheduleHistory()
  - Show list of saved schedules
  - Add click handlers for loading

**2. UI Polish & Features (Day 6)**
- [ ] Add keyboard shortcuts (Enter to send)
- [ ] Add context menu (copy, paste, clear)
- [ ] Add status bar showing current state
- [ ] Add icons and styling
- [ ] Handle window resize gracefully

**3. UI Testing (Day 7)**
- [ ] Manual testing of all UI functions
- [ ] Test with various screen sizes
- [ ] Test error scenarios
- [ ] User acceptance testing
- [ ] Fix UI bugs

**Estimated Effort:** 42 hours

---

### D (Full-stack Developer) - Week 3 Tasks

**1. End-to-End Testing (Day 1-3)**
- [ ] Create integration test suite
- [ ] Test complete workflow scenarios
  - Simple single subject
  - Multiple subjects with priorities
  - Load and modify saved schedule
- [ ] Test edge cases
  - Invalid DSL commands
  - Insufficient availability
  - Conflicting dates
- [ ] Test REST API endpoints
- [ ] Test persistence operations
- [ ] Document test results

**2. Demo Video Creation (Day 4-6)**
- [ ] Plan demo script showing all features
  - Add subjects with different priorities
  - Set availability
  - Generate schedule
  - Show schedule details
  - Close and reopen app
  - Load previous schedule
  - Show history
  - Export to ICS/CSV
  - Test via REST API
  
- [ ] Record screen with narration
  - Use OBS Studio or similar
  - 5-10 minutes duration
  - Clear audio and video
  - Show both UI and code
  
- [ ] Edit video
  - Add intro/outro
  - Add captions if needed
  - Add background music (optional)
  
- [ ] Upload to YouTube
  - Create unlisted video
  - Add description
  - Add timestamps in description
  - Get shareable link

**3. Code Quality & Cleanup (Day 7)**
- [ ] Run code quality checks (SonarLint)
- [ ] Fix all warnings
- [ ] Format code consistently
- [ ] Remove commented code
- [ ] Remove debug statements

**Estimated Effort:** 40 hours

---

### K (Junior Developer) - Week 3 Tasks

**1. Documentation Polish (Day 1-2)**
- [ ] Proofread all documentation files
- [ ] Fix typos and grammar errors
- [ ] Ensure consistent formatting
- [ ] Add table of contents where missing
- [ ] Verify all code examples work

**2. Final Report Assembly (Day 3-5)**
- [ ] Compile all report sections
  - Part 1: Team member information
  - Part 2: Project name
  - Part 3: Member contributions (detailed table)
  - Part 4: Problem statement (based on PROJECT_README.md)
  - Part 5: Framework diagram (from Q)
  - Part 6: Technique description (from Q)
  - Part 7: PPL concepts (from Q)
  - Part 8: YouTube demo link (from D)
  
- [ ] Format report professionally
  - Consistent fonts and styles
  - Page numbers
  - Header/footer
  - Cover page
  
- [ ] Create contribution breakdown table
  - List all tasks per person
  - Estimate hours per task
  - Calculate total contribution percentage
  
- [ ] Proofread entire report
- [ ] Export to PDF

**3. Submission Preparation (Day 6-7)**
- [ ] Verify all deliverables:
  - Source code (GitHub repository)
  - Report PDF
  - YouTube demo link
  - README files
  
- [ ] Create submission checklist
- [ ] Test building project from scratch
- [ ] Prepare backup copies
- [ ] Submit on time

**Estimated Effort:** 30 hours

---

## Work Distribution Summary

### Total Estimated Effort by Person

| Person | Week 1 | Week 2 | Week 3 | Total Hours | Percentage |
|--------|--------|--------|--------|-------------|------------|
| Q      | 40h    | 42h    | 38h    | 120h        | 28%        |
| M      | 40h    | 40h    | 42h    | 122h        | 28%        |
| D      | 35h    | 38h    | 40h    | 113h        | 26%        |
| K      | 30h    | 32h    | 30h    | 92h         | 21%        |
| **Total** | **145h** | **152h** | **150h** | **447h** | **100%** |

### Complexity Distribution

**High Complexity Tasks:**
- Q: Core scheduling algorithm, integration, architecture design
- M: ANTLR4 grammar design, parser implementation, UI implementation

**Medium Complexity Tasks:**
- Q: PlanSpec and Schedule models with validation
- M: SchedulerFacade orchestration, REST API
- D: ScheduleRepository with JSON operations

**Low-Medium Complexity Tasks:**
- D: Domain models (Priority, Subject, StudyBlock, DaySchedule)
- D: Export service implementation

**Low Complexity Tasks:**
- K: Documentation, test data creation, report assembly
- K: Configuration and testing support

---

## Member Contribution Breakdown (for Report Part 3)

### Q (Leader) - 28% Total Contribution

**Architecture & Design:**
- Project structure and layering
- Architecture documentation
- Integration strategy

**Core Implementation:**
- PlanSpec model with validation (complex IR)
- Schedule model enhancement
- SchedulerService algorithm (priority-based, front-loading)
- Shortfall handling and explanations

**Documentation & Leadership:**
- Framework/workflow diagram
- Technique descriptions
- PPL concept documentation
- Code reviews and integration
- Final testing and bug fixes

---

### M (Grammar Specialist) - 28% Total Contribution

**DSL & Parsing:**
- Complete ANTLR4 grammar design
- Extended DSL commands (10+ command types)
- DSLParser implementation with Visitor pattern
- Error handling and reporting

**Backend Services:**
- SchedulerFacade orchestration layer
- REST API implementation
- State management

**User Interface:**
- Complete Swing UI implementation
- Event handling and user interaction
- Auto-load and history features

---

### D (Full-stack Developer) - 26% Total Contribution

**Data Models:**
- Priority enum with weights and ratios
- Subject, StudyBlock, DaySchedule models
- Model unit tests

**Persistence Layer:**
- ScheduleRepository complete implementation
- JSON serialization/deserialization
- File operations and history management

**Testing & Demo:**
- End-to-end integration testing
- Demo video creation and editing
- YouTube upload and documentation

**Additional Features:**
- Export service (ICS, CSV formats)
- Code quality assurance

---

### K (Junior Developer) - 21% Total Contribution

**Documentation:**
- Project documentation review
- Team presentation slides
- Report proofreading and formatting

**Testing Support:**
- Test data creation
- Sample DSL commands
- SchedulerServiceTest implementation
- Export functionality testing

**Configuration:**
- Application.properties documentation
- Development environment setup
- Sample data files

**Final Deliverables:**
- Report assembly and formatting
- Submission preparation
- Quality checks

---

## Communication & Collaboration

### Daily Standups
- 15-minute daily sync (in-person or online)
- Each member reports: Done yesterday, Plan today, Blockers

### Code Reviews
- All code must be reviewed by at least one other member
- Q reviews all complex/critical code
- Use GitHub Pull Requests

### Testing Strategy
- Unit tests: Each developer writes tests for their code
- Integration tests: Q and D collaborate
- UI/End-to-end tests: M and D collaborate

### Documentation
- Inline code comments: All members
- JavaDoc: All members for public methods
- README updates: Q and K collaborate

---

## Risk Management

### If M Falls Behind (Grammar/Parser)
- Q assists with Visitor implementation
- Simplify grammar to essential commands only
- Use simpler parsing approach temporarily

### If Q Falls Behind (Core Algorithm)
- M assists with SchedulerService
- Implement basic greedy allocation first
- Add advanced features (front-loading) later

### If D Falls Behind (Persistence)
- K helps with simple CRUD operations
- Reduce export formats to CSV only initially
- Use simpler JSON structure

### If K Falls Behind (Documentation/Testing)
- D assists with report assembly
- M helps with test data creation
- Q reviews and finalizes documentation

---

## Success Criteria

### Week 1 Complete When:
- [x] All models implemented and tested
- [x] DSL grammar complete and parser generates AST
- [x] Project compiles without errors
- [x] Basic unit tests passing

### Week 2 Complete When:
- [x] Scheduling algorithm works correctly
- [x] Persistence layer saves/loads schedules
- [x] REST API endpoints functional
- [x] Integration tests passing

### Week 3 Complete When:
- [x] UI fully functional
- [x] Demo video uploaded to YouTube
- [x] Report complete and formatted
- [x] All documentation finalized
- [x] Ready for submission

---

## Final Checklist (Before Submission)

- [ ] Code compiles: `mvn clean install` succeeds
- [ ] All tests pass: `mvn test` succeeds
- [ ] Demo video uploaded and accessible
- [ ] Report PDF generated with all 8 parts
- [ ] GitHub repository public and accessible
- [ ] README files complete
- [ ] No sensitive information in code
- [ ] All team members reviewed final version
- [ ] Backup copy saved

---

**Project Start:** November 30, 2024  
**Project End:** December 20, 2024  
**Submission Deadline:** December 20, 2024 (23:59)

Good luck team! 🚀
