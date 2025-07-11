# Recipe Management System

A microservice-based recipe management application that allows users to manage their favorite recipes.

## Architecture

The application follows a microservice architecture with the following components:

- **API Gateway**: Entry point for all client requests, handles routing to appropriate services
- **Config Server**: Centralized configuration management
- **Discovery Service**: Service registry and discovery using Netflix Eureka
- **Identity Service**: Handles authentication and authorization using OAuth2/JWT
- **Recipe Service**: Core service for managing recipes

### Technology Stack
- Java 17
- Spring Boot 3.x
- Spring Cloud
- Spring Data JPA
- MySQL
- Hibernate
- Maven
- Docker
- OpenAPI/Swagger for API documentation
- JUnit and Mockito for testing

## System Requirements
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Docker and Docker Compose (optional for containerization)

## How to Run

### Using Maven
1. Start MySQL server
2. Configure database connection in each service's application.properties
3. Run the services in the following order:
   ```
   cd config-server && mvn spring-boot:run
   cd discovery-service && mvn spring-boot:run
   cd identity-service && mvn spring-boot:run
   cd recipe-service && mvn spring-boot:run
   cd api-gateway && mvn spring-boot:run
   ```

### Using Docker
```
docker-compose up
```

## API Documentation
Once the application is running, access the Swagger UI:
- API Gateway: http://localhost:8080/swagger-ui.html
- Recipe Service: http://localhost:8081/swagger-ui.html
- Identity Service: http://localhost:8082/swagger-ui.html

## Features
- User authentication and authorization
- CRUD operations for recipes
- Advanced filtering for recipes:
  - By vegetarian status
  - By number of servings
  - By ingredients (include/exclude)
  - Text search within instructions