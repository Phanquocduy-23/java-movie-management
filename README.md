# Java Movie Management

A Java Swing desktop application for managing movies and user reviews,
built with JDBC and PostgreSQL.

Developed as a Professional Skills course project at Nam Can Tho University.

**Status: Work in progress.**

## Technology

- Java — target release 17
- Java Swing
- JDBC / PostgreSQL JDBC Driver
- PostgreSQL
- Maven project structure
- Visual Studio Code

## Implemented Features

- Movie list loaded from PostgreSQL
- Search by movie title
- Combined genre and release-year filters
- Movie details
- User registration, login and logout
- Password hashing with PBKDF2-HMAC-SHA256 and random salts
- ADMIN and USER roles
- Genre management: create, update, delete and reload
- Service-level permission checks for genre management
- Background database operations using SwingWorker

## Planned Features

- Movie creation and editing with multiple genres
- Transactions for movie and genre assignments
- Movie posters and visibility management
- User ratings and reviews
- Password changes and account management
- Statistics and pagination
- Standalone application packaging

## Project Structure

- model: data models
- dao: database access
- service: validation, authentication and authorization
- ui: Swing windows and dialogs
- util: password utilities
- database: schema and sample data scripts
- config: external database configuration

Java packages are located under:
src/main/java/vn/edu/nct/quanlyphim

## Run in VS Code

1. Install a compatible JDK and the Extension Pack for Java.
2. Install PostgreSQL and create a database named movie_management.
3. On a fresh database, execute:
   - database/01_schema.sql
   - database/02_seed.sql
4. Copy config/database.properties.example to config/database.properties.
5. Set your local database URL, username and password.
6. Open the project folder in VS Code and allow Java dependencies to load.
7. Run Main.java using the Java Run action.
8. Create an account through the registration screen.

Run with the project root as the working directory so that the application
can read config/database.properties.

The schema and seed scripts are intended for initial setup.
Do not rerun them on an existing populated database.

New registrations receive the USER role.
An authorized database owner can promote a chosen account to ADMIN.

A standalone release and Maven Wrapper are not included yet.

## Verification

Executable checks are available in the root Java package:

- PasswordCheck: password hashing and verification
- AuthCheck: registration, login and account status
- GenrePermissionCheck: USER access denial for genre management
- DatabaseCheck: database connectivity and sample movie retrieval

AuthCheck creates a temporary test account and removes it on completion.
GenrePermissionCheck expects an active USER account named quocduy.

## Configuration

The real config/database.properties file is excluded from Git.
Use the example configuration and supply your own database credentials.

## Author

Phan Quoc Duy
GitHub: https://github.com/Phanquocduy-23
