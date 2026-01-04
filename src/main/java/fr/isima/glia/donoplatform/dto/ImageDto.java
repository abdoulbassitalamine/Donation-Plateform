package fr.isima.glia.donoplatform.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImageDto extends RepresentationModel<ImageDto> {
    private Long id;
    private String fileName;
    private String fileType;
    private String downloadUrl;
}