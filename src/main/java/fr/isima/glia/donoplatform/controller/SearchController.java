package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.dto.SavedSearchDto;
import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import fr.isima.glia.donoplatform.enums.ItemCondition;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.SavedSearch;
import fr.isima.glia.donoplatform.request.CreateSavedSearchRequest;
import fr.isima.glia.donoplatform.request.SearchAnnouncementRequest;
import fr.isima.glia.donoplatform.request.UpdateSavedSearchRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import fr.isima.glia.donoplatform.service.keyword.IKeywordService;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final IAnnouncementService announcementService;
    private final IKeywordService keywordService;
    private final ISavedSearchService savedSearchService;

    // ============================================================
    // SEARCH FORM - HTML
    // ============================================================
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String showSearchFormHtml(Model model) {
        // ✅ CORRECTION : Ajouter l'objet vide pour th:object
        model.addAttribute("searchRequest", new SearchAnnouncementRequest());

        model.addAttribute("itemConditions", ItemCondition.values());
        model.addAttribute("statuses", AnnouncementStatus.values());
        model.addAttribute("keywords", keywordService.getAllKeywords());

        return "search/form";
    }

    // ============================================================
    // SEARCH ANNOUNCEMENTS - JSON (API avec cache)
    // ============================================================
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> searchAnnouncementsJson(
            @RequestBody SearchAnnouncementRequest request,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        Page<Announcement> announcementPage = announcementService.searchAnnouncements(request, pageable);
        Page<AnnouncementDto> dtoPage = announcementPage.map(announcementService::convertToDto);

        // Générer ETag
        String etag = "\"" + Integer.toHexString(dtoPage.getContent().hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(3, TimeUnit.MINUTES))
                .body(new ApiResponse("Search results", dtoPage));
    }

    // ============================================================
    // SEARCH RESULTS - HTML (GET params)
    // ============================================================
    @GetMapping(value = "/results", produces = MediaType.TEXT_HTML_VALUE)
    public String searchResultsHtmlGet(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String geographicArea,
            @RequestParam(required = false) ItemCondition itemCondition,
            @RequestParam(required = false) AnnouncementStatus status,
            @RequestParam(required = false) Boolean handDelivery,
            @RequestParam(required = false) Boolean canShip,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        SearchAnnouncementRequest request = new SearchAnnouncementRequest();
        request.setSearchText(searchText);
        request.setGeographicArea(geographicArea);
        request.setItemCondition(itemCondition);
        request.setStatus(status);
        request.setHandDelivery(handDelivery);
        request.setCanShip(canShip);

        Pageable pageable = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        Page<Announcement> announcementPage = announcementService.searchAnnouncements(request, pageable);
        Page<AnnouncementDto> dtoPage = announcementPage.map(announcementService::convertToDto);

        model.addAttribute("announcements", dtoPage);
        model.addAttribute("searchRequest", request);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", dtoPage.getTotalPages());

        return "search/results";
    }

    // ============================================================
    // SEARCH RESULTS - HTML (POST form)
    // ============================================================
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String searchResultsHtmlPost(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String geographicArea,
            @RequestParam(required = false) ItemCondition itemCondition,
            @RequestParam(required = false) AnnouncementStatus status,
            @RequestParam(required = false) Boolean handDelivery,
            @RequestParam(required = false) Boolean canShip,
            Model model) {

        return searchResultsHtmlGet(searchText, geographicArea, itemCondition,
                status, handDelivery, canShip, 0, 12, model);
    }

    // ============================================================
    // SAVED SEARCHES - CREATE - JSON
    // ============================================================
    @PostMapping(value = "/saved",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> createSavedSearchJson(
            @Valid @RequestBody CreateSavedSearchRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        SavedSearch savedSearch = savedSearchService.createSavedSearch(request, userDetails.getId());
        SavedSearchDto dto = savedSearchService.convertToDto(savedSearch);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Saved search created successfully", dto));
    }

    // ============================================================
    // SAVED SEARCHES - GET BY ID - JSON
    // ============================================================
    @GetMapping(value = "/saved/{savedSearchId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getSavedSearchByIdJson(@PathVariable Long savedSearchId) {
        SavedSearch savedSearch = savedSearchService.getSavedSearchById(savedSearchId);
        SavedSearchDto dto = savedSearchService.convertToDto(savedSearch);
        return ResponseEntity.ok(new ApiResponse("Saved search found", dto));
    }

    // ============================================================
    // SAVED SEARCHES - GET BY USER - JSON (avec cache)
    // ============================================================
    @GetMapping(value = "/saved/user/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getSavedSearchesByUserJson(
            @PathVariable Long userId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<SavedSearch> savedSearches = savedSearchService.getSavedSearchesByUser(userId);
        List<SavedSearchDto> dtos = savedSearches.stream()
                .map(savedSearchService::convertToDto)
                .toList();

        // Générer ETag
        String etag = "\"" + Integer.toHexString(dtos.hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(new ApiResponse("Saved searches retrieved", dtos));
    }

    // ============================================================
    // SAVED SEARCHES - UPDATE - JSON
    // ============================================================
    @PutMapping(value = "/saved/{savedSearchId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> updateSavedSearchJson(
            @PathVariable Long savedSearchId,
            @Valid @RequestBody UpdateSavedSearchRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        SavedSearch updated = savedSearchService.updateSavedSearch(savedSearchId, request, userDetails.getId());
        SavedSearchDto dto = savedSearchService.convertToDto(updated);
        return ResponseEntity.ok(new ApiResponse("Saved search updated successfully", dto));
    }

    // ============================================================
    // SAVED SEARCHES - DELETE - JSON
    // ============================================================
    @DeleteMapping(value = "/saved/{savedSearchId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> deleteSavedSearchJson(
            @PathVariable Long savedSearchId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        savedSearchService.deleteSavedSearch(savedSearchId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Saved search deleted successfully", null));
    }
}