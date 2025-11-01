package fr.isima.glia.donoplatform.dto;

import fr.isima.glia.donoplatform.enums.ItemCondition;
import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class AnnouncementDto {
    private Long id;
    private String title;
    private String description;
    private ItemCondition itemCondition;
    private LocalDateTime publicationDate;
    private String geographicArea;
    private Boolean handDelivery;
    private Boolean canShip;
    private AnnouncementStatus status;
    private Long ownerId;
    private String ownerUsername;
    private Set<KeywordDto> keywords;
    private Set<ImageDto> images;
}
