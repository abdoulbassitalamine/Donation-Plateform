package fr.isima.glia.donoplatform.service.image;

import fr.isima.glia.donoplatform.controller.api.AnnouncementApiController;
import fr.isima.glia.donoplatform.controller.api.ImageApiController;
import fr.isima.glia.donoplatform.dto.ImageDto;
import fr.isima.glia.donoplatform.exceptions.ResourceNotFoundException;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.Image;
import fr.isima.glia.donoplatform.repository.ImageRepository;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService implements IImageService {

    private final ImageRepository imageRepository;
    private final IAnnouncementService announcementService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Image saveImage(MultipartFile file, Long announcementId) {
        Announcement announcement = announcementService.getAnnouncementById(announcementId);

        try {
            Image image = Image.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .data(file.getBytes())
                    .announcement(announcement)
                    .build();

            return imageRepository.save(image);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store image: " + e.getMessage());
        }
    }

    @Override
    public Image getImageById(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + id));
    }

    @Override
    public ImageDto convertToDto(Image image) {
        ImageDto dto = modelMapper.map(image, ImageDto.class);

        // ============================================================
        // HATEOAS - Ajout des liens hypermedia
        // ============================================================
        try {
            // Self link
            dto.add(linkTo(methodOn(ImageApiController.class)
                    .getImageById(image.getId(), null))
                    .withSelfRel());

            // Download link (public)
            dto.add(linkTo(methodOn(ImageApiController.class)
                    .getImageById(image.getId(), null))
                    .withRel("download"));

            // Announcement link
            dto.add(linkTo(methodOn(AnnouncementApiController.class)
                    .getAnnouncementById(image.getAnnouncement().getId(), null))
                    .withRel("announcement"));

            // Delete link
            dto.add(linkTo(methodOn(ImageApiController.class)
                    .deleteImage(image.getId(), null))
                    .withRel("delete"));

        } catch (Exception e) {
            // Si erreur de génération de lien, on continue sans bloquer
        }

        // URL de téléchargement (legacy - pour compatibilité)
        dto.setDownloadUrl("/images/" + image.getId() + "/download");

        return dto;
    }

    @Override
    @Transactional
    public void deleteImage(Long id) {
        Image image = getImageById(id);
        imageRepository.delete(image);
    }

    @Override
    public List<Image> getImagesByAnnouncement(Long announcementId) {
        Announcement announcement = announcementService.getAnnouncementById(announcementId);
        return announcement.getImages().stream().toList();
    }
}
