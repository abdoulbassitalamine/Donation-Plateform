package fr.isima.glia.donoplatform.controller.web;

import fr.isima.glia.donoplatform.dto.FavoriteDto;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.favorite.IFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTML views for favorite operations
 */
@Controller
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteWebController {

    private final IFavoriteService favoriteService;

    // List user favorites
    @GetMapping(value = "/{userId}", produces = MediaType.TEXT_HTML_VALUE)
    public String listFavorites(@PathVariable Long userId, Model model) {
        List<FavoriteDto> favorites = favoriteService.getFavoritesByUser(userId)
                .stream()
                .map(favoriteService::convertToDto)
                .toList();

        model.addAttribute("favorites", favorites);
        model.addAttribute("userId", userId);

        return "favorites/list";
    }

    // Add favorite
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String addFavorite(
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        favoriteService.addFavorite(announcementId, userDetails.getId());
        return "redirect:/announcements/" + announcementId;
    }

    // Remove favorite
    @DeleteMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String removeFavorite(
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        favoriteService.removeFavorite(announcementId, userDetails.getId());
        return "redirect:/announcements/" + announcementId;
    }
}