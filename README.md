# Distributed URL Shortener

This is a containerized URL shortening service built with a distributed architecture to ensure scalability and speed. It utilizes a "Read-Aside" caching pattern to minimize database load and provide lightning-fast redirections.

## Key Features

* **Modern Interactive UI**: A clean, responsive frontend built with Tailwind CSS.
* **Distributed Caching**: Uses **Redis** as a speed layer to serve popular links from memory, bypassing the database for frequent requests.
* **Persistent Storage**: Reliable data management using **PostgreSQL**.
* **Base62 Encoding**: Custom utility to convert unique database IDs into short, user-friendly strings.
* **Fully Containerized**: Orchestrated with **Docker Compose** for consistent environment setup across any machine.

## Tech Stack

* **Backend**: Java 17, Spring Boot 3.5.9, Spring Data JPA.
* **Caching**: Redis.
* **Database**: PostgreSQL 15.
* **Frontend**: HTML5, Tailwind CSS, JavaScript.
* **DevOps**: Docker, Docker Compose.

## Setup and Installation

### Prerequisites

* Docker Desktop installed and running.
* Java 17 and Maven (or use the included `mvnw` wrapper).

### 1. Build the Application

Package the Spring Boot application into an executable JAR file:

```powershell
.\mvnw clean package -DskipTests

```

### 2. Launch with Docker Compose

Run the entire distributed system (App, Postgres, and Redis) with a single command. Replace `your_password` with your desired database password:

```powershell
$env:DB_PASSWORD='your_password'; docker-compose up --build

```

### 3. Access the Service

* **Frontend**: Open [http://localhost:8080](https://www.google.com/search?q=http://localhost:8080) to use the interactive dashboard.
* **API**: You can also shorten URLs via POST requests to `/shorten`.

## 📂 Project Structure

* `src/main/java/.../service/UrlService.java`: Logic for coordinating between the DB and Redis cache.
* `src/main/resources/static/index.html`: The interactive ZipLink web interface.
* `docker-compose.yml`: Defines the multi-container environment for the distributed system.