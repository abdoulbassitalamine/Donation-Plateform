package fr.isima.glia.donoplatform.controller;


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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final IMessageService messageService;

    // ============================================================
    // SEND MESSAGE - JSON ✅ CORRIGÉ
    // ============================================================
    @PostMapping(value = "/send",  // ✅ AJOUT de "/send"
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> sendMessageJson(
            @Valid @RequestBody CreateMessageRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Message message = messageService.sendMessage(request, userDetails.getId());
        MessageDto dto = messageService.convertToDto(message);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Message sent successfully", dto));
    }

    // ============================================================
    // GET MESSAGE BY ID - JSON (avec cache)
    // ============================================================
    @GetMapping(value = "/{messageId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getMessageByIdJson(
            @PathVariable Long messageId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Message message = messageService.getMessageById(messageId);

        // Vérifier que l'utilisateur est impliqué dans le message
        if (!message.getSender().getId().equals(userDetails.getId()) &&
                !message.getReceiver().getId().equals(userDetails.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse("Access denied", null));
        }

        MessageDto dto = messageService.convertToDto(message);

        // Générer ETag
        String etag = "\"" + Integer.toHexString(message.hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(1, TimeUnit.MINUTES))
                .body(new ApiResponse("Message found", dto));
    }

    // ============================================================
    // GET RECEIVED MESSAGES - JSON (avec cache)
    // ============================================================
    @GetMapping(value = "/received",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getReceivedMessagesJson(
            @AuthenticationPrincipal DonoUserDetails userDetails,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<MessageDto> messages = messageService.getMessagesByReceiver(userDetails.getId())
                .stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList());

        // Générer ETag
        String etag = "\"" + Integer.toHexString(messages.hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(new ApiResponse("Messages retrieved", messages));
    }

    // ============================================================
    // GET CONVERSATION - JSON (avec cache)
    // ============================================================
    @GetMapping(value = "/conversation",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getConversationJson(
            @RequestParam Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<MessageDto> messages = messageService.getConversation(userDetails.getId(), announcementId)
                .stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList());

        // Générer ETag
        String etag = "\"" + Integer.toHexString(messages.hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.SECONDS))
                .body(new ApiResponse("Conversation retrieved", messages));
    }

    // ============================================================
    // MARK AS READ - JSON
    // ============================================================
    @PatchMapping(value = "/{messageId}/read",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> markAsReadJson(
            @PathVariable Long messageId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        messageService.markAsRead(messageId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Message marked as read", null));
    }

    // ============================================================
    // GET UNREAD COUNT - JSON
    // ============================================================
    @GetMapping(value = "/unread-count",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getUnreadCountJson(
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        int count = messageService.getUnreadCount(userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Unread count", count));
    }

    // ============================================================
// LIST MESSAGES - HTML (conversations groupées)
// ============================================================
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String listMessagesHtml(
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        Long userId = userDetails.getId();

        // Récupérer tous les messages (reçus + envoyés)
        List<MessageDto> allMessages = new ArrayList<>();
        allMessages.addAll(messageService.getMessagesByReceiver(userId)
                .stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList()));

        allMessages.addAll(messageService.getMessagesBySender(userId)
                .stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList()));

        // Grouper par conversation (announcementId + autre utilisateur)
        Map<String, MessageDto> lastMessageByConversation = new HashMap<>();
        for (MessageDto msg : allMessages) {
            // Identifier l'autre utilisateur
            Long otherUserId = msg.getSenderId().equals(userId)
                    ? msg.getReceiverId()
                    : msg.getSenderId();

            // Clé unique : announcementId + otherUserId
            String conversationKey = msg.getAnnouncementId() + "_" + otherUserId;

            if (!lastMessageByConversation.containsKey(conversationKey) ||
                    msg.getSentAt().isAfter(lastMessageByConversation.get(conversationKey).getSentAt())) {
                lastMessageByConversation.put(conversationKey, msg);
            }
        }

        // Trier par date décroissante
        List<MessageDto> messages = lastMessageByConversation.values().stream()
                .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
                .collect(Collectors.toList());

        int unreadCount = messageService.getUnreadCount(userId);

        model.addAttribute("messages", messages);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("userId", userId);

        return "messages/list";
    }

    // ============================================================
// VIEW CONVERSATION - HTML
// ============================================================
    @GetMapping(value = "/conversation",
            produces = MediaType.TEXT_HTML_VALUE)
    public String viewConversationHtml(
            @RequestParam Long announcementId,
            @RequestParam(required = false) Long otherUserId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        Long userId = userDetails.getId();

        List<MessageDto> conversation = messageService.getConversation(userId, announcementId)
                .stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList());

        // Filtrer par l'autre utilisateur si spécifié
        if (otherUserId != null) {
            conversation = conversation.stream()
                    .filter(msg -> msg.getSenderId().equals(otherUserId)
                            || msg.getReceiverId().equals(otherUserId))
                    .collect(Collectors.toList());
        }

        // Calculer le receiverId
        Long receiverId;
        if (otherUserId != null) {
            receiverId = otherUserId;
        } else if (!conversation.isEmpty()) {
            MessageDto firstMsg = conversation.get(0);
            receiverId = firstMsg.getSenderId().equals(userId)
                    ? firstMsg.getReceiverId()
                    : firstMsg.getSenderId();
        } else {
            // Si pas de conversation, récupérer le propriétaire de l'annonce
            receiverId = messageService.getAnnouncementOwnerId(announcementId, userId);
        }

        model.addAttribute("conversation", conversation);
        model.addAttribute("userId", userId);
        model.addAttribute("announcementId", announcementId);
        model.addAttribute("receiverId", receiverId);

        return "messages/conversation";
    }

    // ============================================================
    // SEND MESSAGE - HTML (formulaire) - NE PAS UTILISER /send !
    // ============================================================
    @PostMapping(value = "/submit",  // ✅ CHANGÉ de "/send" à "/submit"
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String sendMessageHtml(
            @RequestParam Long receiverId,
            @RequestParam Long announcementId,
            @RequestParam String content,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        CreateMessageRequest request = new CreateMessageRequest();
        request.setReceiverId(receiverId);
        request.setAnnouncementId(announcementId);
        request.setContent(content);

        messageService.sendMessage(request, userDetails.getId());

        return "redirect:/messages/conversation?announcementId=" + announcementId;
    }
}