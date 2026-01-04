package fr.isima.glia.donoplatform.controller.api;

import fr.isima.glia.donoplatform.dto.MessageDto;
import fr.isima.glia.donoplatform.model.Message;
import fr.isima.glia.donoplatform.request.CreateMessageRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.message.IMessageService;
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
 * JSON API for messaging operations
 */
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageApiController {

    private final IMessageService messageService;

    // Send message
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> sendMessage(
            @Valid @RequestBody CreateMessageRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Message message = messageService.sendMessage(request, userDetails.getId());
        MessageDto dto = messageService.convertToDto(message);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Message sent successfully", dto));
    }

    // Get message by ID
    @GetMapping(value = "/{messageId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getMessageById(
            @PathVariable Long messageId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Message message = messageService.getMessageById(messageId);

        if (!message.getSender().getId().equals(userDetails.getId()) &&
                !message.getReceiver().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("Access denied", null));
        }

        MessageDto dto = messageService.convertToDto(message);
        String etag = "\"" + Integer.toHexString(message.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES))
                .body(new ApiResponse("Message found", dto));
    }

    // Get received messages
    @GetMapping(value = "/received",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getReceivedMessages(
            @AuthenticationPrincipal DonoUserDetails userDetails,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<MessageDto> messages = messageService.getMessagesByReceiver(userDetails.getId())
                .stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList());

        String etag = "\"" + Integer.toHexString(messages.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(new ApiResponse("Messages retrieved", messages));
    }

    // Get conversation
    @GetMapping(value = "/conversation",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getConversation(
            @RequestParam Long announcementId,
            @RequestParam(required = false) Long otherUserId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<MessageDto> messages = messageService.getConversation(userDetails.getId(), announcementId)
                .stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList());

        if (otherUserId != null) {
            messages = messages.stream()
                    .filter(msg -> msg.getSenderId().equals(otherUserId)
                            || msg.getReceiverId().equals(otherUserId))
                    .collect(Collectors.toList());
        }

        String etag = "\"" + Integer.toHexString(messages.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(new ApiResponse("Conversation retrieved", messages));
    }

    // Mark as read
    @PatchMapping(value = "/{messageId}/read",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable Long messageId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        messageService.markAsRead(messageId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Message marked as read", null));
    }

    // Get unread count
    @GetMapping(value = "/unread-count",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getUnreadCount(
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        int count = messageService.getUnreadCount(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Unread count", count));
    }
}