package fr.isima.glia.donoplatform.service.image;

import fr.isima.glia.donoplatform.dto.ImageDto;
import fr.isima.glia.donoplatform.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {
    Image saveImage(MultipartFile file, Long announcementId);
    Image getImageById(Long id);
    ImageDto convertToDto(Image image);
    void deleteImage(Long id);
    List<Image> getImagesByAnnouncement(Long announcementId);
}
