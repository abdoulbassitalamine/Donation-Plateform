package fr.isima.glia.donoplatform.controller.web;

import fr.isima.glia.donoplatform.dto.MessageDto;
import fr.isima.glia.donoplatform.request.CreateMessageRequest;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.message.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * HTML views for messaging operations
 */
@Controller
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageWebController {

    private final IMessageService messageService;

    // List messages (conversations grouped)
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String listMessages(
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        Long userId = userDetails.getId();

        List<MessageDto> allMessages = new ArrayList<>();
        allMessages.addAll(messageService.getMessagesByReceiver(userId)
                .stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList()));

        allMessages.addAll(messageService.getMessagesBySender(userId)
                .stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList()));

        Map<String, MessageDto> lastMessageByConversation = new HashMap<>();
        for (MessageDto msg : allMessages) {
            Long otherUserId = msg.getSenderId().equals(userId)
                    ? msg.getReceiverId()
                    : msg.getSenderId();

            String conversationKey = msg.getAnnouncementId() + "_" + otherUserId;

            if (!lastMessageByConversation.containsKey(conversationKey) ||
                    msg.getSentAt().isAfter(lastMessageByConversation.get(conversationKey).getSentAt())) {
                lastMessageByConversation.put(conversationKey, msg);
            }
        }

        List<MessageDto> messages = lastMessageByConversation.values().stream()
                .sorted((m1, m2) -> m2.getSentAt().compareTo(m1.getSentAt()))
                .collect(Collectors.toList());

        int unreadCount = messageService.getUnreadCount(userId);

        model.addAttribute("messages", messages);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("userId", userId);

        return "messages/list";
    }

    // View conversation
    @GetMapping(value = "/conversation",
            produces = MediaType.TEXT_HTML_VALUE)
    public String viewConversation(
            @RequestParam Long announcementId,
            @RequestParam(required = false) Long otherUserId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        Long userId = userDetails.getId();

        List<MessageDto> conversation = messageService.getConversation(userId, announcementId)
                .stream()
                .map(messageService::convertToDto)
                .collect(Collectors.toList());

        if (otherUserId != null) {
            conversation = conversation.stream()
                    .filter(msg -> msg.getSenderId().equals(otherUserId)
                            || msg.getReceiverId().equals(otherUserId))
                    .collect(Collectors.toList());
        }

        Long receiverId;
        if (otherUserId != null) {
            receiverId = otherUserId;
        } else if (!conversation.isEmpty()) {
            MessageDto firstMsg = conversation.get(0);
            receiverId = firstMsg.getSenderId().equals(userId)
                    ? firstMsg.getReceiverId()
                    : firstMsg.getSenderId();
        } else {
            receiverId = messageService.getAnnouncementOwnerId(announcementId, userId);
        }

        model.addAttribute("conversation", conversation);
        model.addAttribute("userId", userId);
        model.addAttribute("announcementId", announcementId);
        model.addAttribute("receiverId", receiverId);

        return "messages/conversation";
    }

    // Send message via form
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String sendMessage(
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