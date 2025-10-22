# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Mock Email Service is a Spring Boot application that extends Dumpster SMTP for testing purposes. It provides an in-memory SMTP server (running on port 2025) with a REST API (port 8080) to programmatically send, read, and clear email messages.

**Tech Stack:**
- Java 23
- Spring Boot 3.3.4 (packaged as WAR)
- Dumbster SMTP Server (http://quintanasoft.com/dumbster/)
- Spring Boot Actuator with Micrometer/Prometheus metrics

## Build and Run Commands

**Build:**
```bash
mvn clean package
```
This creates `target/mock-email-service.war`

**Run locally:**
```bash
mvn spring-boot:run
# OR
java -jar target/mock-email-service.war
```

**Docker:**
```bash
# Build must be done first to create the WAR file
mvn clean package
docker build -t mock-email-service .
docker run -it -p 8080:8080 mock-email-service
```

**Health checks:**
```bash
curl http://localhost:8080/healthcheck
curl http://localhost:8080/email/healthcheck
```

## Architecture

### Dual Email Storage System

The service manages emails through two separate systems:

1. **SMTP Server Emails** (`SimpleSmtpServer` via Dumbster)
   - Receives actual SMTP messages on port 2025
   - Messages stored in-memory by Dumbster library
   - Retrieved via `/email/read/{emailAddress}` endpoint
   - Cleared via `/email/clear/{emailAddress}` endpoint

2. **Web Emails** (In-memory HashMap)
   - Stored in `MockEmailServer.webEmails` map: `Map<String, List<SimpleSmtpMessage>>`
   - Created via `/webemail/send/{emailAddress}` REST endpoint
   - Merged with SMTP emails when reading via `/email/read/{emailAddress}`

When you call `/email/read/{emailAddress}`, the response combines both SMTP-received emails AND web-generated emails for that address.

### Key Components

**`MockEmailServer`** (src/main/java/com/cicdaas/mockemailservice/MockEmailServer.java)
- Singleton pattern for SMTP server lifecycle
- Initializes `SimpleSmtpServer` on port 2025
- Maintains the `webEmails` HashMap for REST-created messages

**`MockEmailService`** (src/main/java/com/cicdaas/mockemailservice/MockEmailService.java)
- Main REST controller with email API endpoints
- Scheduled tasks run every 5 minutes (300000ms) for health checks and metrics updates
- Micrometer metrics integration (counters and gauges)
- Email address decoding: if no `@` is present, appends `.com` to the path variable

**`SpringBootMockEmailApplication`** (entry point)
- Starts Spring Boot application
- Calls `MockEmailServer.getInstance()` to initialize SMTP server on startup

### Email Address Handling

The `decodeEmailAddress()` method in `MockEmailService.java:152` adds `.com` if the email address doesn't contain `@`. This allows simplified URL paths:
- `/email/read/test@example.com` and `/email/read/test@example` both work
- `/email/read/user` becomes `user.com`

## API Endpoints

**Send test SMTP email:**
```bash
curl http://localhost:8080/email/send/test@example.com
```

**Send web email (stored in HashMap):**
```bash
curl http://localhost:8080/webemail/send/test@example.com
```

**Read emails (combines both SMTP and web emails):**
```bash
curl http://localhost:8080/email/read/test@example.com
```

**Clear SMTP emails:**
```bash
curl http://localhost:8080/email/clear/test@example.com
```
Note: This only clears SMTP emails, not web emails from the HashMap.

## Observability

**Actuator endpoints:**
```bash
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

**Custom metrics:**
- `webemail.sent.count` (Counter) - total web emails sent
- `emailadress.count` (Counter) - total email addresses in memory
- `webmail.inmemory.count` (Gauge) - current count of web emails in memory (updated every 5 mins)

All actuator endpoints are exposed (configured in `application.properties`).

## Configuration

**Ports:**
- HTTP API: 8080 (default Spring Boot)
- SMTP Server: 2025 (hardcoded in `MockEmailServer.java:16`)

**Views:**
- JSP views configured with prefix `/WEB-INF/views/` and suffix `.jsp`
- Home controller (`MvcController`) serves index page at `/`

## Important Implementation Details

- No persistent storage - all emails are in-memory
- SMTP server and web emails are separate storage mechanisms but merged on read
- Scheduled tasks log health metrics every 5 minutes to help monitor memory usage
- The service packages as a WAR file (not JAR) for traditional servlet container deployment
- No unit tests in the repository currently
- use comments sparingly. Only comment complex code.