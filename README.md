# Java CLI Client-Server Application

This project demonstrates a simple multi-threaded Java server and a command-line client
with user registration, login, and authorized command execution.

## Project Structure

The project is a Maven multi-module project:
- `L7-app` (parent POM)
  - `server` (module for the server application)
  - `client` (module for the client application)

## Prerequisites

- Java Development Kit (JDK) 11 or higher
- Apache Maven 3.6.0 or higher

## Building the Project

1. Navigate to the root directory of the project (`/Users/man-y/Ideas/L7/`).
2. Run the following Maven command to build both client and server:
   ```bash
   mvn clean install
   ```
   This will create two executable JAR files:
   - `server/target/server-1.0-SNAPSHOT.jar`
   - `client/target/client-1.0-SNAPSHOT.jar`

## Running the Application

1.  **Start the Server:**
    Open a terminal and navigate to the project's root directory.
    Run the server using the following command:
    ```bash
    java -jar server/target/server-1.0-SNAPSHOT.jar
    ```
    The server will start and listen on port 12345.

2.  **Start the Client:**
    Open another terminal (or multiple terminals for multiple clients).
    Navigate to the project's root directory.
    Run the client using the following command:
    ```bash
    java -jar client/target/client-1.0-SNAPSHOT.jar
    ```

    Once the client starts, you can use the following commands:
    - `register <username> <password>`: Registers a new user.
      (e.g., `register alice pass123`)
    - `login <username> <password>`: Logs in an existing user.
      (e.g., `login alice pass123`)
    - `echo <message>`: (Authorized command) Server echoes back the message. Requires login.
      (e.g., `echo Hello World`)
    - `mycommand`: (Authorized command) A sample command that only works if logged in.
    - `exit`: Disconnects the client from the server and exits the client application.

## Notes
- Passwords are stored in plain text in memory on the server side. This is for demonstration purposes only and is not secure for real applications.
- The server can handle multiple client connections concurrently using a thread pool.
- The `User.java` class is a simple POJO and not used for much beyond the initial concept for storing user objects. The current implementation uses a `ConcurrentHashMap<String, String>` directly for credentials for simplicity. 