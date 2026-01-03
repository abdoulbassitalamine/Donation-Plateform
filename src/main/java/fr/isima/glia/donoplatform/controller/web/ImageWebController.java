package fr.isima.glia.donoplatform.controller.web;

import fr.isima.glia.donoplatform.model.Image;
import fr.isima.glia.donoplatform.service.image.IImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * Image download endpoint (public, no auth required)
 */
@Controller
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageWebController {

    private final IImageService imageService;

    // Download image binary
    @GetMapping("/{imageId}/download")
    public ResponseEntity<byte[]> downloadImage(
            @PathVariable Long imageId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Image image = imageService.getImageById(imageId);

        String etag = "\"" + Integer.toHexString(image.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + image.getFileName() + "\"")
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
                .body(image.getData());
    }
}