# Quro — AI Database Assistant

Quro is an intelligent Spring Boot application that translates natural language questions into executable SQL queries. It acts as an AI-powered database assistant, allowing users to interact with their MySQL databases using plain English instead of complex SQL syntax.

## 🚀 Features

- **Natural Language Processing:** Converts everyday English into accurate SQL queries using OpenAI's GPT-4o.
- **Automatic Database Initialization:** Automatically provisions the schema and populates sample data (Students, Faculty, Courses, etc.) on startup.
- **Secure Configuration:** Protects sensitive AI API keys using environment variables (`.env`).
- **Interactive UI:** Provides a clean, responsive web interface for querying and viewing results.
- **RESTful API Architecture:** Clean separation of concerns with dedicated controllers, services, and models.

## 🛠️ Tech Stack

- **Backend:** Java 17, Spring Boot 3.5
- **Database:** MySQL, Spring Data JPA / JDBC
- **AI Integration:** OpenAI API (GPT-4o) via GitHub Models
- **Build Tool:** Maven
- **Frontend:** Vanilla HTML, CSS, JavaScript

## 📋 Prerequisites

Before you begin, ensure you have met the following requirements:
- **Java 17** installed
- **MySQL Server** installed and running
- **Git** installed

## ⚙️ Setup & Installation

**1. Clone the repository:**
```bash
git clone https://github.com/shriram1206/quro-db.git
cd quro-db
```

**2. Setup the MySQL Database:**
Create a new database named `quro_db` in your MySQL server.
```sql
CREATE DATABASE quro_db;
```

**3. Configure Environment Variables:**
Copy the example environment file to secure your API key.
```bash
cp .env.example .env
```
Open the `.env` file and replace the placeholder with your actual OpenAI/GitHub API key:
```properties
OPENAI_API_KEY=your_actual_api_key_here
```

**4. Update Database Credentials (Optional):**
If your MySQL username isn't `root` or your password isn't `Quro@123`, update these fields in `src/main/resources/application.properties`:
```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

## 🏃‍♂️ Running the Application

Use the Maven wrapper to build and run the application:

**Windows:**
```cmd
.\mvnw.cmd spring-boot:run
```

**Mac/Linux:**
```bash
./mvnw spring-boot:run
```

Once running, open your web browser and navigate to:
**http://localhost:8081**

## 💡 Usage Example

1. Open the UI at `http://localhost:8081`.
2. Type a natural language query, for example: 
   > *"Show me the names and CGPAs of all Computer Science students who are in their 3rd year."*
3. Quro will generate the SQL query, execute it against the MySQL database, and return the formatted results.

## 🔒 Security Note
This project uses a `.env` file to separate secrets from the source code. The `.env` file is included in `.gitignore` to prevent sensitive credentials from being committed to version control.
