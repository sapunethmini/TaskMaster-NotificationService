package org.example.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a notification entity stored in the database.")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the notification.", example = "101")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "ID of the user who receives the notification.", example = "1")
    private Long userId;

    @Column(nullable = false)
    @Schema(description = "Email address of the recipient.", example = "user@example.com")
    private String email;

    @Column(nullable = false)
    @Schema(description = "Title of the notification.", example = "New Task Assigned")
    private String title;

    @Column(nullable = false, length = 1000)
    @Schema(description = "Detailed message content of the notification.", example = "You have been assigned a new task: 'Complete the project report'.")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "The type of notification (e.g., EMAIL, IN_APP).", example = "EMAIL")
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Delivery status of the notification.", example = "PENDING")
    private NotificationStatus status = NotificationStatus.PENDING;

    @Schema(description = "ID of the associated task, if any.", example = "55")
    private Long taskId;

    @Column(name = "created_at")
    @Schema(description = "Timestamp when the notification was created.")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sent_at")
    @Schema(description = "Timestamp when the notification was sent.")
    private LocalDateTime sentAt;

    @Column(name = "read")
    @Schema(description = "Flag indicating whether the notification has been read.", example = "false")
    private boolean read = false;

    public enum NotificationType {
        EMAIL,
        IN_APP
    }

    public enum NotificationStatus {
        PENDING,
        SENT,
        FAILED,
        NOT_APPLICABLE
    }
}