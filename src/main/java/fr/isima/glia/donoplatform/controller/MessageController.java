package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.MessageDto;
import fr.isima.glia.donoplatform.model.Message;
import fr.isima.glia.donoplatform.request.CreateMessageRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.service.message.IMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final IMessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse> sendMessage(
            @Valid @RequestBody CreateMessageRequest request,
            @RequestParam Long senderId) {
        Message message = messageService.sendMessage(request, senderId);
        MessageDto dto = messageService.convertToDto(message);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Message sent successfully", dto));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<ApiResponse> getMessageById(@PathVariable Long messageId) {
        Message message = messageService.getMessageById(messageId);
        MessageDto dto = messageService.convertToDto(message);
        return ResponseEntity.ok(new ApiResponse("Message found", dto));
    }

    @GetMapping("/received/{userId}")
    public ResponseEntity<ApiResponse> getReceivedMessages(@PathVariable Long userId) {
        List<MessageDto> messages = messageService.getMessagesByReceiver(userId).stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse("Messages retrieved", messages));
    }

    @GetMapping("/conversation")
    public ResponseEntity<ApiResponse> getConversation(
            @RequestParam Long userId,
            @RequestParam Long announcementId) {
        List<MessageDto> messages = messageService.getConversation(userId, announcementId).stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse("Conversation retrieved", messages));
    }

    @PatchMapping("/{messageId}/read")
    public ResponseEntity<ApiResponse> markAsRead(
            @PathVariable Long messageId,
            @RequestParam Long userId) {
        messageService.markAsRead(messageId, userId);
        return ResponseEntity.ok(new ApiResponse("Message marked as read", null));
    }

    @GetMapping("/unread-count/{userId}")
    public ResponseEntity<ApiResponse> getUnreadCount(@PathVariable Long userId) {
        int count = messageService.getUnreadCount(userId);
        return ResponseEntity.ok(new ApiResponse("Unread count", count));
    }
}
