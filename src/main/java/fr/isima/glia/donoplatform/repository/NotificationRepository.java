package fr.isima.glia.donoplatform.repository;

import fr.isima.glia.donoplatform.model.Notification;
import fr.isima.glia.donoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead);
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    Long countByUserAndIsRead(User user, Boolean isRead);
}