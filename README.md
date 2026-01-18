# Distributed URL Shortener

A high-performance URL shortening service built with **Spring Boot** and **PostgreSQL**.

## Features
* **Base62 Encoding**: Converts database IDs into short, user-friendly strings.
* **Redirection**: Automatically redirects short codes to original long URLs.
* **Persistent Storage**: Uses PostgreSQL for secure data management.

## Tech Stack
* **Java 17**
* **Spring Boot 3.5.9**
* **Spring Data JPA**
* **PostgreSQL**

## How to Run
1. Ensure a PostgreSQL database named `url_shortener` exists.
2. Set your database password as an environment variable:
   `$env:DB_PASSWORD='your_password'`
3. Run the app:
   `.\mvnw spring-boot:run`