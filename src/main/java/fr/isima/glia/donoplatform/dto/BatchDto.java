package fr.isima.glia.donoplatform.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BatchDto extends RepresentationModel<BatchDto> {
    private Long id;
    private Long ownerId;
    private String ownerUsername;
    private List<AnnouncementDto> announcements;
    private LocalDateTime createdAt;
}