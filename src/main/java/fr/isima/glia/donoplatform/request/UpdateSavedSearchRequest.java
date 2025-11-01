package fr.isima.glia.donoplatform.request;

import fr.isima.glia.donoplatform.enums.ItemCondition;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateSavedSearchRequest {
    private String name;
    private String geographicArea;
    private Set<String> keywords;
    private ItemCondition itemCondition;
    private Boolean notificationsEnabled;
}
