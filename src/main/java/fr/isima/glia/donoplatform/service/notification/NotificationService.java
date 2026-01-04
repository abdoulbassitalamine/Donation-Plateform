package fr.isima.glia.donoplatform.service.notification;

import fr.isima.glia.donoplatform.dto.NotificationDto;
import fr.isima.glia.donoplatform.exceptions.ResourceNotFoundException;
import fr.isima.glia.donoplatform.exceptions.UnauthorizedAccessException;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.Notification;
import fr.isima.glia.donoplatform.model.SavedSearch;
import fr.isima.glia.donoplatform.model.User;
import fr.isima.glia.donoplatform.repository.NotificationRepository;
import fr.isima.glia.donoplatform.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final IUserService userService;
    private final ModelMapper modelMapper;

    @Transactional
    public Notification createNotification(User user, Announcement announcement, SavedSearch savedSearch) {
        String message = String.format("Nouvelle annonce '%s' correspond à votre recherche '%s'",
                announcement.getTitle(),
                savedSearch.getName());

        Notification notification = Notification.builder()
                .user(user)
                .announcement(announcement)
                .savedSearch(savedSearch)
                .message(message)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("✅ Notification créée pour user {} - annonce {}", user.getId(), announcement.getId());

        return saved;
    }

    public NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = modelMapper.map(notification, NotificationDto.class);
        dto.setUserId(notification.getUser().getId());
        dto.setUsername(notification.getUser().getUsername());
        dto.setAnnouncementId(notification.getAnnouncement().getId());
        dto.setAnnouncementTitle(notification.getAnnouncement().getTitle());

        if (notification.getSavedSearch() != null) {
            dto.setSavedSearchId(notification.getSavedSearch().getId());
            dto.setSavedSearchName(notification.getSavedSearch().getName());
        }

        return dto;
    }

    public List<NotificationDto> getUserNotifications(Long userId, Boolean unreadOnly) {
        User user = userService.getUserById(userId);

        List<Notification> notifications = unreadOnly != null && unreadOnly
                ? notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false)
                : notificationRepository.findByUserOrderByCreatedAtDesc(user);

        return notifications.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Unauthorized");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userService.getUserById(userId);
        List<Notification> unread = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);

        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    public Long getUnreadCount(Long userId) {
        User user = userService.getUserById(userId);
        return notificationRepository.countByUserAndIsRead(user, false);
    }
}