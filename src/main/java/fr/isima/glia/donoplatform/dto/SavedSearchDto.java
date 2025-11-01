package fr.isima.glia.donoplatform.dto;

import fr.isima.glia.donoplatform.enums.ItemCondition;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class SavedSearchDto {
    private Long id;
    private Long userId;
    private String name;
    private String geographicArea;
    private Set<KeywordDto> keywords;
    private ItemCondition itemCondition;
    private Boolean notificationsEnabled;
    private LocalDateTime createdAt;
}
