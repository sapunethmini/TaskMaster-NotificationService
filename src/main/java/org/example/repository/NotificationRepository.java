package org.example.repository;

import org.example.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Notification.NotificationStatus status);
    
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.status = 'PENDING'")
    List<Notification> findPendingNotificationsByUserId(@Param("userId") Long userId);
    
    long countByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
}