
# BugTracker - Software Bug & Ticket Tracking System

A Jira-style bug tracker built with **Java Spring Boot**, **MySQL**, **Maven** and a **Bootstrap** frontend
(navy / white / purple SaaS design, responsive, with a sidebar and dashboard).

---

## 1. Demo logins

| Role      | Username  | Password    | What this role can do                                                                 |
|-----------|-----------|-------------|---------------------------------------------------------------------------------------|
| Admin     | `admin`   | `admin123`  | Everything: edit / delete any ticket, assign developers, change any status            |
| Developer | `dev`     | `dev123`    | Report bugs, comment, move **their assigned** tickets Open → In Progress → Resolved   |
| Developer | `dev2`    | `dev123`    | Same as above (second developer)                                                      |
| Tester    | `tester`  | `tester123` | Report bugs, edit own tickets, delete own *Open* tickets, **Close** or **re-open** tickets |
| Tester    | `tester2` | `tester123` | Same as above (second tester)                                                         |

Everybody can view all tickets, search/filter them and add comments.
Log in as different roles to see that buttons appear/disappear depending on what the role is allowed to do.

---

## 2. How to run it on a MacBook (step by step)

### Step 1 - Install the tools (one time)

Open **Terminal** and run these commands. (If you do not have Homebrew yet, install it first from <https://brew.sh>.)

```bash
# Java 21 (any JDK from 17 to 24 works) + Maven + MySQL
brew install --cask temurin@21
brew install maven mysql
```

Tell your Mac to use Java 21 for Maven (copy-paste as one block, then **close and reopen Terminal**):

```bash
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 21)' >> ~/.zshrc
```

Check it worked:

```bash
java -version      # should say 21
mvn -version       # should show "Java version: 21..."
```

### Step 2 - Start MySQL and set the root password

```bash
brew services start mysql
mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root';"
```

(A fresh Homebrew MySQL has **no** root password, so the command above sets it to `root`.
If you already have a password, use `mysql -u root -p` instead and remember your password for Step 3.)

You do **not** need to create the database or the tables - the app creates them automatically.

### Step 3 - Check the database settings

Open `src/main/resources/application.properties`. The defaults are:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

If your MySQL username/password are different, change them here.

### Step 4 - Run the application

```bash
cd path/to/bug-tracker        # the folder that contains pom.xml
mvn spring-boot:run
```

The first start downloads libraries (a few minutes). Wait until you see
`Started BugTrackerApplication` and `Demo data created: 5 users, 14 tickets, 12 comments`.

### Step 5 - Open the app

Go to **<http://localhost:8080>** in your browser and log in with one of the demo accounts above.
Stop the server with `Ctrl + C` in the Terminal.

### Optional - run from an IDE

* **IntelliJ IDEA:** File → Open → select the `bug-tracker` folder (it detects `pom.xml`), then open
  `BugTrackerApplication.java` and press the green ▶ button.
* **VS Code:** install the "Extension Pack for Java" and "Spring Boot Extension Pack", open the folder and
  click *Run* above the `main` method.

### Optional - build one runnable file

```bash
mvn clean package
java -jar target/bug-tracker-1.0.0.jar
```

### Resetting the demo data

Demo data is inserted only when the database is empty. To start fresh:

```bash
mysql -u root -p -e "DROP DATABASE bugtracker;"
```

then start the app again.

### Troubleshooting

| Problem | Fix |
|---------|-----|
| `Access denied for user 'root'@'localhost'` | Username/password in `application.properties` do not match MySQL (Step 2/3). |
| `Communications link failure` / `Connection refused` | MySQL is not running: `brew services start mysql`. |
| `Port 8080 was already in use` | Stop the other program, or add `server.port=8081` to `application.properties`. |
| `release version 17 not supported` / `invalid target release` | Maven is using an old Java. Re-check `JAVA_HOME` (Step 1). |
| Page shows "Cannot reach the server" | The Spring Boot app is not running (Step 4). |

---

## 3. Features

1. Login page with **Admin / Developer / Tester** roles (session based, passwords stored as BCrypt hashes)
2. **Dashboard** - total, open, in-progress and resolved bugs, status donut, priority bars, developer workload, recent tickets
3. **Report a Bug** form - title, description, priority, severity
4. **Unique ticket IDs** generated automatically (`BUG-1001`, `BUG-1002`, ...)
5. **All Tickets** table
6. **Search and filter** by text, status, priority, severity and assignee ("Assigned to me" shortcut)
7. **Assign** tickets to developers (admin)
8. **Update status**: Open, In Progress, Resolved, Closed
9. **Edit and delete** tickets
10. **Ticket details** page with **comments**
11. **Reports** page with statistics and **Export CSV**

---

## 4. Project structure

```
bug-tracker/
├── pom.xml                               Maven file (libraries + Java version)
├── database/schema.sql                   Reference SQL (optional - the app creates tables itself)
└── src/main/
    ├── java/com/bugtracker/
    │   ├── BugTrackerApplication.java    main() - starts the server
    │   ├── config/        AppConfig (password encoder), WebConfig (login check), DataInitializer (demo data)
    │   ├── controller/    REST endpoints: Auth, Ticket, Comment, User, Dashboard
    │   ├── dto/           Small classes that carry JSON between browser and server
    │   ├── exception/     Our own exceptions + GlobalExceptionHandler
    │   ├── model/         Database entities: BaseEntity, User, Ticket, Comment + enums
    │   ├── repository/    Interfaces that talk to MySQL (Spring Data JPA)
    │   ├── security/      Role permissions (interface + 3 role classes) and the login gatekeeper
    │   ├── service/       Business logic: TicketService (interface) + TicketServiceImpl, ...
    │   └── util/          TicketCodeGenerator (BUG-1001 ...)
    └── resources/
        ├── application.properties        Database settings
        └── static/                       The frontend (served by Spring Boot)
            ├── login.html  dashboard.html  tickets.html  report.html  ticket.html  reports.html
            ├── css/app.css               Theme and layout
            ├── js/                       app.js (shared code) + one JavaScript file per page
            └── vendor/                   Bootstrap 5 + Bootstrap Icons (included, works offline)
```

**How a request flows:** Browser (JavaScript `fetch`) → `Controller` → `Service` (rules) → `Repository` → MySQL,
and the answer comes back the same way as JSON.

---

## 5. Where each OOP concept is used (handy for your viva)

| Concept | Where |
|---------|-------|
| **Classes & objects** | `User`, `Ticket`, `Comment`, services, controllers - e.g. `new Ticket()` in `TicketServiceImpl.create()` |
| **Encapsulation** | Private fields + getters/setters in every model class (`Ticket.java`); `AuthService` keeps the password check private |
| **Inheritance** | `User`, `Ticket`, `Comment` **extend** `BaseEntity`; `ResourceNotFoundException`, `ForbiddenException`... **extend** `BugTrackerException`; `AdminPermissions` etc. extend `AbstractRolePermissions` |
| **Abstraction** | `BaseEntity` and `AbstractRolePermissions` are `abstract` classes; `BaseEntity.describe()` is an abstract method |
| **Interfaces** | `RolePermissions`, `TicketService` (implemented by `TicketServiceImpl`), the repository interfaces |
| **Polymorphism** | `PermissionFactory.forRole(role)` returns a `RolePermissions` - the same call `canDeleteTicket()` gives a different answer for Admin, Developer and Tester. Also `describe()` is implemented differently in each entity, and `GlobalExceptionHandler` handles all child exceptions through the parent type |
| **Collections** | `List<Ticket>` and `List<TicketResponse>` in `TicketServiceImpl.search()`, `LinkedHashMap` for the statistics, `List.of(...)` for allowed statuses |
| **Exception handling** | Custom exception hierarchy in `exception/`, thrown by services and turned into JSON errors by `GlobalExceptionHandler`; the browser shows the message |
| **Enums** | `Status`, `Priority`, `Severity`, `Role` |

---

## 6. REST API (all URLs start with `/api`, everything except login needs a logged-in session)

| Method | URL | Purpose |
|--------|-----|---------|
| POST | `/auth/login` | Log in `{username, password}` |
| POST | `/auth/logout` | Log out |
| GET | `/auth/me` | Who is logged in |
| GET | `/tickets?search=&status=&priority=&severity=&assigneeId=` | List / search / filter tickets (`assigneeId=0` = unassigned) |
| POST | `/tickets` | Report a bug `{title, description, priority, severity}` |
| GET | `/tickets/{id}` | One ticket |
| PUT | `/tickets/{id}` | Edit a ticket |
| DELETE | `/tickets/{id}` | Delete a ticket (and its comments) |
| PUT | `/tickets/{id}/status` | Change status `{status}` |
| PUT | `/tickets/{id}/assign` | Assign `{assigneeId}` (`null` = unassign) |
| GET / POST | `/tickets/{id}/comments` | Read / add comments |
| GET | `/users?role=DEVELOPER` | List users |
| GET | `/dashboard/stats` | Numbers for dashboard and reports |

---

## 7. Ideas if you want to extend it later

File attachments, e-mail notifications, a user management page, ticket history/audit log, pagination.
