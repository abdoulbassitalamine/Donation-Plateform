package fr.isima.glia.donoplatform.dto;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
public class NotificationDto extends RepresentationModel<NotificationDto> {
    private Long id;
    private Long userId;
    private String username;
    private Long announcementId;
    private String announcementTitle;
    private Long savedSearchId;
    private String savedSearchName;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}