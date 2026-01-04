package fr.isima.glia.donoplatform.controller.api;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.request.CreateAnnouncementRequest;
import fr.isima.glia.donoplatform.request.UpdateAnnouncementRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * JSON API for announcement operations
 */
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementApiController {

    private final IAnnouncementService announcementService;

    // List announcements
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> listAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Announcement> announcementPage = announcementService.getAllAnnouncements(pageable);
        Page<AnnouncementDto> dtoPage = announcementPage.map(announcementService::convertToDto);

        String etag = "\"" + Integer.toHexString(dtoPage.getContent().hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(new ApiResponse("Announcements retrieved", dtoPage));
    }

    // Get announcement by ID
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getAnnouncementById(
            @PathVariable Long id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Announcement announcement = announcementService.getAnnouncementById(id);
        AnnouncementDto dto = announcementService.convertToDto(announcement);

        String etag = "\"" + Integer.toHexString(announcement.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic())
                .body(new ApiResponse("Announcement found", dto));
    }

    // Create announcement
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Announcement announcement = announcementService.createAnnouncement(request, userDetails.getId());
        AnnouncementDto dto = announcementService.convertToDto(announcement);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Announcement created successfully", dto));
    }

    // Update announcement
    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAnnouncementRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Announcement updated = announcementService.updateAnnouncement(id, request, userDetails.getId());
        AnnouncementDto dto = announcementService.convertToDto(updated);

        return ResponseEntity.ok(new ApiResponse("Announcement updated successfully", dto));
    }

    // Update announcement status
    @PatchMapping(value = "/{id}/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam AnnouncementStatus status,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Announcement updated = announcementService.updateAnnouncementStatus(id, status, userDetails.getId());
        AnnouncementDto dto = announcementService.convertToDto(updated);

        return ResponseEntity.ok(new ApiResponse("Status updated successfully", dto));
    }

    // Delete announcement
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> deleteAnnouncement(
            @PathVariable Long id,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        announcementService.deleteAnnouncement(id, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Announcement deleted successfully", null));
    }

    // Get user announcements
    @GetMapping(value = "/user/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getUserAnnouncements(@PathVariable Long userId) {
        var announcements = announcementService.getAnnouncementsByUser(userId).stream()
                .map(announcementService::convertToDto)
                .toList();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(2, TimeUnit.MINUTES))
                .body(new ApiResponse("User announcements retrieved", announcements));
    }
}