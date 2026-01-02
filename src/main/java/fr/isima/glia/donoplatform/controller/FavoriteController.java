package fr.isima.glia.donoplatform.controller;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final IFavoriteService favoriteService;

    // ============================================================
    // ADD FAVORITE - JSON (API)
    // ============================================================
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> addFavoriteJson(
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Favorite favorite = favoriteService.addFavorite(announcementId, userDetails.getId());
        FavoriteDto dto = favoriteService.convertToDto(favorite);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Added to favorites", dto));
    }

    // ============================================================
    // REMOVE FAVORITE - JSON (API)
    // ============================================================
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> removeFavoriteJson(
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        favoriteService.removeFavorite(announcementId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Removed from favorites", null));
    }

    // ============================================================
    // GET USER FAVORITES - JSON (API avec cache)
    // ============================================================
    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getUserFavoritesJson(
            @PathVariable Long userId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<Favorite> favorites = favoriteService.getFavoritesByUser(userId);
        List<FavoriteDto> dtos = favorites.stream()
                .map(favoriteService::convertToDto)
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
                .cacheControl(CacheControl.maxAge(2, TimeUnit.MINUTES))
                .body(new ApiResponse("Favorites retrieved", dtos));
    }

    // ============================================================
    // GET USER FAVORITES - HTML (Web)
    // ============================================================
    @GetMapping(value = "/{userId}", produces = MediaType.TEXT_HTML_VALUE)
    public String listFavoritesHtml(@PathVariable Long userId, Model model) {
        List<FavoriteDto> favorites = favoriteService.getFavoritesByUser(userId)
                .stream()
                .map(favoriteService::convertToDto)
                .toList();

        model.addAttribute("favorites", favorites);
        model.addAttribute("userId", userId);

        return "favorites/list";
    }

    // ============================================================
    // CHECK FAVORITE - JSON (API)
    // ============================================================
    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> isFavoriteJson(
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        boolean isFavorite = favoriteService.isFavorite(announcementId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Favorite status", isFavorite));
    }

    // ============================================================
    // TOGGLE FAVORITE - HTML (Web via POST)
    // ============================================================
    @PostMapping(value = "/toggle",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String toggleFavoriteHtml(
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        boolean isFavorite = favoriteService.isFavorite(announcementId, userDetails.getId());

        if (isFavorite) {
            favoriteService.removeFavorite(announcementId, userDetails.getId());
        } else {
            favoriteService.addFavorite(announcementId, userDetails.getId());
        }

        // Rediriger vers la page de l'annonce
        return "redirect:/announcements/" + announcementId;
    }
}