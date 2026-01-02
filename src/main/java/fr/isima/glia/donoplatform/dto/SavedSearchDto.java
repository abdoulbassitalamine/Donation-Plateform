package fr.isima.glia.donoplatform.dto;

import fr.isima.glia.donoplatform.enums.ItemCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class SavedSearchDto extends RepresentationModel<SavedSearchDto> {
    private Long id;
    private Long userId;
    private String name;
    private String geographicArea;
    private Set<KeywordDto> keywords;
    private ItemCondition itemCondition;
    private Boolean notificationsEnabled;
    private LocalDateTime createdAt;
}