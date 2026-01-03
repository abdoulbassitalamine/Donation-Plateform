package fr.isima.glia.donoplatform.controller.api;

import fr.isima.glia.donoplatform.dto.FavoriteDto;
import fr.isima.glia.donoplatform.model.Favorite;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.favorite.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JSON API for favorite operations
 */
@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteApiController {

    private final IFavoriteService favoriteService;

    // Add favorite
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> addFavorite(
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Favorite favorite = favoriteService.addFavorite(announcementId, userDetails.getId());
        FavoriteDto dto = favoriteService.convertToDto(favorite);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Added to favorites", dto));
    }

    // Remove favorite
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> removeFavorite(
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        favoriteService.removeFavorite(announcementId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Removed from favorites", null));
    }

    // Get user favorites
    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getUserFavorites(
            @PathVariable Long userId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<Favorite> favorites = favoriteService.getFavoritesByUser(userId);
        List<FavoriteDto> dtos = favorites.stream()
                .map(favoriteService::convertToDto)
                .toList();

        String etag = "\"" + Integer.toHexString(dtos.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(2, TimeUnit.MINUTES))
                .body(new ApiResponse("Favorites retrieved", dtos));
    }

    // Check favorite status
    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> isFavorite(
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        boolean isFavorite = favoriteService.isFavorite(announcementId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Favorite status", isFavorite));
    }
}