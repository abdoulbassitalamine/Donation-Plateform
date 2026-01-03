package fr.isima.glia.donoplatform.controller.web;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.dto.FavoriteDto;
import fr.isima.glia.donoplatform.dto.SavedSearchDto;
import fr.isima.glia.donoplatform.dto.UserDto;
import fr.isima.glia.donoplatform.request.UserUpdateRequest;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import fr.isima.glia.donoplatform.service.favorite.IFavoriteService;
import fr.isima.glia.donoplatform.service.savedsearch.ISavedSearchService;
import fr.isima.glia.donoplatform.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HTML views for user operations
 */
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserWebController {

    private final IUserService userService;
    private final IAnnouncementService announcementService;
    private final IFavoriteService favoriteService;
    private final ISavedSearchService savedSearchService;

    // View user profile
    @GetMapping(value = "/{userId}",
            produces = MediaType.TEXT_HTML_VALUE)
    public String viewProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        UserDto user = userService.convertToDto(userService.getUserById(userId));

        List<AnnouncementDto> announcements = announcementService.getAnnouncementsByUser(userId)
                .stream()
                .map(announcementService::convertToDto)
                .collect(Collectors.toList());

        List<FavoriteDto> favorites = favoriteService.getFavoritesByUser(userId)
                .stream()
                .map(favoriteService::convertToDto)
                .collect(Collectors.toList());

        List<SavedSearchDto> savedSearches = savedSearchService.getSavedSearchesByUser(userId)
                .stream()
                .map(savedSearchService::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("announcements", announcements);
        model.addAttribute("favorites", favorites);
        model.addAttribute("savedSearches", savedSearches);
        model.addAttribute("userId", userId);
        model.addAttribute("isOwnProfile", userId.equals(userDetails.getId()));

        return "profile/view";
    }

    // Show edit profile form
    @GetMapping(value = "/{userId}/edit",
            produces = MediaType.TEXT_HTML_VALUE)
    public String editProfileForm(
            @PathVariable Long userId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        if (!userId.equals(userDetails.getId())) {
            return "redirect:/users/" + userId;
        }

        UserDto user = userService.convertToDto(userService.getUserById(userId));
        model.addAttribute("user", user);

        return "profile/edit";
    }

    // Update profile via form
    @PutMapping(value = "/{userId}",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String updateProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            Model model) {

        if (!userId.equals(userDetails.getId())) {
            model.addAttribute("error", "Vous ne pouvez modifier que votre propre profil");
            return "redirect:/users/" + userId;
        }

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPhoneNumber(phoneNumber);

        try {
            userService.updateUser(userId, request);
            return "redirect:/users/" + userId;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            UserDto user = userService.convertToDto(userService.getUserById(userId));
            model.addAttribute("user", user);
            return "profile/edit";
        }
    }
}