package fr.isima.glia.donoplatform.controller.web;

import fr.isima.glia.donoplatform.dto.NotificationDto;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationWebController {

    private final NotificationService notificationService;

    // List all notifications
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String listNotifications(
            @AuthenticationPrincipal DonoUserDetails userDetails,
            @RequestParam(required = false, defaultValue = "false") Boolean unreadOnly,
            Model model) {

        List<NotificationDto> notifications = notificationService.getUserNotifications(
                userDetails.getId(),
                unreadOnly
        );

        Long unreadCount = notificationService.getUnreadCount(userDetails.getId());

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("unreadOnly", unreadOnly);

        return "notifications/list";
    }

    // Mark notification as read (via POST redirect)
    @PostMapping(value = "/{id}/read",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        notificationService.markAsRead(id, userDetails.getId());
        return "redirect:/notifications";
    }

    // Mark all as read
    @PostMapping(value = "/read-all",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String markAllAsRead(@AuthenticationPrincipal DonoUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getId());
        return "redirect:/notifications";
    }
}