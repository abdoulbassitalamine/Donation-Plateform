package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.SavedSearchDto;
import fr.isima.glia.donoplatform.model.SavedSearch;
import fr.isima.glia.donoplatform.request.CreateSavedSearchRequest;
import fr.isima.glia.donoplatform.request.UpdateSavedSearchRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.service.savedsearch.ISavedSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/saved-searches")
@RequiredArgsConstructor
public class SavedSearchController {

    private final ISavedSearchService savedSearchService;

    @PostMapping
    public ResponseEntity<ApiResponse> createSavedSearch(
            @Valid @RequestBody CreateSavedSearchRequest request,
            @RequestParam Long userId) {
        SavedSearch savedSearch = savedSearchService.createSavedSearch(request, userId);
        SavedSearchDto dto = savedSearchService.convertToDto(savedSearch);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Saved search created successfully", dto));
    }

    @GetMapping("/{savedSearchId}")
    public ResponseEntity<ApiResponse> getSavedSearchById(@PathVariable Long savedSearchId) {
        SavedSearch savedSearch = savedSearchService.getSavedSearchById(savedSearchId);
        SavedSearchDto dto = savedSearchService.convertToDto(savedSearch);
        return ResponseEntity.ok(new ApiResponse("Saved search found", dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getSavedSearchesByUser(@PathVariable Long userId) {
        List<SavedSearchDto> savedSearches = savedSearchService.getSavedSearchesByUser(userId).stream()
                .map(savedSearchService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse("Saved searches retrieved", savedSearches));
    }

    @PutMapping("/{savedSearchId}")
    public ResponseEntity<ApiResponse> updateSavedSearch(
            @PathVariable Long savedSearchId,
            @Valid @RequestBody UpdateSavedSearchRequest request,
            @RequestParam Long userId) {
        SavedSearch updated = savedSearchService.updateSavedSearch(savedSearchId, request, userId);
        SavedSearchDto dto = savedSearchService.convertToDto(updated);
        return ResponseEntity.ok(new ApiResponse("Saved search updated successfully", dto));
    }

    @DeleteMapping("/{savedSearchId}")
    public ResponseEntity<ApiResponse> deleteSavedSearch(
            @PathVariable Long savedSearchId,
            @RequestParam Long userId) {
        savedSearchService.deleteSavedSearch(savedSearchId, userId);
        return ResponseEntity.ok(new ApiResponse("Saved search deleted successfully", null));
    }
}
