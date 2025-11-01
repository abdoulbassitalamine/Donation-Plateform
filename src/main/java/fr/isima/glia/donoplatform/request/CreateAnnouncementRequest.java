package fr.isima.glia.donoplatform.request;

import fr.isima.glia.donoplatform.enums.ItemCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class CreateAnnouncementRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Item condition is required")
    private ItemCondition itemCondition;

    @NotBlank(message = "Geographic area is required")
    private String geographicArea;

    @NotNull(message = "Hand delivery option is required")
    private Boolean handDelivery;

    @NotNull(message = "Shipping option is required")
    private Boolean canShip;

    private Set<String> keywords;
}
