package fr.isima.glia.donoplatform.request;

import fr.isima.glia.donoplatform.enums.ItemCondition;
import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import lombok.Data;

import java.util.Set;

@Data
public class SearchAnnouncementRequest {
    private String geographicArea;
    private Set<String> keywords;
    private ItemCondition itemCondition;
    private AnnouncementStatus status;
    private Boolean handDelivery;
    private Boolean canShip;
    private String searchText;
}
