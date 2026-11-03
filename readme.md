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

### Alternative: Using Collection Runner
Alternatively, the entire collection can be executed using Postman's Collection Runner.
  
## Test Accounts
The collection uses the following test accounts:
    Username	Password	Role	
    admin		  admin123	ADMIN
    abu		    user123		USER
    ali		    user123		USER

- These values can be viewed and modified under the collection's Variables tab.
- If all requests complete successfully and the tests pass, the application is functioning as expected.