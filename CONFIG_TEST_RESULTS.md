# CONFIG TEST RESULTS

## Test Environment
- OS: Windows 10
- Java: JDK 17
- Framework: Spring Boot 3.2.0
- Web Server: Embedded Apache Tomcat

## Test Case 1: Shorter Block Duration
- Changed `scheduler.default.block.duration=60`
- Result: Schedule generated correctly
- Observation: More blocks per day, higher fragmentation

## Test Case 2: Increased Daily Limit
- Changed `scheduler.max.hours.per.day=10`
- Result: No errors
- Observation: Denser schedules, risk of fatigue

## Test Case 3: High Priority Emphasis
- Changed `scheduler.priority.high.weight=2.0`
- Result: Correct prioritization
- Observation: High-priority subjects dominate early slots

## Test Case 4: Reduced Continuous Blocks
- Changed `scheduler.max.continuous.block=2`
- Result: More frequent breaks inserted
- Observation: Better balance, slightly longer schedules

## Key Findings
- Extreme priority weights can starve low-priority subjects.
- Very short block durations reduce focus efficiency.
- Timezone configuration is critical for correct date boundaries.