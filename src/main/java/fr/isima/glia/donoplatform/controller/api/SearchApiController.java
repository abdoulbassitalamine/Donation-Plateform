package fr.isima.glia.donoplatform.controller.api;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.dto.SavedSearchDto;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.SavedSearch;
import fr.isima.glia.donoplatform.request.CreateSavedSearchRequest;
import fr.isima.glia.donoplatform.request.SearchAnnouncementRequest;
import fr.isima.glia.donoplatform.request.UpdateSavedSearchRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import fr.isima.glia.donoplatform.service.savedsearch.ISavedSearchService;
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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JSON API for search operations
 */
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchApiController {

    private final IAnnouncementService announcementService;
    private final ISavedSearchService savedSearchService;

    // Search announcements - JSON
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> searchAnnouncements(
            @RequestBody SearchAnnouncementRequest request,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        Page<Announcement> announcementPage = announcementService.searchAnnouncements(request, pageable);
        Page<AnnouncementDto> dtoPage = announcementPage.map(announcementService::convertToDto);

        String etag = "\"" + Integer.toHexString(dtoPage.getContent().hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(3, TimeUnit.MINUTES))
                .body(new ApiResponse("Search results", dtoPage));
    }

    // Create saved search
    @PostMapping(value = "/saved",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> createSavedSearch(
            @Valid @RequestBody CreateSavedSearchRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        SavedSearch savedSearch = savedSearchService.createSavedSearch(request, userDetails.getId());
        SavedSearchDto dto = savedSearchService.convertToDto(savedSearch);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Saved search created successfully", dto));
    }

    // Get saved search by ID
    @GetMapping(value = "/saved/{savedSearchId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getSavedSearchById(@PathVariable Long savedSearchId) {
        SavedSearch savedSearch = savedSearchService.getSavedSearchById(savedSearchId);
        SavedSearchDto dto = savedSearchService.convertToDto(savedSearch);
        return ResponseEntity.ok(new ApiResponse("Saved search found", dto));
    }

    // Get saved searches by user
    @GetMapping(value = "/saved/user/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getSavedSearchesByUser(
            @PathVariable Long userId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<SavedSearch> savedSearches = savedSearchService.getSavedSearchesByUser(userId);
        List<SavedSearchDto> dtos = savedSearches.stream()
                .map(savedSearchService::convertToDto)
                .toList();

        String etag = "\"" + Integer.toHexString(dtos.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(new ApiResponse("Saved searches retrieved", dtos));
    }

    // Update saved search
    @PutMapping(value = "/saved/{savedSearchId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updateSavedSearch(
            @PathVariable Long savedSearchId,
            @Valid @RequestBody UpdateSavedSearchRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        SavedSearch updated = savedSearchService.updateSavedSearch(savedSearchId, request, userDetails.getId());
        SavedSearchDto dto = savedSearchService.convertToDto(updated);
        return ResponseEntity.ok(new ApiResponse("Saved search updated successfully", dto));
    }

    // Delete saved search
    @DeleteMapping(value = "/saved/{savedSearchId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> deleteSavedSearch(
            @PathVariable Long savedSearchId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        savedSearchService.deleteSavedSearch(savedSearchId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Saved search deleted successfully", null));
    }
}