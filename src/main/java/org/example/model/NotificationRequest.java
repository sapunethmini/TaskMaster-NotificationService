package org.example.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request model for creating a new notification.")
public class NotificationRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "The unique identifier of the user to be notified.", required = true, example = "1")
    private Long userId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(description = "The email address of the recipient.", required = true, example = "user@example.com")
    private String email;

    @NotBlank(message = "Title is required")
    @Schema(description = "The title of the notification.", required = true, example = "New Task Alert")
    private String title;

    @NotBlank(message = "Message is required")
    @Schema(description = "The main content of the notification message.", required = true, example = "A new task has been assigned to you.")
    private String message;

    @Schema(description = "Type of notification. Defaults to 'EMAIL'.", example = "EMAIL")
    private String type = "EMAIL";

    @Schema(description = "Optional ID of a task associated with this notification.", example = "55")
    private Long taskId;

    // Constructor for Kafka events
    public NotificationRequest(Long userId, String email, String title, String message) {
        this.userId = userId;
        this.email = email;
        this.title = title;
        this.message = message;
        this.type = "EMAIL"; // Default type
    }
}