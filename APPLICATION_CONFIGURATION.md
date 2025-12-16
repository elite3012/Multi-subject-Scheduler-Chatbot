# APPLICATION CONFIGURATION
## Overview
This document describes all configuration properties used by the **Multi-Subject Scheduler Chatbot** application, their purpose, and guidance on when and how to modify them.

## Configuration Table

| Property Name | Default Value | Description | Unit | When to Change |
|--------------|--------------|-------------|------|---------------|
| spring.application.name | Multi-Subject Scheduler Chatbot | Application identifier used by Spring Boot | – | Rarely; only if running multiple apps |
| server.port | 8081 | HTTP port for the application server | port | When port conflict occurs |
| server.servlet.encoding.charset | UTF-8 | Default character encoding | charset | If non-UTF-8 system required |
| spring.jackson.time-zone | Asia/Ho_Chi_Minh | Default timezone for JSON & scheduling | timezone | When deploying to another region |
| logging.level.root | INFO | Global logging level | – | DEBUG for dev, WARN/ERROR for prod |
| logging.level.com.scheduler.chatbot | DEBUG | Scheduler/chatbot log detail | – | Reduce in production |
| scheduler.default.block.duration | 90 | Length of one study block | minutes | Adjust based on focus style |
| scheduler.default.break.duration | 15 | Break time between blocks | minutes | Shorter/longer breaks |
| scheduler.max.hours.per.day | 8 | Daily maximum study hours | hours | Based on workload |
| scheduler.max.continuous.block | 3 | Max consecutive blocks | blocks | To reduce fatigue |
| scheduler.priority.low.weight | 1.0 | Weight for low priority tasks | multiplier | Rarely |
| scheduler.priority.medium.weight | 1.2 | Weight for medium priority tasks | multiplier | Fine-tuning balance |
| scheduler.priority.high.weight | 1.5 | Weight for high priority tasks | multiplier | Increase for urgent goals |
| scheduler.frontload.low | 0.4 | Early-day ratio for low priority | ratio | Change schedule shape |
| scheduler.frontload.medium | 0.5 | Early-day ratio for medium priority | ratio | Change schedule shape |
| scheduler.frontload.high | 0.6 | Early-day ratio for high priority | ratio | Emphasize important tasks |


## How to Modify Configuration
1. Edit `application.properties` (or environment-specific file).
2. Change the desired property value.
3. Restart the Spring Boot application for changes to take effect.