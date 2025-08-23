# HMCTS Dev Test Backend
This will be the backend for the brand new HMCTS case management system. As a potential candidate we are leaving
this in your hands. Please refer to the brief for the complete list of tasks! Complete as much as you can and be
as creative as you want.

You should be able to run `./gradlew build` to start with to ensure it builds successfully. Then from that you
can run the service in IntelliJ (or your IDE of choice) or however you normally would.

There is an example endpoint provided to retrieve an example of a case. You are free to add/remove fields as you
wish.

# HMCTS Task Management System

A comprehensive task management system designed for HMCTS caseworkers to efficiently track and manage their tasks.

## 🚀 Features

### Backend API
- **Create Tasks**: Add new tasks with title, description, status, and due date
- **Retrieve Tasks**: Get individual tasks by ID or fetch all tasks with optional status filtering
- **Update Tasks**: Modify task details or update status specifically
- **Delete Tasks**: Remove tasks when no longer needed
- **Task Status Management**: Support for TODO, IN_PROGRESS, COMPLETED, and CANCELLED statuses
- **Overdue Detection**: Automatic identification of overdue tasks
- **Comprehensive Validation**: Input validation and error handling
- **RESTful API**: Well-structured REST endpoints with proper HTTP status codes

### Frontend Application
- **Intuitive Dashboard**: Clean, user-friendly interface showing task statistics
- **Task Management**: Create, edit, update, and delete tasks seamlessly
- **Status Filtering**: Filter tasks by status for better organization
- **Visual Status Indicators**: Color-coded status badges with icons
- **Overdue Alerts**: Clear visual indicators for overdue tasks
- **Responsive Design**: Works on desktop and mobile devices
- **Real-time Updates**: Immediate UI updates after task operations

## 🛠️ Tech Stack

### Backend
- **Java 11**
- **Spring Boot 2.7.0** - Main framework
- **Spring Data JPA** - Data persistence
- **Spring Web** - REST API endpoints
- **Spring Validation** - Input validation
- **H2 Database** - In-memory database for development
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework

### Frontend
- **React 18** - UI framework
- **JavaScript ES6+** - Programming language
- **Tailwind CSS** - Styling framework
- **Lucide React** - Icon library
- **Modern Browser APIs** - Fetch for API calls

## 📋 Prerequisites

- **Java 11** or higher
- **Node.js 16** or higher
- **npm** or **yarn**
- **Git**

## 🚀 Quick Start

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd hmcts-task-management/backend
   ```

2. **Build and run the application**
   ```bash
   ./gradlew bootRun
   ```
   
   Or on Windows:
   ```bash
   gradlew.bat bootRun
   ```

3. **Verify the backend is running**
   - API: http://localhost:8080/api/tasks
   - H2 Console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:mem:testdb`
     - Username: `sa`
     - Password: `password`

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd hmcts-task-management/frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Start the development server**
   ```bash
   npm start
   ```

4. **Access the application**
   - Frontend: http://localhost:3000

## 📚 API Documentation

### Base URL: `http://localhost:8080/api`

### Endpoints

#### Tasks

| Method | Endpoint | Description |
|-----
