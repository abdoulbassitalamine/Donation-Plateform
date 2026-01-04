package fr.isima.glia.donoplatform.controller.api;

import fr.isima.glia.donoplatform.dto.UserDto;
import fr.isima.glia.donoplatform.model.User;
import fr.isima.glia.donoplatform.request.UserUpdateRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.user.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JSON API for user operations
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserApiController {

    private final IUserService userService;

    // Get user by ID
    @GetMapping(value = "/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getUserById(
            @PathVariable Long userId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        User user = userService.getUserById(userId);
        UserDto userDto = userService.convertToDto(user);

        String etag = "\"" + Integer.toHexString(user.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES))
                .body(new ApiResponse("User found", userDto));
    }

    // Get user by username
    @GetMapping(value = "/username/{username}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        UserDto userDto = userService.convertToDto(user);
        return ResponseEntity.ok(new ApiResponse("User found", userDto));
    }

    // Get all users
    @GetMapping(value = "/all",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getAllUsers(
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<User> users = userService.getAllUsers();
        List<UserDto> userDtos = users.stream()
                .map(userService::convertToDto)
                .collect(Collectors.toList());

        String etag = "\"" + Integer.toHexString(userDtos.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(new ApiResponse("Users retrieved", userDtos));
    }

    // Update user
    @PutMapping(value = "/{userId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        if (!userId.equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("You can only update your own profile", null));
        }

        User updatedUser = userService.updateUser(userId, request);
        UserDto userDto = userService.convertToDto(updatedUser);
        return ResponseEntity.ok(new ApiResponse("User updated successfully", userDto));
    }

    // Delete user
    @DeleteMapping(value = "/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        if (!userId.equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("You can only delete your own profile", null));
        }

        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse("User deleted successfully", null));
    }
}