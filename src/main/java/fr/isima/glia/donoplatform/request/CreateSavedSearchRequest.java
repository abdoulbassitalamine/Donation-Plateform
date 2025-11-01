package fr.isima.glia.donoplatform.request;

import fr.isima.glia.donoplatform.enums.ItemCondition;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class CreateSavedSearchRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String geographicArea;
    private Set<String> keywords;
    private ItemCondition itemCondition;
    private Boolean notificationsEnabled = true;
}
