package fr.isima.glia.donoplatform.controller.api;

import fr.isima.glia.donoplatform.dto.ImageDto;
import fr.isima.glia.donoplatform.model.Image;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.image.IImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JSON API for image operations
 */
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageApiController {

    private final IImageService imageService;

    // Upload image
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Image image = imageService.saveImage(file, announcementId);
        ImageDto dto = imageService.convertToDto(image);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Image uploaded successfully", dto));
    }

    // Get image metadata by ID
    @GetMapping(value = "/{imageId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getImageById(
            @PathVariable Long imageId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Image image = imageService.getImageById(imageId);
        ImageDto dto = imageService.convertToDto(image);

        String etag = "\"" + Integer.toHexString(image.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(new ApiResponse("Image found", dto));
    }

    // Get images by announcement
    @GetMapping(value = "/announcement/{announcementId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getImagesByAnnouncement(
            @PathVariable Long announcementId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<ImageDto> images = imageService.getImagesByAnnouncement(announcementId)
                .stream()
                .map(imageService::convertToDto)
                .collect(Collectors.toList());

        String etag = "\"" + Integer.toHexString(images.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(new ApiResponse("Images retrieved", images));
    }

    // Delete image
    @DeleteMapping(value = "/{imageId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> deleteImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        imageService.deleteImage(imageId);
        return ResponseEntity.ok(new ApiResponse("Image deleted successfully", null));
    }
}