package org.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents an event that could trigger a notification.")
public class NotificationEvent {

    @Schema(description = "Type of the event.", example = "TASK_ASSIGNED")
    private String eventType;

    @Schema(description = "ID of the user related to the event.", example = "1")
    private Long userId;

    @Schema(description = "Email of the user.", example = "user@example.com")
    private String userEmail;

    @Schema(description = "Title for the notification.", example = "Task Assignment")
    private String title;

    @Schema(description = "Message for the notification.", example = "You have a new task.")
    private String message;

    @Schema(description = "ID of the associated task.", example = "55")
    private Long taskId;

    @Schema(description = "Title of the associated task.", example = "Deploy to production")
    private String taskTitle;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Timestamp of the event.", example = "2024-06-15 10:30:00")
    private LocalDateTime timestamp = LocalDateTime.now();
}