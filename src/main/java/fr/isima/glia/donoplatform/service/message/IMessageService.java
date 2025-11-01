package fr.isima.glia.donoplatform.service.message;

import fr.isima.glia.donoplatform.dto.MessageDto;
import fr.isima.glia.donoplatform.model.Message;
import fr.isima.glia.donoplatform.request.CreateMessageRequest;

import java.util.List;

public interface IMessageService {
    Message sendMessage(CreateMessageRequest request, Long senderId);
    Message getMessageById(Long id);
    MessageDto convertToDto(Message message);
    List<Message> getMessagesByReceiver(Long receiverId);
    List<Message> getConversation(Long userId, Long announcementId);
    void markAsRead(Long messageId, Long userId);
    int getUnreadCount(Long userId);
}
