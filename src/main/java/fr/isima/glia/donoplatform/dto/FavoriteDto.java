package fr.isima.glia.donoplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavoriteDto {
    private Long id;
    private Long userId;
    private AnnouncementDto announcement;
    private LocalDateTime createdAt;
}
