# Camunda 7 Backend with Keycloak Authentication

A Spring Boot-based workflow automation backend using Camunda 7, secured with Keycloak OAuth2 authentication.

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Quick Start](#3-quick-start)
4. [User Guide](#4-user-guide)
5. [API Reference](#5-api-reference)
6. [Security \& Authentication](#6-security--authentication)
7. [The Approval Process](#7-the-approval-process)
8. [Keycloak Configuration](#8-keycloak-configuration)
9. [Troubleshooting](#9-troubleshooting)
10. [Project Structure](#10-project-structure)

---

## 1. Project Overview

### What This Project Is

This is a **workflow automation backend** built with Camunda 7 (a popular open-source workflow engine) and secured with **Keycloak** for authentication. It provides a REST API for managing business processes and includes a web interface (Camunda Cockpit) for monitoring and interacting with processes.

### Purpose

The system implements an **Approval Process** workflow that routes requests based on amount thresholds:
- **Low amounts (<= 1000)**: Requires employee submission only
- **High amounts (> 1000)**: Requires employee submission + manager approval

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              USER'S BROWSER                                 │
│                                                                             │
│   ┌─────────────────────┐          ┌─────────────────────────────────────┐ │
│   │  Camunda Cockpit    │          │        Frontend Application        │ │
│   │  (Web UI)           │          │        (REST API Consumer)         │ │
│   └──────────┬──────────┘          └─────────────────┬───────────────────┘ │
│              │                                           │                    │
│              │  http://localhost:7509/camunda           │                    │
│              │                                           │ http://localhost: │
│              │                                           │        7509/api   │
└──────────────┼───────────────────────────────────────────┼────────────────────┘
               │                                           │
               │           ┌───────────────────────┐       │
               │           │   Spring Boot App     │       │
               │           │   (Port 7509)         │       │
               │           │                       │       │
               │           │  ┌─────────────────┐  │       │
               │           │  │  REST API       │  │       │
               │           │  │  /api/*         │  │       │
               │           │  └─────────────────┘  │       │
               │           │  ┌─────────────────┐  │       │
               │           │  │  Camunda Webapp │  │       │
               │           │  │  /camunda/*     │  │       │
               │           │  └─────────────────┘  │       │
               │           └───────────┬───────────┘       │
               │                       │                   │
               │              ┌────────┴────────┐          │
               │              │  Spring Security │          │
               │              │  OAuth2 + JWT    │          │
               │              └────────┬────────┘          │
               │                       │                   │
               │         ┌─────────────┼─────────────┐     │
               │         │             │             │     │
               │         ▼             ▼             ▼     │
               │   ┌─────────────────────────────────────┐  │
               │   │           MARIADB (Port 7506)       │  │
               │   │      Camunda Process Engine DB     │  │
               │   └─────────────────────────────────────┘  │
               │                       │                   │
               └───────────────────────┼───────────────────┘
                                       │
                                       │ OAuth2 Redirect
                                       ▼
                          ┌────────────────────────┐
                          │   KEYCLOAK (Port 7508) │
                          │   Authentication Server│
                          │   - Login Page         │
                          │   - User Management    │
                          │   - OAuth2 Provider    │
                          └────────────────────────┘
                                       │
                                       │ MariaDB (Port 7507)
                                       ▼
                          ┌────────────────────────┐
                          │  KEYCLOAK DB (Port 7507)│
                          │  Users, Roles, Clients │
                          └────────────────────────┘
```

---

## 2. Technology Stack

### Core Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 3.3.13 | Application framework |
| **Camunda** | 7.22.0 | Workflow/BPM engine |
| **Java** | 17 | Runtime environment |
| **Maven** | 3.x | Build tool |

### Infrastructure

| Service | Image | Port | Purpose |
|---------|-------|------|---------|
| **MariaDB** | mariadb:11.2 | 7506 | Camunda database |
| **MariaDB (Keycloak)** | mariadb:11.2 | 7507 | Keycloak database |
| **Keycloak** | quay.io/keycloak/keycloak:24.0.4 | 7508 | Authentication/SSO |
| **Spring Boot App** | (built locally) | 7509 | Your application |

### Security

| Component | Technology |
|-----------|------------|
| Authentication | Keycloak OAuth2 (OpenID Connect) |
| Authorization | JWT Bearer Tokens |
| Web Security | Spring Security OAuth2 |
| SSO | Camunda Webapps integrated with Keycloak |

### Project Dependencies (pom.xml)

```xml
<!-- Spring Boot -->
- spring-boot-starter-web
- spring-boot-starter-security
- spring-boot-starter-oauth2-resource-server
- spring-boot-starter-oauth2-client
- spring-boot-starter-data-jpa

<!-- Camunda -->
- camunda-bpm-spring-boot-starter (7.22.0)
- camunda-bpm-spring-boot-starter-rest (7.22.0)
- camunda-bpm-spring-boot-starter-webapp (7.22.0)
- camunda-bpm-spring-boot-starter-security (7.22.0)

<!-- Database -->
- mariadb-java-client (3.3.3)

<!-- Keycloak -->
- keycloak-spring-boot-starter (24.0.4)
```

---

## 3. Quick Start

### Prerequisites

- **Java 17** or higher installed
- **Docker** and **Docker Compose** installed
- **Maven** installed (or use Maven wrapper)

### Step 1: Start the Infrastructure

Start all required services (MariaDB for Camunda, MariaDB for Keycloak, Keycloak):

```bash
cd backend
docker-compose up -d
```

Wait for services to be healthy:
```bash
docker-compose ps
```

Expected output:
```
NAME                IMAGE                      STATUS                   PORTS
camunda-mariadb     mariadb:11.2              Up (healthy)            0.0.0.0:7506->3306/tcp
keycloak-db         mariadb:11.2              Up (healthy)            0.0.0.0:7507->3306/tcp
camunda-keycloak    quay.io/keycloak:24.0.4   Up (healthy)            0.0.0.0:7508->8080/tcp
```

### Step 2: Build and Run the Application

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or build first, then run
./mvnw clean package -DskipTests
java -jar target/camunda-backend-1.0.0.jar
```

### Step 3: Access the Services

| Service | URL | Description |
|---------|-----|-------------|
| **Camunda Cockpit** | http://localhost:7509/camunda | Process monitoring & management |
| **Camunda Tasklist** | http://localhost:7509/camunda/tasklist | User task inbox |
| **Camunda Admin** | http://localhost:7509/camunda/admin | Administration |
| **REST API Base** | http://localhost:7509/api | Backend API |
| **Keycloak Admin** | http://localhost:7508/admin | Keycloak administration |

---

## 4. User Guide

### Accessing Camunda Cockpit

1. Open your browser and navigate to: **http://localhost:7509/camunda**

2. You will be redirected to the **Keycloak login page**

3. Enter your credentials:
   - **Username**: `alice` (or `bob`, `charlie`, `diana`)
   - **Password**: `alice123` (or respective password)

4. After successful login, you will be redirected to Camunda Cockpit

### The Approval Process Workflow

The system implements a 2-step approval process based on amount:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        APPROVAL PROCESS                                      │
└─────────────────────────────────────────────────────────────────────────────┘

   ┌──────────┐
   │  START   │
   └────┬─────┘
        │
        ▼
   ┌────────────────────────────┐
   │   Submit Request (Alice)   │  ◄── Assigned to alice (employee)
   │   - amount: 1500           │
   └─────────────┬──────────────┘
                 │
                 ▼
         ┌───────────────┐
         │ Amount > 1000?│
         └───────┬───────┘
                 │
        ┌────────┴────────┐
        │                 │
       YES                NO
        │                 │
        ▼                 ▼
┌───────────────┐   ┌──────────────────┐
│ Manager       │   │ Mark As Done    │
│ Approval      │   │ (Diana)         │
│ (Bob)         │   │                 │
└───────┬───────┘   └────────┬────────┘
        │                     │
        └──────────┬──────────┘
                   │
                   ▼
            ┌──────────┐
            │   END    │
            └──────────┘
```

### Testing the Workflow

#### Scenario 1: High Amount (> 1000) - Requires Manager Approval

1. Login as **alice** / **alice123**
2. Use the API to start a process with high amount:

```bash
curl -X POST http://localhost:7509/api/process-definitions/approval-process/start \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"variables": {"amount": 1500}}'
```

3. Login as **bob** / **bob123** in a new browser tab
4. Go to Tasklist to see the approval task
5. Complete the task

#### Scenario 2: Low Amount (<= 1000) - No Manager Approval Needed

```bash
curl -X POST http://localhost:7509/api/process-definitions/approval-process/start \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"variables": {"amount": 500}}'
```

The process skips manager approval and goes directly to completion.

---

## 5. API Reference

### Authentication

All API endpoints require a **JWT Bearer Token** in the Authorization header.

#### Getting a Token

**Option 1: Using Keycloak's API**

```bash
curl -X POST http://localhost:7508/realms/camunda-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=camunda-app" \
  -d "client_secret=change-this-secret" \
  -d "username=alice" \
  -d "password=alice123"
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6IC...",
  "expires_in": 300,
  "refresh_token": "...",
  "token_type": "Bearer",
  "session_state": "...",
  "scope": "profile email"
}
```

**Option 2: Using the Frontend Flow**

1. Navigate to http://localhost:7509/camunda
2. Login via Keycloak
3. The browser will establish a session
4. For API calls, you can extract the token from browser DevTools or implement a token refresh flow

#### Using the Token

Include the token in the Authorization header:

```bash
curl -X GET http://localhost:7509/api/tasks \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6IC..."
```

---

### Endpoints

#### Process Definitions

##### Get All Process Definitions

```http
GET /api/process-definitions
```

**Example:**
```bash
curl -X GET http://localhost:7509/api/process-definitions \
  -H "Authorization: Bearer <TOKEN>"
```

**Response:**
```json
[
  {
    "id": "approval-process:1:12345",
    "key": "approval-process",
    "name": "Approval Process",
    "version": 1,
    "deploymentId": "12345",
    "resource": "approval-process.bpmn",
    "suspensionState": 1
  }
]
```

##### Start a Process

```http
POST /api/process-definitions/{key}/start
```

**Parameters:**
- `key` (path): Process definition key (e.g., `approval-process`)

**Request Body (optional):**
```json
{
  "businessKey": "optional-business-key",
  "variables": {
    "amount": 1500,
    "description": "Purchase request"
  }
}
```

**Example:**
```bash
curl -X POST http://localhost:7509/api/process-definitions/approval-process/start \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "variables": {
      "amount": 1500
    }
  }'
```

**Response:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "processDefinitionId": "approval-process:1:12345",
  "processDefinitionKey": "approval-process",
  "processDefinitionName": "Approval Process",
  "businessKey": null,
  "activityId": "Activity_SubmitRequest",
  "suspended": false,
  "tenantId": null
}
```

---

#### Process Instances

##### Get Process Instance

```http
GET /api/process-instances/{id}
```

**Example:**
```bash
curl -X GET http://localhost:7509/api/process-instances/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <TOKEN>"
```

**Response:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "processDefinitionId": "approval-process:1:12345",
  "processDefinitionKey": "approval-process",
  "processDefinitionName": "Approval Process",
  "businessKey": null,
  "activityId": "Activity_SubmitRequest",
  "suspended": false,
  "tenantId": null
}
```

##### Get Process Instance Variables

```http
GET /api/process-instances/{id}/variables
```

**Example:**
```bash
curl -X GET http://localhost:7509/api/process-instances/a1b2c3d4-e5f6-7890-abcd-ef1234567890/variables \
  -H "Authorization: Bearer <TOKEN>"
```

**Response:**
```json
[
  {
    "name": "amount",
    "type": "Integer",
    "value": 1500
  }
]
```

##### Delete Process Instance

```http
DELETE /api/process-instances/{id}
```

**Example:**
```bash
curl -X DELETE http://localhost:7509/api/process-instances/a1b2c3d4-e5f6-7890-abcd-ef1234567890 \
  -H "Authorization: Bearer <TOKEN>"
```

---

#### Tasks

##### Get Tasks for Current User

```http
GET /api/tasks
```

Returns all tasks assigned to or visible to the currently authenticated user.

**Example:**
```bash
curl -X GET http://localhost:7509/api/tasks \
  -H "Authorization: Bearer <TOKEN>"
```

**Response:**
```json
[
  {
    "id": "task123",
    "name": "Submit Request",
    "assignee": "alice",
    "owner": null,
    "created": "2026-02-24T10:30:00.000+0000",
    "due": null,
    "priority": 50,
    "processInstanceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "processDefinitionId": "approval-process:1:12345",
    "taskDefinitionKey": "Activity_SubmitRequest",
    "candidateGroups": ["employee"],
    "candidateUsers": [],
    "canComplete": true
  }
]
```

> **Note:** `canComplete` is `true` when the current user is either: the task assignee, in the candidate groups, or in the candidate users list.

##### Get Task by ID

```http
GET /api/tasks/{id}
```

**Example:**
```bash
curl -X GET http://localhost:7509/api/tasks/task123 \
  -H "Authorization: Bearer <TOKEN>"
```

##### Complete a Task

```http
POST /api/tasks/{id}/complete
```

**Request Body (optional):**
```json
{
  "variables": {
    "approved": true,
    "comments": "Approved for purchase"
  }
}
```

**Example:**
```bash
curl -X POST http://localhost:7509/api/tasks/task123/complete \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "variables": {
      "approved": true
    }
  }'
```

**Response:** `200 OK` on success, `403 Forbidden` if user cannot complete the task

##### Claim a Task

```http
POST /api/tasks/{id}/claim
```

Assigns a task to the current user. The user must be a candidate (either in candidate groups or candidate users) for the task.

**Example:**
```bash
curl -X POST http://localhost:7509/api/tasks/task123/claim \
  -H "Authorization: Bearer <TOKEN>"
```

**Response:** `204 No Content` on success, `404 Not Found` if task doesn't exist

---

### API Summary Table

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/process-definitions` | List all process definitions |
| POST | `/api/process-definitions/{key}/start` | Start a new process instance |
| GET | `/api/process-instances/{id}` | Get process instance details |
| GET | `/api/process-instances/{id}/variables` | Get process variables |
| DELETE | `/api/process-instances/{id}` | Delete a process instance |
| GET | `/api/tasks` | Get tasks for current user |
| GET | `/api/tasks/{id}` | Get task details |
| POST | `/api/tasks/{id}/complete` | Complete a task |
| POST | `/api/tasks/{id}/claim` | Claim a task (assign to current user) |

---

## 6. Security & Authentication

### How It Works

The application uses **OAuth2 with OpenID Connect** for authentication:

1. **Browser Access** (`/camunda/*`):
   - User visits Camunda Cockpit
   - Redirected to Keycloak login page
   - After login, redirected back with session established
   - Session cookie maintains authentication

2. **API Access** (`/api/*`):
   - Client includes JWT token in Authorization header
   - Token validated against Keycloak
   - User identity and roles extracted from token

### Security Configuration

The security is configured in `SecurityConfig.java`:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/actuator/**").permitAll()  // Health checks public
    .requestMatchers("/camunda/**").authenticated() // Requires login
    .requestMatchers("/api/**").authenticated()     // Requires valid JWT
    .anyRequest().authenticated()
)
.oauth2Login(...)  // Browser login flow
.oauth2ResourceServer(...)  // JWT validation for API
```

### Keycloak Roles

Users are assigned roles in Keycloak. These roles are mapped to Spring Security authorities:

| Keycloak Role | Spring Authority |
|---------------|-------------------|
| `employee` | `ROLE_employee` |
| `manager` | `ROLE_manager` |
| `admin` | `ROLE_admin` |

### Test Users

| Username | Password | Roles | Description |
|----------|----------|-------|-------------|
| `alice` | `alice123` | `employee` | Can submit requests |
| `bob` | `bob123` | `manager` | Can approve high amounts |
| `charlie` | `charlie123` | `admin` | Full admin access |
| `diana` | `diana123` | `employee`, `manager` | Both roles |

---

## 7. The Approval Process

### BPMN Process Definition

The process is defined in `src/main/resources/processes/approval-process.bpmn`.

### Workflow Steps

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ START                                                                        │
│                                                                             │
│  │                                                                             │
│  ▼                                                                             │
│ ┌───────────────────┐                                                         │
│ │  Submit Request   │  User Task - Assigned to: alice                       │
│ │  (Employee)       │  Candidate Groups: employee                           │
│ │                   │                                                         │
│ │  Input:           │  This is the first task. The employee submits          │
│ │  - amount         │  a request with an amount.                            │
│ │  - description    │                                                         │
│ └────────┬──────────┘                                                         │
│          │                                                                      │
│          ▼                                                                      │
│ ┌───────────────────┐                                                         │
│ │  Amount > 1000?   │  Exclusive Gateway                                     │
│ └────────┬──────────┘                                                         │
│          │                                                                      │
│    ┌─────┴─────┐                                                              │
│    │           │                                                              │
│   YES          NO                                                             │
│    │           │                                                              │
│    ▼           ▼                                                              │
│ ┌───────────────────┐     ┌───────────────────┐                              │
│ │ Manager Approval │     │  Mark As Done     │                              │
│ │ (Bob)             │     │  (Diana)          │                              │
│ │                   │     │                   │                              │
│ │ Only executed     │     │ Automatic step   │                              │
│ │ if amount > 1000  │     │ for low amounts  │                              │
│ └────────┬──────────┘     └────────┬──────────┘                              │
│          │                         │                                          │
│          └────────────┬────────────┘                                          │
│                       ▼                                                        │
│               ┌──────────┐                                                    │
│               │   END    │                                                    │
│               └──────────┘                                                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Task Assignments

| Task | Assignee | Candidate Groups | Condition |
|------|----------|------------------|-----------|
| Submit Request | `alice` | `employee` | Always |
| Manager Approval | `bob` | `manager` | Only if amount > 1000 |
| Mark As Done | `diana` | `employee` | Always |

### Process Variables

| Variable | Type | Description |
|----------|------|-------------|
| `amount` | Integer | The request amount (determines routing) |
| `approved` | Boolean | Manager approval status (if applicable) |
| `description` | String | Optional description |

---

## 8. Keycloak Configuration

### Realm: camunda-realm

The Keycloak realm is exported in `keycloak/realm-export.json`.

### Clients

#### camunda-app

OAuth2 client for authentication:
- **Client ID**: `camunda-app`
- **Client Secret**: `change-this-secret` (change in production!)
- **Protocol**: OpenID Connect
- **Valid Redirect URIs**: `http://localhost:7509/*`
- **Web Origins**: `http://localhost:7509`

#### camunda-webapps

Client for Camunda web applications:
- **Client ID**: `camunda-webapps`
- **Standard Flow Enabled**: true (for browser login)

### Adding New Users

1. Go to Keycloak Admin: http://localhost:7508/admin
2. Login with `admin` / `admin`
3. Select realm **camunda-realm**
4. Go to **Users** → **Add user**
5. Fill in user details
6. Set credentials (password)
7. Assign roles
8. Save

### Resetting Keycloak

If you need to reset Keycloak to its initial state:

```bash
# Stop Keycloak
docker-compose stop keycloak

# Remove Keycloak database volume
docker-compose down -v

# Restart (will re-import realm-export.json)
docker-compose up -d keycloak
```

---

## 9. Troubleshooting

### Issue: "Invalid parameter: redirect_uri"

**Cause**: Keycloak client doesn't have the correct redirect URI configured.

**Solution**:
1. Go to http://localhost:7508/admin
2. Login with `admin` / `admin`
3. Go to **Clients** → **camunda-app**
4. Ensure **Valid Redirect URIs** includes: `http://localhost:7509/*`
5. Restart Keycloak if needed

### Issue: 401 Unauthorized on API calls

**Cause**: JWT token is missing or invalid.

**Solution**:
1. Ensure you're including the token: `-H "Authorization: Bearer <TOKEN>"`
2. Get a fresh token (tokens expire after 5 minutes by default)
3. Check token hasn't expired

### Issue: 403 Forbidden on Task Completion

**Cause**: User is authenticated but not authorized to complete the task.

**Solution**:
1. Ensure the user is the assignee or in a candidate group
2. Check Camunda authorization is disabled (in `application.yml`): `camunda.bpm.authorization.enabled: false`

### Issue: "Realm 'camunda-realm' already exists. Import skipped"

**Cause**: Keycloak has an old version of the realm cached.

**Solution**:
```bash
# Delete the realm and re-import
docker-compose stop keycloak
docker-compose rm -v keycloak
docker-compose up -d keycloak
```

Or manually delete via Keycloak Admin Console:
1. Go to http://localhost:7508/admin
2. Select camunda-realm
3. Go to **Realm Settings** → **Delete Realm**
4. Restart Keycloak

### Issue: "Connection refused" on port 7506/7507/7508

**Cause**: Docker services not running.

**Solution**:
```bash
docker-compose ps
docker-compose up -d
```

### Viewing Logs

```bash
# Keycloak logs
docker logs camunda-keycloak

# Application logs (when running locally)
tail -f logs/application.log

# Or in IDE console output
```

---

## 10. Project Structure

### Directory Layout

```
backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/camunda/
│       │       ├── Application.java              # Main Spring Boot application
│       │       ├── config/
│       │       │   ├── CamundaConfig.java        # Camunda configuration
│       │       │   └── SecurityConfig.java       # OAuth2/Security configuration
│       │       ├── controller/
│       │       │   ├── ProcessController.java     # Process REST endpoints
│       │       │   ├── ProcessDefinitionController.java
│       │       │   ├── ProcessInstanceController.java
│       │       │   └── TaskController.java        # Task REST endpoints
│       │       ├── dto/                          # Data Transfer Objects
│       │       │   ├── CompleteTaskDto.java
│       │       │   ├── ProcessDefinitionDto.java
│       │       │   ├── ProcessInstanceDto.java
│       │       │   ├── StartProcessDto.java
│       │       │   ├── TaskDto.java
│       │       │   └── VariableDto.java
│       │       └── service/
│       │           ├── CamundaTaskService.java   # Task business logic
│       │           └── ProcessService.java      # Process business logic
│       └── resources/
│           ├── application.yml                  # Main configuration
│           └── processes/
│               └── approval-process.bpmn        # BPMN process definition
├── keycloak/
│   └── realm-export.json                        # Keycloak realm configuration
├── docker-compose.yml                            # Infrastructure services
├── pom.xml                                      # Maven dependencies
├── Dockerfile                                   # Container build file
└── README.md                                    # This file
```

### Configuration Files

| File | Purpose |
|------|---------|
| `application.yml` | Spring Boot & Camunda configuration |
| `SecurityConfig.java` | OAuth2, JWT, and endpoint security |
| `docker-compose.yml` | MariaDB & Keycloak services |
| `realm-export.json` | Keycloak users, roles, clients |
| `approval-process.bpmn` | Workflow definition |

### Key Configuration Properties

**application.yml highlights:**

```yaml
server:
  port: 7509                          # Application port

spring:
  datasource:
    url: jdbc:mariadb://localhost:7506/camunda
    username: camunda
    password: camundapassword

  security.oauth2.client.registration.keycloak:
    client-id: camunda-app
    client-secret: change-this-secret

camunda:
  bpm:
    authorization.enabled: false     # Disable Camunda authorization
    auto-deployment.enabled: true    # Auto-deploy BPMN files
    oauth2.enabled: true             # Enable OAuth2 SSO
```

---

## Quick Reference Card

### Start Everything
```bash
docker-compose up -d
./mvnw spring-boot:run
```

### Access Points
| Service | URL |
|---------|-----|
| Cockpit | http://localhost:7509/camunda |
| API | http://localhost:7509/api |
| Keycloak | http://localhost:7508/admin |

### Test Credentials
| User | Password |
|------|----------|
| alice | alice123 |
| bob | bob123 |
| charlie | charlie123 |
| diana | diana123 |

### Get API Token
```bash
curl -X POST http://localhost:7508/realms/camunda-realm/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=camunda-app" \
  -d "client_secret=change-this-secret" \
  -d "username=alice" \
  -d "password=alice123"
```

### Start a Process
```bash
curl -X POST http://localhost:7509/api/process-definitions/approval-process/start \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"variables": {"amount": 1500}}'
```

---

*Last updated: February 2026*
