# HMCTS Task Management System

## Overview

This project provides a comprehensive task management system for HMCTS caseworkers to efficiently manage their tasks. The system includes features for creating, updating, retrieving, and deleting tasks with various status tracking capabilities.

## Features

- **Task Creation**: Create new tasks with title, description, status, and due dates
- **Task Management**: Update task status, retrieve tasks by ID, and delete tasks
- **Task Filtering**: Filter tasks by status and search by title/description
- **Task Analytics**: Get task statistics and identify overdue tasks
- **RESTful API**: Complete REST API with proper HTTP status codes
- **Database Integration**: JPA/Hibernate with H2 database for development
- **Comprehensive Testing**: Unit, integration, functional, and smoke tests

## Prerequisites

- **Java 21** - Required for compilation and runtime
- **Gradle** - Build tool (wrapper included)
- **Git** - For version control

## Setup Instructions

### Environment Setup

1. **Install Java 21**:
   ```bash
   # macOS with Homebrew
   brew install openjdk@21
   
   # Export Java 21 to PATH
   export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"
   
   # Verify installation
   java -version
   ```

2. **Clone the Repository**:
   ```bash
   git clone <backend-repository-url>
   cd backend
   ```

### Build and Run

3. **Build the Project**:
   ```bash
   ./gradlew build
   ```

4. **Run the Application**:
   ```bash
   ./gradlew bootRun
   ```
   
   The application will start on port **4000** by default.

5. **Verify Installation**:
   ```bash
   curl http://localhost:4000/
   ```
   Should return: `Welcome to test-backend`

### Development Tools

- **H2 Database Console**: Available at `http://localhost:4000/h2-console`
  - JDBC URL: `jdbc:h2:mem:devdb`
  - Username: `sa`
  - Password: `password`

- **API Documentation**: Available at `http://localhost:4000/swagger-ui/index.html`

## API Endpoints

### Root Endpoint

#### GET /
Returns a welcome message.

**Response:**
- **200 OK**: `"Welcome to test-backend"`

---

### Task Management Endpoints

Base URL: `/api/tasks`

#### POST /api/tasks
Creates a new task.

**Request Body:**
```json
{
  "title": "string (required, max 255 chars)",
  "description": "string (optional, max 1000 chars)", 
  "status": "PENDING|IN_PROGRESS|COMPLETED|CANCELLED (optional, defaults to PENDING)",
  "dueDate": "2024-12-31T23:59:59 (optional, ISO 8601 format)"
}
```

**Responses:**
- **201 Created**: Task created successfully
- **400 Bad Request**: Invalid request data (empty title, etc.)

**Example:**
```bash
curl -X POST http://localhost:4000/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Review case documents",
    "description": "Review all submitted documents for case ABC123",
    "status": "PENDING",
    "dueDate": "2024-12-31T17:00:00"
  }'
```

#### GET /api/tasks
Retrieves all tasks ordered by due date.

**Responses:**
- **200 OK**: Array of tasks

**Example Response:**
```json
[
  {
    "id": 1,
    "title": "Review case documents",
    "description": "Review all submitted documents for case ABC123",
    "status": "PENDING",
    "dueDate": "2024-12-31T17:00:00",
    "createdDate": "2024-01-15T10:00:00",
    "updatedDate": "2024-01-15T10:00:00"
  }
]
```

#### GET /api/tasks/{id}
Retrieves a specific task by ID.

**Path Parameters:**
- `id`: Task ID (Long)

**Responses:**
- **200 OK**: Task details
- **404 Not Found**: Task doesn't exist
- **400 Bad Request**: Invalid ID format

**Example:**
```bash
curl http://localhost:4000/api/tasks/1
```

#### PUT /api/tasks/{id}/status
Updates the status of a specific task.

**Path Parameters:**
- `id`: Task ID (Long)

**Request Body:**
```json
{
  "status": "PENDING|IN_PROGRESS|COMPLETED|CANCELLED"
}
```

**Responses:**
- **200 OK**: Task updated successfully
- **404 Not Found**: Task doesn't exist
- **400 Bad Request**: Invalid status or ID

**Example:**
```bash
curl -X PUT http://localhost:4000/api/tasks/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}'
```

#### DELETE /api/tasks/{id}
Deletes a specific task.

**Path Parameters:**
- `id`: Task ID (Long)

**Responses:**
- **204 No Content**: Task deleted successfully
- **404 Not Found**: Task doesn't exist
- **400 Bad Request**: Invalid ID format

**Example:**
```bash
curl -X DELETE http://localhost:4000/api/tasks/1
```

---

### Case Management Endpoints (Legacy)

#### GET /get-example-case
Returns a sample case object.

**Response:**
- **200 OK**: Example case with ID, case number, title, description, status, and created date

#### GET /get-all-cases
Returns all cases.

**Response:**
- **200 OK**: Array of cases

---

## Task Status Values

| Status | Description |
|--------|-------------|
| `PENDING` | Task is created but not started |
| `IN_PROGRESS` | Task is currently being worked on |
| `COMPLETED` | Task has been finished |
| `CANCELLED` | Task has been cancelled |

## Development

### Running Tests

```bash
# Run all tests
./gradlew testAll

# Run specific test types
./gradlew test           # Unit tests
./gradlew integration    # Integration tests
./gradlew functional     # Functional tests
./gradlew smoke         # Smoke tests
```

### Development Profile

```bash
# Run with development profile
./gradlew dev
```

### Code Quality

```bash
# Run code quality checks
./gradlew check

# Generate test coverage report
./gradlew jacocoTestReport
# Report available at: build/reports/jacoco/test/html/index.html

# Run dependency vulnerability check
./gradlew dependencyCheckAnalyze
```

### Database Management

The application uses H2 in-memory database with the following configurations:

- **Development**: `jdbc:h2:mem:devdb`
- **Test**: `jdbc:h2:mem:testdb`
- **Integration Test**: `jdbc:h2:mem:testdb-integration`

Sample data is automatically initialized in development mode but disabled during testing.

## Configuration

### Environment Variables

- `SERVER_PORT`: Application port (default: 4000)
- `SPRING_PROFILES_ACTIVE`: Active Spring profiles

### Application Profiles

- **default**: Development profile with H2 console enabled
- **test**: Test profile with minimal logging
- **integration**: Integration test profile with isolated database


## Error Handling

The API returns standard HTTP status codes:

- **200 OK**: Successful GET/PUT requests
- **201 Created**: Successful POST requests
- **204 No Content**: Successful DELETE requests
- **400 Bad Request**: Invalid request data
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side errors

## CORS Configuration

The API supports Cross-Origin Resource Sharing (CORS) with the following configuration:
- **Allowed Origins**: `*` (all origins)
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Allowed Headers**: All headers

## Monitoring and Health Checks

- **Health Check**: `GET /health`
- **Application Info**: `GET /info`
- **Readiness Check**: `GET /health/readiness`

## Troubleshooting

### Common Issues

1. **Port Already in Use**:
   ```bash
   # Check what's using port 4000
   lsof -i :4000
   
   # Kill the process or change port in .env file
   SERVER_PORT=4001
   ```

2. **Java Version Issues**:
   ```bash
   # Verify Java 21 is being used
   java -version
   ./gradlew -version
   ```

3. **Test Failures**:
   ```bash
   # Run tests with detailed output
   ./gradlew test --info --stacktrace
   
   # Check test reports
   open build/reports/tests/test/index.html
   ```

4. **Database Issues**:
   ```bash
   # Clear Gradle cache
   ./gradlew clean
   
   # Reset H2 database (automatic on restart)
   ```

### Development Tips

- Use `./gradlew build --continuous` for continuous builds during development
- Enable H2 console for database inspection: `http://localhost:4000/h2-console`
- Check application logs for detailed error information
- Use the `--scan` flag with Gradle for build insights

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests: `./gradlew testAll`
5. Submit a pull request