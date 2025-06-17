# 🔔 Notification Service

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Coverage](https://img.shields.io/badge/coverage-85%25-green.svg)]()
[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)]()

> Microservice responsible for creating and managing notifications for the Task Management Platform.

## 🎯 Service Overview

The Notification Service provides essential notification capabilities:
- Create notifications for task updates and events
- Retrieve user notifications with pagination
- Mark notifications as read
- Basic notification management

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Task Service  │───▶│ Apache Kafka    │───▶│ Notification    │
└─────────────────┘    │   (Events)      │    │   Service       │
┌─────────────────┐    └─────────────────┘    └─────────────────┘
│   User Service  │───▶                                 │
└─────────────────┘                                     ▼
                                              ┌─────────────────┐
                                              │   MongoDB       │
                                              │ (Notifications) │
                                              └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- MongoDB 5.0+
- Apache Kafka 2.8+
- Docker (optional)

### 🐳 Run with Docker

```bash
# Start MongoDB container
docker run -d \
  --name notification-mongo \
  -p 27017:27017 \
  -e MONGO_INITDB_DATABASE=notificationdb \
  mongo:5.0

# Start Kafka container
docker run -d \
  --name notification-kafka \
  -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  confluentinc/cp-kafka:latest

# Run the service
mvn spring-boot:run
```

### 🛠️ Local Development

```bash
# Clone and navigate
git clone  https://github.com/sapunethmini/TaskMaster-NotificationService.git
cd notification-service

# Install dependencies
mvn clean install

# Run tests
mvn test

# Start the service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The service will start on **http://localhost:8085**

## 📡 API Endpoints

### Core Notification Operations

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/notifications` | Create a new notification | ✅ |
| GET | `/api/notifications` | Get user notifications (paginated) | ✅ |
| GET | `/api/notifications/{id}` | Get notification by ID | ✅ |
| PUT | `/api/notifications/{id}/read` | Mark notification as read | ✅ |
| GET | `/api/notifications/unread-count` | Get unread notification count | ✅ |

### API Documentation

Access the interactive API documentation at: **http://localhost:8085/swagger-ui/index.html**

## 📋 Request/Response Examples

### Create Notification Request
```json
{
  "type": "TASK_ASSIGNED",
  "title": "New Task Assigned",
  "message": "You have been assigned a new task: 'Complete project documentation'",
  "userId": "uuid-user-456",
  "data": {
    "taskId": "uuid-task-789",
    "taskTitle": "Complete project documentation",
    "assignedBy": "johndoe",
    "priority": "HIGH",
    "dueDate": "2024-12-31T23:59:59Z"
  }
}
```

### Notification Response
```json
{
  "id": "uuid-notification-123",
  "type": "TASK_ASSIGNED",
  "title": "New Task Assigned",
  "message": "You have been assigned a new task: 'Complete project documentation'",
  "data": {
    "taskId": "uuid-task-789",
    "taskTitle": "Complete project documentation",
    "assignedBy": "johndoe",
    "priority": "HIGH",
    "dueDate": "2024-12-31T23:59:59Z"
  },
  "userId": "uuid-user-456",
  "isRead": false,
  "createdAt": "2024-06-17T10:30:00Z",
  "readAt": null,
  "metadata": {
    "source": "task-service",
    "category": "task_management"
  }
}
```

### Get Notifications Response
```json
{
  "content": [
    {
      "id": "uuid-notification-123",
      "type": "TASK_ASSIGNED",
      "title": "New Task Assigned",
      "message": "You have been assigned a new task",
      "userId": "uuid-user-456",
      "isRead": false,
      "createdAt": "2024-06-17T10:30:00Z"
    }
  ],
  "totalElements": 25,
  "totalPages": 3,
  "currentPage": 0,
  "pageSize": 10,
  "hasNext": true,
  "hasPrevious": false
}
```

## 🔧 Configuration

### Application Properties

```yaml
# application.yml
server:
  port: 8085

spring:
  application:
    name: notification-service
  
  data:
    mongodb:
      uri: mongodb://localhost:27017/notificationdb
      auto-index-creation: true
  
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
    consumer:
      group-id: notification-service-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.taskmanagement.events"

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true

logging:
  level:
    com.taskmanagement.notificationservice: DEBUG
    org.springframework.kafka: INFO
```

### Environment Variables

```bash
# MongoDB Configuration
MONGODB_URI=mongodb://localhost:27017/notificationdb

# Kafka Configuration
KAFKA_SERVERS=localhost:9092

# Service Configuration
SERVER_PORT=8085
```

## 🗄️ Database Schema (MongoDB)

### Notifications Collection

```javascript
// notifications collection
{
  "_id": ObjectId("..."),
  "notificationId": "uuid-notification-123",
  "type": "TASK_ASSIGNED",
  "title": "New Task Assigned",
  "message": "You have been assigned a new task",
  "data": {
    "taskId": "uuid-task-789",
    "taskTitle": "Complete documentation",
    "assignedBy": "johndoe"
  },
  "userId": "uuid-user-456",
  "isRead": false,
  "createdAt": ISODate("2024-06-17T10:30:00Z"),
  "readAt": null,
  "metadata": {
    "source": "task-service",
    "category": "task_management"
  }
}
```

### Indexes

```javascript
// Create indexes for performance
db.notifications.createIndex({ "userId": 1, "createdAt": -1 })
db.notifications.createIndex({ "userId": 1, "isRead": 1 })
db.notifications.createIndex({ "type": 1 })
```

## 🔄 Event Processing

### Kafka Event Listeners

```java
@KafkaListener(topics = "task.assigned")
public void handleTaskAssigned(TaskAssignedEvent event) {
    CreateNotificationRequest notification = CreateNotificationRequest.builder()
            .type(NotificationType.TASK_ASSIGNED)
            .userId(event.getAssigneeId())
            .title("New Task Assigned")
            .message("You have been assigned: " + event.getTaskTitle())
            .data(Map.of(
                "taskId", event.getTaskId(),
                "taskTitle", event.getTaskTitle(),
                "assignedBy", event.getAssignedBy()
            ))
            .build();
    
    notificationService.createNotification(notification);
}
```

### Supported Notification Types

```java
public enum NotificationType {
    TASK_CREATED,
    TASK_ASSIGNED,
    TASK_COMPLETED,
    TASK_OVERDUE,
    TASK_DUE_REMINDER,
    TASK_STATUS_CHANGED,
    TASK_COMMENT_ADDED,
    USER_MENTIONED
}
```

## 🧪 Testing

### Integration Tests

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class NotificationServiceIntegrationTest {

    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:5.0")
            .withExposedPorts(27017);

    @Test
    void shouldCreateNotificationSuccessfully() {
        // Given
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .type(NotificationType.TASK_ASSIGNED)
                .userId("user-123")
                .title("Test Notification")
                .message("Test message")
                .build();

        // When
        NotificationResponse response = notificationService.createNotification(request);

        // Then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getType()).isEqualTo(NotificationType.TASK_ASSIGNED);
        assertThat(response.isRead()).isFalse();
    }

    @Test
    void shouldGetUserNotificationsSuccessfully() {
        // Given
        String userId = "user-123";
        
        // When
        Page<NotificationResponse> notifications = notificationService
                .getUserNotifications(userId, PageRequest.of(0, 10));

        // Then
        assertThat(notifications.getContent()).isNotEmpty();
        assertThat(notifications.getTotalElements()).isGreaterThan(0);
    }
}
```

## 📊 Health Checks

```java
@Component
public class NotificationHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Check MongoDB connectivity
            mongoTemplate.getCollection("notifications").countDocuments();
            
            // Check Kafka connectivity
            kafkaTemplate.send("health-check", "ping").get(5, TimeUnit.SECONDS);
            
            return Health.up()
                    .withDetail("mongodb", "UP")
                    .withDetail("kafka", "UP")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

## 🚀 Deployment

### Docker Configuration

```dockerfile
FROM openjdk:17-jre-slim

COPY target/notification-service-1.0.0.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Docker Compose

```yaml
version: '3.8'
services:
  notification-service:
    build: .
    ports:
      - "8085:8085"
    environment:
      - MONGODB_URI=mongodb://mongo:27017/notificationdb
      - KAFKA_SERVERS=kafka:9092
    depends_on:
      - mongo
      - kafka

  mongo:
    image: mongo:5.0
    ports:
      - "27017:27017"
    environment:
      - MONGO_INITDB_DATABASE=notificationdb

  kafka:
    image: confluentinc/cp-kafka:latest
    ports:
      - "9092:9092"
    environment:
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
```

## 🐛 Troubleshooting

### Common Issues

**MongoDB Connection Issues**
```bash
# Check MongoDB connectivity
mongosh mongodb://localhost:27017/notificationdb

# Verify service health
curl http://localhost:8085/actuator/health
```

**Kafka Consumer Issues**
```bash
# Check consumer group status
kafka-consumer-groups --bootstrap-server localhost:9092 \
  --describe --group notification-service-group

# List topics
kafka-topics --bootstrap-server localhost:9092 --list
```

**API Testing**
```bash
# Test notification creation
curl -X POST http://localhost:8085/api/notifications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "type": "TASK_ASSIGNED",
    "title": "Test Notification",
    "message": "Test message",
    "userId": "user-123"
  }'

# Get user notifications
curl -X GET "http://localhost:8085/api/notifications?page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

## 📚 Related Documentation

- [Main Project README](../README.md)
- [Task Service Documentation](../task-service/README.md)
- [User Service Documentation](../user-service/README.md)
- [API Documentation](http://localhost:8085/swagger-ui/index.html)

---

**Service Contact**: Notification Service Team | **Last Updated**: June 2025
