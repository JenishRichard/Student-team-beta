# College Class Room Booking System using Microservices and CI/CD

## Overview
In our college, there is no single web application to quickly check **classroom availability** and book a room for staff activities such as extra lectures, tutorials, meetings, or events. This project aims to build a **web-based room booking system** that shows real-time availability and allows staff to reserve and release rooms easily.

**MVP Scope:** Start with **1 floor** (limited set of rooms) and later extend to all floors/buildings.

---

## Problem Statement
Currently, staff members rely on manual coordination to find free rooms. This leads to:
- confusion and last-minute clashes,
- double bookings,
- wastage of time in confirming availability.

This system will solve it by providing:
- room availability view by date/time,
- booking and cancellation,
- “Mark Not Needed” option to release a room when a class is cancelled.

---

## Users
- **Staff (Primary):** view available rooms, book/cancel, mark room not needed  
- **Admin (Secondary):** manage room details (rooms list, capacity, equipment), override bookings (optional)

---

## High-Level Architecture
This project follows a microservices architecture with CI/CD automation.

### Core Microservices
1. **Room & Availability Service**
   - Manage rooms by floor (CRUD)
   - Provide availability for a given date/time slot
   - Store room metadata: capacity, type, equipment

2. **Booking Service**
   - Create/update/cancel bookings (CRUD)
   - Prevent double booking (overlapping time)
   - “Mark Not Needed” to release room slot
   - Calls Room Service to validate availability and lock/release room

### Platform Components
- **API Gateway** (single entry point)
- **Service Discovery (Eureka)**
- **Config Server** (central config)
- **Security** (JWT based authentication)
- **Resilience4J** (fallback handling when a dependent service is down)
- **Observability** (Spring Actuator + Prometheus + Grafana)
- **Swagger/OpenAPI** for API documentation

---

## Tech Stack
- **Backend:** Java, Spring Boot, Spring Cloud
- **Microservices:** REST APIs, Eureka, Spring Cloud Gateway, Config Server
- **Build:** Maven
- **CI/CD:** GitHub + Jenkins Pipeline (Jenkinsfile)
- **Code Quality:** Checkstyle + SonarQube (Quality Gate)
- **Artifacts:** Nexus Repository (planned)
- **Containers/Deploy:** Docker + Docker Compose (Dev/Staging/Prod profiles)
- **Monitoring:** Prometheus + Grafana

---

## Repository Structure
Recommended structure (current / planned):
college-classroom-booking/
services/
api-gateway/
discovery-server/
config-server/
booking-service/
room-service/
deployments/
docker-compose/
docs/
architecture/
api-specs/
Jenkinsfile
README.md
.gitignore

---

## Planned Key Features (MVP - 1 Floor)
- View rooms on Floor 1 with filters (capacity, type, equipment)
- View available slots for selected date/time
- Book a room (no double booking)
- Cancel booking / update booking time
- Mark room “Not Needed” (release the room slot)
- Role-based access (Staff/Admin)

---

## API (Planned) – Quick View
### Room Service
- `POST /rooms` (Admin)
- `GET /rooms?floor=1`
- `GET /rooms/{roomId}`
- `GET /rooms/{roomId}/availability?date=YYYY-MM-DD`
- `PATCH /rooms/{roomId}` (Admin)
- `DELETE /rooms/{roomId}` (Admin)

### Booking Service
- `POST /bookings`
- `GET /bookings?userId=...`
- `GET /bookings?roomId=...&date=...`
- `PATCH /bookings/{bookingId}`
- `DELETE /bookings/{bookingId}`
- `POST /bookings/{bookingId}/not-needed` (Release slot)

---

## CI/CD Plan (Pipeline)
GitHub webhook triggers Jenkins pipeline:
1. Checkout code
2. Build + Unit tests (Maven)
3. Checkstyle / static checks
4. SonarQube scan + Quality Gate
5. Package artifacts (JAR)
6. Store artifacts in Nexus (planned)
7. Build Docker images
8. Deploy to **Development** (auto)
9. Deploy to **main** (Manual)
10. Notifications (Slack) + Monitoring (Grafana)

---

## Jira Link
Jira Project: https://student-team-beta.atlassian.net/jira

GitHub Repository: https://github.com/JenishRichard/Student-team-beta.git

---


## Team
	•	Jenish Richard – A00336114
	•	Sanket Shetty – A00336144
	•	Shivendra Surve – A00336154
