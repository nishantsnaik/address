## README.md
```md
# Address Service
Spring Boot microservice managing billing & shipping addresses.

## How to Run
### 1. Start MongoDB (local)
```
or
```
docker run -d -p 27017:27017 --name mongo mongo:7
```


### 2. Run the app
```
mvn spring-boot:run
```


### 3. Test the API
Create:
```
curl -X POST http://localhost:8081/addresses
-H "Content-Type: application/json"
-d '{"line1":"123 Main St","city":"Austin","state":"TX","zip":"73301"}'
```


Get all:
```
curl http://localhost:8081/addresses
```

## Tech Stack
- Spring Boot 3
- MongoDB
- Redis Cache
- Spring Cloud Eureka Client
- Spring Cloud Config Client
- Docker


## Run locally
```
mvn clean package -DskipTests
java -jar target/address-service-1.0.0.jar
```


## Docker Build
```
docker build -t address-service .
```


## Kubernetes & Spring Cloud
Service will register with Eureka and load config from config-server.