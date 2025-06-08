# Recipe Management System - Architecture Documentation

## System Overview

The Recipe Management System is designed as a microservice-based application that allows users to manage their favorite recipes. Users can add, update, remove, and fetch recipes, as well as filter recipes based on various criteria such as vegetarian status, number of servings, specific ingredients, and text search within instructions.

## Architecture Design

The system follows a microservice architecture pattern with the following components:

### 1. API Gateway
- Entry point for all client requests
- Routes requests to appropriate services
- Handles authentication validation
- Provides API documentation via Swagger UI

### 2. Config Server
- Centralized configuration management
- Provides configuration to all services
- Can use Git-based configuration repository

### 3. Discovery Service
- Service registry and discovery using Netflix Eureka
- Allows services to find and communicate with each other
- Enables dynamic scaling

### 4. Identity Service
- Handles user authentication and authorization
- Manages user registration and login
- Issues JWT tokens for authenticated users
- Validates tokens for protected endpoints

### 5. Recipe Service
- Core service for managing recipes
- Provides CRUD operations for recipes
- Implements recipe filtering functionality
- Manages recipe ownership and permissions

### 6. Observability Stack
- **Prometheus**: Metrics collection and storage
- **Grafana**: Metrics visualization and dashboards
- **Micrometer**: Metrics instrumentation
- **Spring Boot Actuator**: Exposes metrics endpoints
- **Distributed Tracing**: Using Brave and Zipkin

## Technology Stack

- **Java 17**: Core programming language
- **Spring Boot 3.x**: Application framework
- **Spring Cloud**: Microservice ecosystem
- **Spring Data JPA**: Data access layer
- **MySQL**: Primary database
- **Hibernate**: ORM for database interaction
- **Maven**: Build and dependency management
- **Docker**: Containerization
- **JWT**: Authentication mechanism
- **Swagger/OpenAPI**: API documentation
- **Prometheus**: Metrics collection
- **Grafana**: Metrics visualization
- **JUnit/Mockito**: Testing framework

## API Endpoints

### Identity Service

- `POST /auth/signup`: Register a new user
- `POST /auth/login`: Authenticate user and get token

### Recipe Service

- `GET /recipes`: Get all recipes
- `GET /recipes/{id}`: Get recipe by ID
- `POST /recipes`: Create a new recipe
- `PUT /recipes/{id}`: Update an existing recipe
- `DELETE /recipes/{id}`: Delete a recipe
- `POST /recipes/filter`: Filter recipes based on criteria
- `GET /recipes/my-recipes`: Get recipes created by the authenticated user

## Data Model

### User
- id: Long
- username: String
- email: String
- password: String
- roles: Set<String>

### Recipe
- id: Long
- name: String
- description: String
- vegetarian: boolean
- servings: int
- instructions: String
- preparationTime: Integer
- cookingTime: Integer
- ingredients: List<Ingredient>
- createdBy: String

### Ingredient
- id: Long
- name: String
- amount: String
- unit: String
- recipe: Recipe

## Security

The system implements a JWT-based authentication mechanism:

1. Users authenticate via the Identity Service
2. Upon successful authentication, a JWT token is issued
3. The token contains user identity and roles
4. The token is sent in the Authorization header for subsequent requests
5. The API Gateway and Recipe Service validate the token
6. Authorization is enforced based on user roles and resource ownership

## Filtering Capabilities

The Recipe Service provides robust filtering capabilities:

1. Filter by vegetarian status
2. Filter by number of servings
3. Filter by included ingredients
4. Filter by excluded ingredients
5. Filter by text in instructions
6. Combine multiple filters

## Observability and Monitoring

The system includes comprehensive observability features:

### Metrics Collection
- Each service exposes metrics via Spring Boot Actuator
- Prometheus scrapes metrics from each service
- Custom metrics track business operations (recipe creation, updates, etc.)
- Method execution times are measured using AOP
- JVM metrics (memory, CPU, garbage collection) are collected

### Dashboards
- Grafana dashboards visualize system metrics
- Recipe Service Dashboard: Shows recipe operations, filter usage, HTTP requests
- JVM Dashboard: Shows memory usage, CPU usage, garbage collection, thread counts

### Health Checks
- Spring Boot Actuator provides health endpoints
- Services report health status to the discovery service
- Prometheus monitors service availability

### Tracing
- Distributed tracing using Brave and Zipkin
- Traces span across service boundaries
- Performance bottlenecks can be identified

## Testing Strategy

The system includes multiple levels of testing:

1. **Unit Tests**: Test individual components in isolation
2. **Integration Tests**: Test interactions between components
3. **Repository Tests**: Test data access layer
4. **Controller Tests**: Test API endpoints
5. **Security Tests**: Test authentication and authorization

## Deployment

The system is deployed using Docker containers:

1. Each service is packaged as a Docker image
2. Docker Compose is used for local deployment
3. Environment variables are used for configuration
4. Services communicate via the discovery service
5. Prometheus and Grafana containers collect and visualize metrics
