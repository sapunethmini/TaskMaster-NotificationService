package org.example.service;

import org.example.model.NotificationEvent;
import org.example.model.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class KafkaListenerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaListenerService.class);

    private final NotificationService notificationService;

    @Autowired
    public KafkaListenerService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "task-events", groupId = "notification-service-group")
    public void handleTaskEvent(NotificationEvent event) {
        try {
            logger.info("Received task event: {} for user: {}", event.getEventType(), event.getUserId());

            String title = generateNotificationTitle(event.getEventType(), event.getTaskTitle());
            String message = generateNotificationMessage(event.getEventType(), event);

            NotificationRequest request = new NotificationRequest(
                    event.getUserId(),
                    event.getUserEmail(),
                    title,
                    message
            );
            request.setTaskId(event.getTaskId());

            notificationService.createNotification(request);

        } catch (Exception e) {
            logger.error("Error processing task event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "user-events", groupId = "notification-service-group")
    public void handleUserEvent(NotificationEvent event) {
        try {
            logger.info("Received user event: {} for user: {}", event.getEventType(), event.getUserId());

            String title = generateNotificationTitle(event.getEventType(), null);
            String message = generateNotificationMessage(event.getEventType(), event);

            NotificationRequest request = new NotificationRequest(
                    event.getUserId(),
                    event.getUserEmail(),
                    title,
                    message
            );

            notificationService.createNotification(request);

        } catch (Exception e) {
            logger.error("Error processing user event: {}", e.getMessage());
        }
    }

    private String generateNotificationTitle(String eventType, String taskTitle) {
        switch (eventType.toUpperCase()) {
            case "TASK_CREATED":
                return "New Task Created";
            case "TASK_UPDATED":
                return "Task Updated";
            case "TASK_COMPLETED":
                return "Task Completed";
            case "TASK_DELETED":
                return "Task Deleted";
            case "TASK_DUE_SOON":
                return "Task Due Soon";
            case "TASK_OVERDUE":
                return "Task Overdue";
            case "USER_REGISTERED":
                return "Welcome to TaskApp";
            case "PASSWORD_RESET":
                return "Password Reset Request";
            default:
                return "TaskApp Notification";
        }
    }

    private String generateNotificationMessage(String eventType, NotificationEvent event) {
        switch (eventType.toUpperCase()) {
            case "TASK_CREATED":
                return "A new task '" + event.getTaskTitle() + "' has been created.";
            case "TASK_UPDATED":
                return "Task '" + event.getTaskTitle() + "' has been updated.";
            case "TASK_COMPLETED":
                return "Congratulations! You've completed the task '" + event.getTaskTitle() + "'.";
            case "TASK_DELETED":
                return "Task '" + event.getTaskTitle() + "' has been deleted.";
            case "TASK_DUE_SOON":
                return "Reminder: Task '" + event.getTaskTitle() + "' is due soon.";
            case "TASK_OVERDUE":
                return "Alert: Task '" + event.getTaskTitle() + "' is overdue.";
            case "USER_REGISTERED":
                return "Welcome to TaskApp! Your account has been successfully created.";
            case "PASSWORD_RESET":
                return "We received a request to reset your password. Please follow the instructions in this email.";
            default:
                return event.getMessage();
        }
    }
}