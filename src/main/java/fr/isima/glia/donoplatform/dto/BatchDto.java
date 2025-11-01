package fr.isima.glia.donoplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BatchDto {
    private Long id;
    private Long ownerId;
    private String ownerUsername;
    private List<AnnouncementDto> announcements;
    private LocalDateTime createdAt;
}
