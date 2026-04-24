# Timetable Generator - Intelligent Academic ERP System

An enterprise resource planning system that automates academic timetable generation using advanced constraint-aware algorithms. The system manages faculty, classrooms, and schedules while ensuring conflict-free allocations, supporting budget constraints, and providing comprehensive analytics dashboards.

## Overview

This project combines intelligent scheduling algorithms, a robust database layer, and a modern web interface to solve the complex problem of academic timetable generation. It supports multiple scheduling strategies including genetic algorithms, graph coloring, and greedy approaches.

## Key Features

- **Intelligent Scheduling**: Multiple scheduling algorithms
  - Genetic Algorithm for optimization
  - Graph Coloring for constraint satisfaction
  - Greedy scheduling for rapid deployment
  
- **Constraint Management**
  - Faculty availability and preferences
  - Room/classroom capacity and type constraints
  - Time slot conflicts detection
  - Department and section scheduling

- **Financial Management**: Budget tracking and resource allocation

- **Risk Analysis**: AI-powered risk assessment for scheduling conflicts

- **Reporting & Analytics**
  - Timetable generation and export
  - Resource utilization reports
  - Analytics dashboards
  - Conflict analysis

- **Web Dashboard**
  - Faculty management
  - Room/classroom management
  - Department and section configuration
  - Real-time optimization interface
  - Multi-user access with role-based controls

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
