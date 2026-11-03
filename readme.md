# A. Starting the Application
## Prerequisites
  - Java 17
  -  Maven
## Steps
  1. Open a terminal and navigate to the project root directory.
  2. Run the following command: `./mvnw spring-boot:run`
  3. Once the application starts successfully, it will be available at: `http://localhost:8080`



# B. Importing the Postman Collection
## Steps
  1. Navigate to the postman folder in the project.
  2. Locate the file: Task-Management-API.postman_collection.json
  3. Open the Postman Desktop application.
  4. Click the drop down icon on the top-left conner then hover over file and select Import or Click Import in the top-left corner.
  5. Select the Import tab then under select files or folders, click file and choose: Task-Management-API postman_collection.json
  6. Click Import.
  7. The API collection will be added to your Postman workspace and will be ready for testing.



# C. Database Setup
- The application is currently configured to use a PostgreSQL database hosted on Aiven.
- DB connection details is in: `src\main\resources\application.properties`
- Database Scripts:
  - Create tables: `database/schema.sql`
  - Populate test data: `database/seed-data.sql`
		


# D. Testing the API with Postman
The Postman collection is designed to demonstrate the full application workflow, including:
  - Authentication
  - Task creation
  - Task retrieval
  - Task updates
  - Task assignment
  - Authorization checks
  - Validation checks
  - Logout
  - Running the Tests

## Steps to Test
1. After importing the collection:
2. Open the imported Task Management API collection.
3. Ensure the Spring Boot application is running on: `http://localhost:8080`
4. Execute the requests in numerical order:
    1. Login - Wrong Password (401)
    2. Login as Admin
    3. Create Task
    4. Create Task - Validation Error (400)
    5. Get All Tasks
    6. Update Task
    7. Login as User (ali)
    8. Get My Tasks
    9. Get My Task by Id
    10. Update My Task Status
    11. Delete Task as User (403)
    12. View Someone Else's Task (403)
    13. Login as Admin
    14. Assign Task to abu
    15. Delete Task
    16. Logout
    17. Get Tasks After Logout (401)

- Values can be viewed and modified under the collection's Variables tab.
- If all requests complete successfully and the tests pass, the application is functioning as expected.

### Alternative: Using Collection Runner
Alternatively, the entire collection can be executed using Postman's Collection Runner.
  


## Seed Data
| Id | Username | Password | Role  |
|----|----------|----------|-------|
| 1  | admin    | admin123 | ADMIN |
| 2  | abu      | user123  | USER  |
| 3  | ali      | user123  | USER  |

| Task id | Title  | Status      | Assigned to |
|---------|--------|-------------|-------------|
| 1       | Task 1 | IN_PROGRESS | ali         |
| 2       | Task 2 | NEW         | ali         |
| 3       | Task 3 | COMPLETED   | abu         |
| 4       | Task 4 | NEW         | (none)      |



## Project Structure

```
src/main/java/com/example/taskmanager
├── TaskManagementApplication.java
├── config/SecurityConfig.java            # URL rules (login open, rest needs login), BCrypt encoder
├── controller/AuthController.java        # POST /api/auth/login, /api/auth/logout
├── controller/TaskController.java        # /api/tasks endpoints + @PreAuthorize role checks
├── service/AuthService.java              # Login (check username + password) and logout
├── service/TaskService.java              # Task business rules ("USER only sees own tasks")
├── repository/UserRepository.java        # Spring Data JPA repositories
├── repository/TaskRepository.java
├── entity/User.java                      # JPA entities (Role / Status enums nested inside)
├── entity/Task.java                      # Task field validation (@NotBlank, @Size, @NotNull)
├── dto/LoginRequest.java                 # Request / response records
├── dto/TaskRequest.java
├── dto/StatusRequest.java
├── dto/TaskResponse.java
└── exception/GlobalExceptionHandler.java # Turns exceptions into JSON errors
src/main/resources/application.properties # Aiven database connection
database/schema.sql                       # Tables and indexes (run manually once)
database/seed-data.sql                    # Sample users and tasks
postman/                                  # Postman collection (one file)
```