package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.request.CreateAnnouncementRequest;
import fr.isima.glia.donoplatform.request.SearchAnnouncementRequest;
import fr.isima.glia.donoplatform.request.UpdateAnnouncementRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final IAnnouncementService announcementService;

    @PostMapping
    public ResponseEntity<ApiResponse> createAnnouncement(
            @Valid @RequestBody CreateAnnouncementRequest request,
            @RequestParam Long userId) {
        Announcement announcement = announcementService.createAnnouncement(request, userId);
        AnnouncementDto dto = announcementService.convertToDto(announcement);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Announcement created successfully", dto));
    }

    @GetMapping("/{announcementId}")
    public ResponseEntity<ApiResponse> getAnnouncementById(@PathVariable Long announcementId) {
        Announcement announcement = announcementService.getAnnouncementById(announcementId);
        AnnouncementDto dto = announcementService.convertToDto(announcement);
        return ResponseEntity.ok(new ApiResponse("Announcement found", dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Announcement> announcementPage = announcementService.getAllAnnouncements(pageable);

        Page<AnnouncementDto> dtoPage = announcementPage.map(announcementService::convertToDto);

        return ResponseEntity.ok(new ApiResponse("Announcements retrieved", dtoPage));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getAnnouncementsByUser(@PathVariable Long userId) {
        List<AnnouncementDto> announcements = announcementService.getAnnouncementsByUser(userId).stream()
                .map(announcementService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse("User announcements retrieved", announcements));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse> searchAnnouncements(
            @RequestBody SearchAnnouncementRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        Page<Announcement> announcementPage = announcementService.searchAnnouncements(request, pageable);

        Page<AnnouncementDto> dtoPage = announcementPage.map(announcementService::convertToDto);

        return ResponseEntity.ok(new ApiResponse("Search results", dtoPage));
    }

    @PutMapping("/{announcementId}")
    public ResponseEntity<ApiResponse> updateAnnouncement(
            @PathVariable Long announcementId,
            @Valid @RequestBody UpdateAnnouncementRequest request,
            @RequestParam Long userId) {
        Announcement updated = announcementService.updateAnnouncement(announcementId, request, userId);
        AnnouncementDto dto = announcementService.convertToDto(updated);
        return ResponseEntity.ok(new ApiResponse("Announcement updated successfully", dto));
    }

    @PatchMapping("/{announcementId}/status")
    public ResponseEntity<ApiResponse> updateAnnouncementStatus(
            @PathVariable Long announcementId,
            @RequestParam AnnouncementStatus status,
            @RequestParam Long userId) {
        Announcement updated = announcementService.updateAnnouncementStatus(announcementId, status, userId);
        AnnouncementDto dto = announcementService.convertToDto(updated);
        return ResponseEntity.ok(new ApiResponse("Status updated successfully", dto));
    }

    @DeleteMapping("/{announcementId}")
    public ResponseEntity<ApiResponse> deleteAnnouncement(
            @PathVariable Long announcementId,
            @RequestParam Long userId) {
        announcementService.deleteAnnouncement(announcementId, userId);
        return ResponseEntity.ok(new ApiResponse("Announcement deleted successfully", null));
    }
}
