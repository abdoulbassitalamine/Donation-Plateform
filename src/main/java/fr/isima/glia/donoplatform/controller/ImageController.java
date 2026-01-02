package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.ImageDto;
import fr.isima.glia.donoplatform.model.Image;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.image.IImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final IImageService imageService;

    // ============================================================
    // UPLOAD IMAGE - JSON (multipart/form-data)
    // ============================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> uploadImageJson(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Image image = imageService.saveImage(file, announcementId);
        ImageDto dto = imageService.convertToDto(image);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Image uploaded successfully", dto));
    }

    // ============================================================
    // GET IMAGE BY ID - JSON (avec cache)
    // ============================================================
    @GetMapping(value = "/{imageId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getImageByIdJson(
            @PathVariable Long imageId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Image image = imageService.getImageById(imageId);
        ImageDto dto = imageService.convertToDto(image);

        // Générer ETag
        String etag = "\"" + Integer.toHexString(image.hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(new ApiResponse("Image found", dto));
    }

    // ============================================================
    // DOWNLOAD IMAGE - BINAIRE (PUBLIC - pas d'auth requise)
    // ============================================================
    @GetMapping("/{imageId}/download")
    public ResponseEntity<byte[]> downloadImage(
            @PathVariable Long imageId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Image image = imageService.getImageById(imageId);

        // Générer ETag basé sur le contenu
        String etag = "\"" + Integer.toHexString(image.hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + image.getFileName() + "\"")
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS)) // Cache 1 heure
                .body(image.getData());
    }

    // ============================================================
    // GET IMAGES BY ANNOUNCEMENT - JSON (avec cache)
    // ============================================================
    @GetMapping(value = "/announcement/{announcementId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getImagesByAnnouncementJson(
            @PathVariable Long announcementId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<ImageDto> images = imageService.getImagesByAnnouncement(announcementId)
                .stream()
                .map(imageService::convertToDto)
                .collect(Collectors.toList());

        // Générer ETag
        String etag = "\"" + Integer.toHexString(images.hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(new ApiResponse("Images retrieved", images));
    }

    // ============================================================
    // DELETE IMAGE - JSON (auth requise)
    // ============================================================
    @DeleteMapping(value = "/{imageId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> deleteImageJson(
            @PathVariable Long imageId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        imageService.deleteImage(imageId);
        return ResponseEntity.ok(new ApiResponse("Image deleted successfully", null));
    }
}