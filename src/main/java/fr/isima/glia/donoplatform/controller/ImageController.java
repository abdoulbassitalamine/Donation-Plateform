package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.ImageDto;
import fr.isima.glia.donoplatform.model.Image;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.service.image.IImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/images")
@RequiredArgsConstructor
public class ImageController {

    private final IImageService imageService;

    @PostMapping
    public ResponseEntity<ApiResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long announcementId) {
        Image image = imageService.saveImage(file, announcementId);
        ImageDto dto = imageService.convertToDto(image);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Image uploaded successfully", dto));
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ApiResponse> getImageById(@PathVariable Long imageId) {
        Image image = imageService.getImageById(imageId);
        ImageDto dto = imageService.convertToDto(image);
        return ResponseEntity.ok(new ApiResponse("Image found", dto));
    }

    @GetMapping("/{imageId}/download")
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long imageId) {
        Image image = imageService.getImageById(imageId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
                .body(image.getData());
    }

    @GetMapping("/announcement/{announcementId}")
    public ResponseEntity<ApiResponse> getImagesByAnnouncement(@PathVariable Long announcementId) {
        List<ImageDto> images = imageService.getImagesByAnnouncement(announcementId).stream()
                .map(imageService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse("Images retrieved", images));
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse> deleteImage(@PathVariable Long imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.ok(new ApiResponse("Image deleted successfully", null));
    }
}
