<div align="center">
  <img src="https://img.icons8.com/color/96/000000/api.png" alt="Logo" width="80" height="80">

  <h1 align="center">Quro — AI Database Assistant</h1>

  <p align="center">
    An intelligent Spring Boot application that translates natural language questions into executable SQL queries.
    <br />
    <br />
    <a href="https://github.com/shriram1206/quro-db"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/shriram1206/quro-db">View Demo</a>
    ·
    <a href="https://github.com/shriram1206/quro-db/issues">Report Bug</a>
    ·
    <a href="https://github.com/shriram1206/quro-db/issues">Request Feature</a>
  </p>
</div>

<!-- BADGES -->
<div align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/OpenAI_API-412991?style=for-the-badge&logo=openai&logoColor=white" alt="OpenAI API">
</div>

---

## 📖 About The Project

**Quro** acts as an AI-powered database assistant, allowing users to interact with their MySQL databases using plain English instead of complex SQL syntax. Built with Spring Boot and powered by OpenAI's GPT-4o model, it bridges the gap between non-technical users and database management.

### ✨ Key Features

* **🗣️ Natural Language Processing:** Converts everyday English into accurate SQL queries.
* **⚡ Automatic Database Initialization:** Provisions the schema and populates sample data on startup.
* **🛡️ Secure Configuration:** Protects sensitive AI API keys using `.env` files.
* **🖥️ Interactive UI:** Clean, responsive web interface for querying and viewing results.
* **🧩 RESTful Architecture:** Industry-standard separation of controllers, services, and models.

---

## 🛠️ Supported Tech Stack

| Component | Technology | Version |
| --- | --- | --- |
| **Backend Framework** | Spring Boot | `3.5+` |
| **Language** | Java | `17+` |
| **Database** | MySQL | `8.x` |
| **ORM / Data Access** | Spring Data JPA / JDBC |
| **AI Integration** | GPT-4o (GitHub Models) |
| **Build Tool** | Maven |

---

## 🚀 Getting Started

Follow these instructions to get a local copy up and running.

### 📋 Prerequisites

Ensure you have the following installed:
* [Java 17 JDK](https://adoptium.net/)
* [MySQL Server](https://dev.mysql.com/downloads/mysql/)
* [Git](https://git-scm.com/)

### ⚙️ Installation

1. **Clone the repository:**
   ```bash
   git clone https://github.com/shriram1206/quro-db.git
   cd quro-db
   ```

2. **Setup the MySQL Database:**
   ```sql
   CREATE DATABASE quro_db;
   ```

3. **Secure Your API Keys:**
   Copy the environment template:
   ```bash
   cp .env.example .env
   ```
   Open `.env` and add your GitHub/OpenAI API token:
   ```properties
   OPENAI_API_KEY=your_actual_api_key_here
   ```

4. **Update Database Credentials *(Optional)*:**
   If your MySQL username/password differs from the defaults (`root` / `Quro@123`), update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

---

## 🏃‍♂️ Running the Application

Use the Maven wrapper to build and start the server:

**Windows:**
```cmd
.\mvnw.cmd spring-boot:run
```

**Mac / Linux:**
```bash
./mvnw spring-boot:run
```

🌐 **Access the UI at** 👉 `http://localhost:8081`

---

## 💡 Usage Example

1. Open your browser and navigate to the application dashboard.
2. Type a natural language query:
   > *"Show me the names and CGPAs of all Computer Science students who are in their 3rd year and have a CGPA above 8.0."*
3. Watch as Quro securely generates the SQL, executes it against your local database, and displays the exact results!

---

## 🔒 Security Best Practices
This project implements best practices for secret management. Access tokens are loaded via `.env`, which is strictly ignored via `.gitignore` to prevent leaks into version control.

<p align="center">
  <i>Developed with ❤️ by Shriram</i>
</p>

