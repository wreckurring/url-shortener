# URL Shortener

A distributed URL shortening service designed to explore scalable ID generation, efficient redirection handling, and REST API design.

![Java](https://img.shields.io/badge/Java-17-orange.svg) ![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)

------------------------------------------------------------------------

## Key Features

-   **Short Link Generation** - Deterministic and collision-safe short URL
-   **RESTful API Design** - Clean endpoint structure for create, fetch, and redirect flows
-   **Persistence Layer** - Reliable storage using relational database indexing
-   **Scalable Architecture** - Designed for horizontal growth and high read traffic

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Spring Boot, Java 17 |
| **Database** | Relational DB (JPA) |
| **Cache** | Redis 7 |
| **Build Tool** | Maven |

## Architecture

    Client → REST Controller → Service Layer → Repository → Database
                         ↓
                     Redirect Logic

## Key Challenges Solved

-   Designed constant-time lookup for high-speed redirection.
-   Ensured database indexing for efficient large-scale reads.
-   Handled browser requests separately

## Scalability Design

-   Read-heavy optimization for fast redirect performance.
-   Stateless backend enabling horizontal scaling.
-   Database indexing on short codes for O(log n) retrieval.
-   Clean separation of concerns for future caching layer integration.

## Security

-   Input validation on URL creation requests.
-   Structured exception handling for safe API responses.

## Project Structure

    url-shortener/
    ├── src/main/java/
    │   ├── controller/      # REST endpoints
    │   ├── service/         # Business logic
    │   ├── repository/      # Data access layer
    │   ├── model/           # Entity definitions
    │   └── dto/             # Request/response objects
    ├── pom.xml              # Maven configuration
    └── README.md

## Quick Start

``` bash
git clone https://github.com/wreckurring/url-shortener.git
cd url-shortener
mvn spring-boot:run
```

Server runs at:

    http://localhost:9090

## Future Improvements

-   **Analytics** -- Click tracking and usage statistics
-   **Custom Aliases** -- User-defined short URLs
-   **Rate Limiting** -- Abuse prevention for public APIs
-   **Distributed ID Generation** -- Snowflake ULID-based scaling

## Testing

-   Unit tests for service logic
-   Integration tests for API endpoints
-   Edge case testing for invalid/expired URLs

## License

MIT License
