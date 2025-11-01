package fr.isima.glia.donoplatform.request;

import fr.isima.glia.donoplatform.enums.ItemCondition;
import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateAnnouncementRequest {
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private ItemCondition itemCondition;
    private String geographicArea;
    private Boolean handDelivery;
    private Boolean canShip;
    private AnnouncementStatus status;
    private Set<String> keywords;
}
