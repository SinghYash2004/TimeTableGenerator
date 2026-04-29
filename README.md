# Timetable Generator - Intelligent Academic ERP System

An enterprise resource planning system that automates academic timetable generation using advanced constraint-aware algorithms. The system manages faculty, classrooms, and schedules while ensuring conflict-free allocations, supporting budget constraints, and providing comprehensive analytics dashboards.

## Overview

This project combines intelligent scheduling algorithms, a robust database layer, and a modern web interface to solve the complex problem of academic timetable generation. It supports multiple scheduling strategies including genetic algorithms, graph coloring, and greedy approaches.

## Key Features

- **Intelligent Scheduling**: Three powerful scheduling algorithms for different use cases
  - **Genetic Algorithm**: Evolutionary approach that mimics natural selection to find near-optimal timetables. Ideal for complex scenarios with multiple competing constraints. Supports configurable population size, mutation rates, and iteration limits for fine-tuned optimization
  - **Graph Coloring Scheduler**: Treats scheduling as a graph coloring problem, ensuring no conflicts exist at any node. Highly efficient for systems with strict non-overlapping requirements. Guarantees constraint satisfaction
  - **Greedy Scheduling**: Fast heuristic-based approach for rapid deployment and quick approximations. Perfect for prototyping or time-constrained scenarios. Delivers acceptable solutions in seconds rather than minutes
  - Configurable parameters for each algorithm accessible through the web dashboard
  
- **Advanced Constraint Management**
  - **Faculty Constraints**: Honor teacher availability windows, teaching preferences, maximum classes per day/week, preferred time slots, and subject specialization mapping
  - **Classroom Constraints**: Respect room capacity limits, facility type requirements (lab, lecture hall, seminar room), equipment availability, and building proximity preferences
  - **Time Slot Management**: Define working hours, break periods, and special scheduling windows. Prevent back-to-back classes with configurable buffer times
  - **Conflict Detection**: Real-time validation of scheduling conflicts including faculty double-booking, room overlaps, student group conflicts, and time-based impossibilities
  - **Department & Section Scheduling**: Organize timetables by academic departments and student sections with cross-departmental conflict resolution

- **Financial Management**: Comprehensive budget and resource allocation system
  - Track resource costs (classroom utilization, faculty hours, facilities)
  - Budget constraints for departmental allocations
  - Cost analysis by department, faculty member, and resource type
  - Financial reporting and spending forecasts
  - Resource efficiency metrics and ROI analysis

- **AI-Powered Risk Analysis**: Intelligent risk assessment and scheduling quality evaluation
  - Automatically identify high-risk scheduling scenarios before implementation
  - Predict potential conflicts based on historical data patterns
  - Suggest optimizations to reduce scheduling fragmentation
  - Quality scoring for generated timetables based on multiple metrics (faculty satisfaction, room utilization, student convenience)
  - Risk severity classification (critical, high, medium, low) with actionable recommendations

- **Comprehensive Reporting & Analytics**
  - **Timetable Export**: Generate and download timetables in CSV, PDF, and Excel formats
  - **Resource Utilization Reports**: Detailed analytics on classroom usage, faculty workload distribution, and time slot demand
  - **Interactive Dashboards**: Visual analytics with charts and graphs showing scheduling patterns, conflicts resolved, and algorithm performance metrics
  - **Conflict Analysis**: Detailed reports on resolved conflicts, near-miss scenarios, and constraint violations
  - **Performance Metrics**: Algorithm comparison reports, schedule stability analysis, and quality assurance metrics
  - **Audit Trails**: Complete history of all changes and schedule iterations

- **Full-Featured Web Dashboard**
  - **Dashboard Home**: Real-time overview of current schedules, pending conflicts, and system health
  - **Faculty Management**: Complete CRUD operations - add/edit/delete faculty with availability, specializations, and workload limits
  - **Classroom Management**: Configure available rooms with capacity, facilities, location, and scheduling restrictions
  - **Department & Section Configuration**: Manage academic structures, assign faculty to departments, organize student sections
  - **Timetable Builder**: Interactive visual timetable editor with drag-and-drop functionality and instant conflict feedback
  - **Planning & Configuration**: Set scheduling parameters, define academic calendar, configure algorithm preferences
  - **Optimization Engine**: One-click optimization with algorithm selection, parameter tuning, and real-time progress tracking
  - **Reports Module**: Generate, preview, and export various reports with customizable filters and date ranges
  - **Multi-User Access**: Role-based authentication (Admin, Department Head, Faculty, Viewer) with granular permission controls
  - **Responsive Design**: Works seamlessly on desktop, tablet, and mobile devices for on-the-go access

## Project Structure

```
TimetableGenerator/
├── erp-system/                    # Core ERP system
│   ├── src/                       # Java source files
│   │   ├── ai/                    # AI and risk analysis
│   │   ├── config/                # Database configuration
│   │   ├── dao/                   # Data access objects
│   │   ├── finance/               # Budget and finance management
│   │   ├── ga/                    # Genetic algorithm
│   │   ├── model/                 # Domain models
│   │   ├── reporting/             # Report generation
│   │   └── scheduling/            # Scheduling algorithms
│   ├── bin/                       # Compiled classes
│   ├── database.sql               # Database schema
│   ├── reports/                   # Generated reports
│   └── webapp/                    # Spring Boot web application
│       ├── src/main/java/com/erp/ # Web application code
│       ├── src/main/resources/    # Configuration and static files
│       └── pom.xml                # Maven dependencies
├── docs/                          # Documentation
├── lib/                           # External libraries
└── README.md                      # This file
```

## Technologies Used

- **Backend**: Java, Spring Boot
- **Frontend**: HTML5, CSS3, JavaScript
- **Database**: MySQL/SQL
- **Build Tool**: Maven
- **Scheduling Algorithms**: Genetic Algorithm, Graph Coloring, Greedy
- **Architecture**: MVC with DAO pattern

## Prerequisites
- Java 8 or higher
- Maven 3.6+
- MySQL 5.7 or higher
- Git

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd TimetableGenerator
```

### 2. Set Up Database

```bash
mysql -u root -p < erp-system/database.sql
```

Update database credentials in `erp-system/webapp/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/timetable_db
spring.datasource.username=root
spring.datasource.password=your_password
```

### 3. Build the Project

```bash
cd erp-system/webapp
mvn clean install
```

### 4. Run the Application

**Using Maven:**
```bash
mvn spring-boot:run
```

**Or using the PowerShell script:**
```powershell
.\run-webapp.ps1
```

The web application will be available at `http://localhost:8080`

## Usage

### Web Dashboard Features

- **Dashboard**: Overview of current schedules and conflicts
- **Faculty Management**: Add, edit, and manage faculty members
- **Rooms**: Configure available classrooms and capacity
- **Departments**: Manage academic departments and sections
- **Timetable**: View and generate optimized schedules
- **Planning**: Configure scheduling parameters and constraints
- **Optimization**: Run scheduling algorithms with custom parameters
- **Reports**: Generate and export timetable reports

### Command Line

Compile the core ERP system:
```bash
javac -d erp-system/bin erp-system/src/*.java erp-system/src/**/*.java
```

## Configuration

### Database Configuration
Edit `erp-system/webapp/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/timetable_db
spring.datasource.username=root
spring.datasource.password=password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

### Scheduling Parameters
Configure algorithm parameters in the web dashboard under **Planning** > **Optimization Settings**

## Key Components

### Scheduling Algorithms
- **GeneticAlgorithm.java**: Evolutionary approach for optimal timetable generation
- **GraphColoringScheduler.java**: Graph-based constraint satisfaction
- **GreedyScheduler.java**: Fast heuristic-based scheduling

### Data Access Layer
- Faculty, Classroom, Department, Section, Subject, TimeSlot management
- Transaction handling and database operations

### Business Logic
- **ConflictChecker.java**: Validates scheduling constraints
- **FitnessCalculator.java**: Evaluates schedule quality
- **RiskAnalyzer.java**: Identifies potential scheduling risks
- **BudgetManager.java**: Manages financial constraints

## Building and Deploying

### Build JAR
```bash
cd erp-system/webapp
mvn clean package
```

The packaged JAR will be in `target/erp-webapp-1.0.0.jar`

### Run JAR
```bash
java -jar erp-webapp-1.0.0.jar
```

## API Endpoints

The web application provides RESTful endpoints for:
- Faculty CRUD operations
- Room/Classroom management
- Department and section management
- Timetable generation and retrieval
- Report generation

Refer to the web dashboard or API documentation for complete endpoint details.

## Troubleshooting

### Database Connection Issues
- Verify MySQL is running
- Check database credentials in `application.properties`
- Ensure database schema is created

### Port Already in Use
- Change port in `application.properties`: `server.port=8081`

### Build Failures
- Run `mvn clean` first
- Verify Java version: `java -version`
- Check Maven installation: `mvn -version`

## Performance Tips

- Use Genetic Algorithm for complex scheduling scenarios
- Use Greedy Scheduler for quick, approximate solutions
- Adjust population size and iterations for GA based on dataset size
- Index frequently queried columns in database

## Future Enhancements

- Mobile application interface
- Advanced ML-based scheduling
- Real-time schedule adjustments
- Integration with student information systems
- Calendar synchronization
- Email notifications

## Support

For issues, questions, or suggestions, please refer to the documentation in the `docs/` folder or contact the development team.

## License

[Add appropriate license information here]

---

**Last Updated**: April 2026
