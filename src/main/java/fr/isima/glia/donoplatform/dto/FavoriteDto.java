package fr.isima.glia.donoplatform.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class FavoriteDto extends RepresentationModel<FavoriteDto> {
    private Long id;
    private Long userId;
    private AnnouncementDto announcement;
    private LocalDateTime createdAt;
}