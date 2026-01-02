package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.dto.FavoriteDto;
import fr.isima.glia.donoplatform.dto.SavedSearchDto;
import fr.isima.glia.donoplatform.dto.UserDto;
import fr.isima.glia.donoplatform.model.User;
import fr.isima.glia.donoplatform.request.UserUpdateRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import fr.isima.glia.donoplatform.service.favorite.IFavoriteService;
import fr.isima.glia.donoplatform.service.savedsearch.ISavedSearchService;
import fr.isima.glia.donoplatform.service.user.IUserService;
import jakarta.validation.Valid;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final IAnnouncementService announcementService;
    private final IFavoriteService favoriteService;
    private final ISavedSearchService savedSearchService;

    // ============================================================
    // GET USER BY ID - JSON (avec cache)
    // ============================================================
    @GetMapping(value = "/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getUserByIdJson(
            @PathVariable Long userId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        User user = userService.getUserById(userId);
        UserDto userDto = userService.convertToDto(user);

        // Générer ETag
        String etag = "\"" + Integer.toHexString(user.hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(new ApiResponse("User found", userDto));
    }

    // ============================================================
    // GET USER BY USERNAME - JSON
    // ============================================================
    @GetMapping(value = "/username/{username}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getUserByUsernameJson(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        UserDto userDto = userService.convertToDto(user);
        return ResponseEntity.ok(new ApiResponse("User found", userDto));
    }

    // ============================================================
    // GET ALL USERS - JSON (avec cache)
    // ============================================================
    @GetMapping(value = "/all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getAllUsersJson(
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(userService::convertToDto)
                .collect(Collectors.toList());

        // Générer ETag
        String etag = "\"" + Integer.toHexString(userDtos.hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(new ApiResponse("Users retrieved", userDtos));
    }

    // ============================================================
    // UPDATE USER - JSON
    // ============================================================
    @PutMapping(value = "/{userId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> updateUserJson(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        // Vérifier que l'utilisateur modifie son propre profil
        if (!userId.equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("You can only update your own profile", null));
        }

        User updatedUser = userService.updateUser(userId, request);
        UserDto userDto = userService.convertToDto(updatedUser);
        return ResponseEntity.ok(new ApiResponse("User updated successfully", userDto));
    }

    // ============================================================
    // DELETE USER - JSON
    // ============================================================
    @DeleteMapping(value = "/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> deleteUserJson(
            @PathVariable Long userId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        // Vérifier que l'utilisateur supprime son propre profil
        if (!userId.equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("You can only delete your own profile", null));
        }

        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse("User deleted successfully", null));
    }

    // ============================================================
    // VIEW PROFILE - HTML
    // ============================================================
    @GetMapping(value = "/{userId}",
            produces = MediaType.TEXT_HTML_VALUE)
    public String viewProfileHtml(
            @PathVariable Long userId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        // Get user info
        UserDto user = userService.convertToDto(userService.getUserById(userId));

        // Get user's announcements
        List<AnnouncementDto> announcements = announcementService.getAnnouncementsByUser(userId)
                .stream()
                .map(announcementService::convertToDto)
                .collect(Collectors.toList());

        // Get user's favorites
        List<FavoriteDto> favorites = favoriteService.getFavoritesByUser(userId)
                .stream()
                .map(favoriteService::convertToDto)
                .collect(Collectors.toList());

        // Get user's saved searches
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

    // ============================================================
    // EDIT PROFILE - HTML (GET - afficher formulaire)
    // ============================================================
    @GetMapping(value = "/edit",
            produces = MediaType.TEXT_HTML_VALUE)
    public String editProfileFormHtml(
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        UserDto user = userService.convertToDto(userService.getUserById(userDetails.getId()));
        model.addAttribute("user", user);

        return "profile/edit";
    }

    // ============================================================
    // UPDATE PROFILE - HTML (POST formulaire)
    // ============================================================
    @PostMapping(value = "/edit",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String updateProfileHtml(
            @AuthenticationPrincipal DonoUserDetails userDetails,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String phoneNumber,
            Model model) {

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPhoneNumber(phoneNumber);

        try {
            userService.updateUser(userDetails.getId(), request);
            return "redirect:/users/" + userDetails.getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            UserDto user = userService.convertToDto(userService.getUserById(userDetails.getId()));
            model.addAttribute("user", user);
            return "profile/edit";
        }
    }
}