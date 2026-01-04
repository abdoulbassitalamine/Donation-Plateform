package fr.isima.glia.donoplatform.controller.api;

import fr.isima.glia.donoplatform.dto.NotificationDto;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationApiController {

    private final NotificationService notificationService;

    // Get all notifications
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getNotifications(
            @AuthenticationPrincipal DonoUserDetails userDetails,
            @RequestParam(required = false) Boolean unreadOnly) {

        List<NotificationDto> notifications = notificationService.getUserNotifications(
                userDetails.getId(),
                unreadOnly
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS))
                .body(new ApiResponse("Notifications retrieved", notifications));
    }

    // Get unread count
    @GetMapping(value = "/unread/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getUnreadCount(@AuthenticationPrincipal DonoUserDetails userDetails) {
        Long count = notificationService.getUnreadCount(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Unread count", count));
    }

    // Mark as read
    @PatchMapping(value = "/{id}/read", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        notificationService.markAsRead(id, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Notification marked as read", null));
    }

    // Mark all as read
    @PatchMapping(value = "/read-all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> markAllAsRead(@AuthenticationPrincipal DonoUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("All notifications marked as read", null));
    }
}