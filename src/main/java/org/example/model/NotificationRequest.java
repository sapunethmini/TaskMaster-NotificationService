package org.example.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private String type = "EMAIL";
    
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