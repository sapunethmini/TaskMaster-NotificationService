package org.example.service;

import org.example.model.Notification;
import org.example.model.NotificationRequest;
import org.example.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationService(
            NotificationRepository notificationRepository,
            EmailService emailService,
            SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
        this.messagingTemplate = messagingTemplate;
    }

    public Notification createNotification(NotificationRequest request) {
        try {
            Notification notification = new Notification();
            notification.setUserId(request.getUserId());
            notification.setEmail(request.getEmail());
            notification.setTitle(request.getTitle());
            notification.setMessage(request.getMessage());
            notification.setTaskId(request.getTaskId());

            // Set notification type
            if ("EMAIL".equalsIgnoreCase(request.getType())) {
                notification.setType(Notification.NotificationType.EMAIL);
            } else if ("IN_APP".equalsIgnoreCase(request.getType())) {
                notification.setType(Notification.NotificationType.IN_APP);
            } else {
                notification.setType(Notification.NotificationType.EMAIL); // default
            }

            // Save notification
            notification = notificationRepository.save(notification);
            logger.info("Notification created with ID: {}", notification.getId());

            // Process the notification
            processNotification(notification);

            return notification;

        } catch (Exception e) {
            logger.error("Failed to create notification: {}", e.getMessage());
            throw new RuntimeException("Failed to create notification", e);
        }
    }

    public void processNotification(Notification notification) {
        try {
            boolean success = false;

            switch (notification.getType()) {
                case EMAIL:
                    success = emailService.sendSimpleEmail(
                            notification.getEmail(),
                            notification.getTitle(),
                            notification.getMessage()
                    );
                    break;

                case IN_APP:
                    success = sendInAppNotification(notification);
                    break;

                default:
                    logger.warn("Unknown notification type: {}", notification.getType());
            }

            // Update notification status
            if (success) {
                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                logger.info("Notification {} sent successfully", notification.getId());
            } else {
                notification.setStatus(Notification.NotificationStatus.FAILED);
                logger.error("Failed to send notification {}", notification.getId());
            }

            notificationRepository.save(notification);

        } catch (Exception e) {
            logger.error("Error processing notification {}: {}", notification.getId(), e.getMessage());
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notificationRepository.save(notification);
        }
    }

    private boolean sendInAppNotification(Notification notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    notification.getUserId().toString(),
                    "/queue/notifications",
                    notification
            );
            logger.info("In-app notification sent to user: {}", notification.getUserId());
            return true;
        } catch (Exception e) {
            logger.error("Failed to send in-app notification: {}", e.getMessage());
            return false;
        }
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getPendingNotifications(Long userId) {
        return notificationRepository.findPendingNotificationsByUserId(userId);
    }

    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, Notification.NotificationStatus.PENDING);
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setStatus(Notification.NotificationStatus.SENT);
        return notificationRepository.save(notification);
    }
}