# Library Management System

A Java 21 library administration system with two entry points: the original Swing desktop client and a Spring Boot browser application. Both use the same MySQL/JDBC service layer for authenticated administration, catalog and student management, issue/return workflows, configurable late fees, payment history, reports, and CSV export.

## Browser application and deployment

The deployable web application is Spring Boot; the Swing client is desktop-only and does not have a public browser URL. For local web use, configure MySQL, then run:

```powershell
mvn spring-boot:run
```

Open `http://localhost:8080`. The first browser visit creates the administrator if the database has no admin account. The application serves its browser UI and JSON API from one origin.

### Railway deployment

The repository includes a production `Dockerfile` and `railway.json`; Railway is the recommended option here because the app requires MySQL.

1. Push this project to a GitHub repository and sign in to Railway.
2. Create a Railway project from that GitHub repository and add a MySQL service to the same environment.
3. Connect to the MySQL service using Railway's connection details (enable its TCP proxy if connecting from MySQL Workbench) and execute `database/library_management.sql`. Ensure the `library_management` schema is accessible to the application user.
4. In the web service's Variables, add `SESSION_COOKIE_SECURE=true` and set:

   ```text
   LIBRARY_DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/library_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   LIBRARY_DB_USER=${{MySQL.MYSQLUSER}}
   LIBRARY_DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   ```

   Replace `MySQL` in those Railway references with the exact database service name. Enter secrets only in Railway's Variables UI; do not commit them. The app also understands `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, and `MYSQLPASSWORD` when `LIBRARY_DB_*` variables are absent.
5. Deploy the web service. Railway builds the Dockerfile, uses `/health` for its health check, and assigns the public domain under **Settings > Networking > Generate Domain**.

I cannot generate a public URL from this workspace: it requires your Railway account, GitHub repository connection, and production database credentials. Railway displays the real URL only after those steps and a passing health check. Do not use a URL until Railway has generated it.

For a local Docker build, run `docker build -t library-management-system .`, then start it with the database and `PORT` environment variables configured.

## Requirements

- JDK 21
- Apache Maven 3.9 or newer
- MySQL Server 8.0 or newer
- Visual Studio Code with Extension Pack for Java and Maven for Java

## Database setup

1. Start MySQL Server.
2. Import `database/library_management.sql` using MySQL Workbench, or from PowerShell:

   ```powershell
   mysql -u root -p < database/library_management.sql
   ```

3. Copy `src/main/resources/library.properties.example` to `library.properties` in the project root and set the connection URL, username, and password. The file is ignored by Git.

   ```properties
   db.url=jdbc:mysql://localhost:3306/library_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=your_mysql_user
   db.password=your_mysql_password
   ```

   Alternatively set `LIBRARY_DB_URL`, `LIBRARY_DB_USER`, and `LIBRARY_DB_PASSWORD` in the process environment. Environment variables take precedence.

The SQL script creates the database and all required tables, including `fine_payments` to retain each payment. It inserts example books, students, a default 14-day loan period, and a $1.00 daily fine. It intentionally does not insert a default admin or plaintext password: on the first launch, create the administrator in the setup screen. Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes.

## Build and run

From the project root:

```powershell
mvn clean test package
java -jar target/library-management-system-1.0.0.jar
```

The packaged JAR starts the Spring Boot web application. Open `http://localhost:8080` after it starts. To run the desktop Swing client instead:

```powershell
mvn exec:java -Dexec.mainClass=com.library.Main
```

The JAR includes the runtime dependencies. For local database settings, keep `library.properties` beside the JAR or use `LIBRARY_DB_URL`, `LIBRARY_DB_USER`, and `LIBRARY_DB_PASSWORD`.

## Visual Studio Code

1. Open this folder (`JAVA`) in VS Code.
2. Install **Extension Pack for Java** and **Maven for Java**.
3. Select a JDK 21 installation when prompted (or set `java.jdt.ls.java.home` to its path).
4. Configure the database using `library.properties` or environment variables.
5. Use **Terminal > Run Task > Maven: package** to build, or **Maven: run application** to launch the web app.
6. Use **Run and Debug > Library Web Application** for the browser app or **Library Desktop Client** for Swing after Maven dependencies have resolved.

## Application guide

- **Dashboard:** live counts for catalog titles, available copies, active loans, students, overdue loans, and paid fines.
- **Books:** add/edit/delete, search by title, author, ISBN, or ID, and sort the catalog. Inventory edits preserve copies already on loan; records referenced by history cannot be deleted.
- **Students:** add, edit, search, and delete student records. Student IDs are immutable after creation; students referenced by issue history cannot be deleted.
- **Issue Books:** choose a registered student and an available copy. The configured loan period determines the due date; stock decrement and issue creation commit together.
- **Returns & fines:** return an active loan, calculate overdue days and the current daily fine, inspect the return history, and make full or partial fine payments. Each payment is retained in history.
- **Reports:** view all books, available books, active issues, returns, overdue issues, registered students, and fine-payment collection; export the selected report to CSV.
- **Settings:** configure the daily fine and loan period. Changes apply to subsequent issues/returns.
- **Log out:** ends the current authenticated desktop session and returns to the login screen.

## Validation and tests

`mvn test` runs the unit tests for input validation and due-date calculations. The issue, return, inventory, and payment operations use JDBC transactions and prepared statements. For a full workflow check after MySQL setup, create an admin, add/edit/search/delete an unreferenced book and student, issue an available book, return it, record a partial and final payment where applicable, inspect the reports, then log out. Deleting records referenced by history is intentionally blocked by foreign keys.
