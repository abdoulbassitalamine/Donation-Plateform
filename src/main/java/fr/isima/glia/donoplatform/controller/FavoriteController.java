package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.FavoriteDto;
import fr.isima.glia.donoplatform.model.Favorite;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.service.favorite.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @PostMapping
    public ResponseEntity<ApiResponse> addFavorite(
            @RequestParam Long announcementId,
            @RequestParam Long userId) {
        Favorite favorite = favoriteService.addFavorite(announcementId, userId);
        FavoriteDto dto = favoriteService.convertToDto(favorite);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Added to favorites", dto));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> removeFavorite(
            @RequestParam Long announcementId,
            @RequestParam Long userId) {
        favoriteService.removeFavorite(announcementId, userId);
        return ResponseEntity.ok(new ApiResponse("Removed from favorites", null));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getFavoritesByUser(@PathVariable Long userId) {
        List<FavoriteDto> favorites = favoriteService.getFavoritesByUser(userId).stream()
                .map(favoriteService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse("Favorites retrieved", favorites));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse> isFavorite(
            @RequestParam Long announcementId,
            @RequestParam Long userId) {
        boolean isFavorite = favoriteService.isFavorite(announcementId, userId);
        return ResponseEntity.ok(new ApiResponse("Favorite status", isFavorite));
    }
}
