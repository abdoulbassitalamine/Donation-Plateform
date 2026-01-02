package fr.isima.glia.donoplatform.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

@Data
@EqualsAndHashCode(callSuper = true)
public class KeywordDto extends RepresentationModel<KeywordDto> {
    private Long id;
    private String name;
}