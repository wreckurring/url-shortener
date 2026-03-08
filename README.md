# URL Shortener

A distributed URL shortening service designed to explore efficient redirection handling, and REST API design.

![Java](https://img.shields.io/badge/Java-17-orange.svg) ![Spring
Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)

------------------------------------------------------------------------

## Key Features

-   **Short Link Generation** - Deterministic and collision-safe short URL generation using a distributed Token Range strategy.
-   **RESTful API Design** - Clean endpoint structure for create, fetch, and redirect flows.
-   **Event-Driven Analytics** - Asynchronous click tracking and statistics processing using Apache Kafka.
-   **Scalable Architecture** - Designed for horizontal growth, high read traffic, and zero thread contention.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Spring Boot, Java 17 |
| **Database** | PostgreSQL (JPA) |
| **Cache** | Redis 7 |
| **Messaging**| Apache Kafka & Zookeeper |
| **Build Tool** | Maven & Docker Compose |

## Architecture

    Client → REST Controller → Service Layer → Repository → Database
                         ↓           ↓
                     Redirect    Kafka (Analytics)
                         ↓
                    Redis Cache

## Key Challenges Solved

-   Designed constant-time lookup for high-speed redirection using Redis.
-   Resolved thread contention issues in ID generation using `AtomicLong` and Redis range allocation.
-   Prevented cache-penetration attacks via Negative Caching for invalid short links.
-   Offloaded heavy analytics write operations to a Kafka consumer, preventing redirect thread blocking.

## Scalability Design

-   Read-heavy optimization for fast redirect performance using Redis caching.
-   Stateless backend enabling horizontal scaling.
-   Database indexing on short codes for O(log n) retrieval.
-   Distributed Token Range allocation (`INCRBY`) to prevent database sequence bottlenecks.

## Security

-   Input validation on URL creation requests.
-   Structured exception handling for safe API responses.
-   Cache penetration protection (Negative Caching).

## Project Structure

    url-shortener/
    ├── src/main/java/
    │   ├── controller/      # REST endpoints
    │   ├── service/         # Business logic & Kafka services
    │   ├── repository/      # Data access layer
    │   ├── model/           # Entity definitions
    │   └── util/            # Base62 Encoding
    ├── docker-compose.yml   # Container orchestration
    ├── pom.xml              # Maven configuration
    └── README.md

## Quick Start

``` bash
git clone [https://github.com/wreckurring/url-shortener.git](https://github.com/wreckurring/url-shortener.git)
cd url-shortener
docker-compose up --build -d
```

Server runs at:

    http://localhost:9090

## Future Improvements

-   **Tests** -- Add unit and integration tests
-   **Custom Aliases** -- User-defined short URLs
-   **Rate Limiting** -- Abuse prevention for public APIs

## License

MIT License
