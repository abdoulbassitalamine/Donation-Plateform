package fr.isima.glia.donoplatform.service.message;

import fr.isima.glia.donoplatform.dto.MessageDto;
import fr.isima.glia.donoplatform.exceptions.ResourceNotFoundException;
import fr.isima.glia.donoplatform.exceptions.UnauthorizedAccessException;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.Message;
import fr.isima.glia.donoplatform.model.User;
import fr.isima.glia.donoplatform.repository.MessageRepository;
import fr.isima.glia.donoplatform.request.CreateMessageRequest;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import fr.isima.glia.donoplatform.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {

    private final MessageRepository messageRepository;
    private final IUserService userService;
    private final IAnnouncementService announcementService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Message sendMessage(CreateMessageRequest request, Long senderId) {
        User sender = userService.getUserById(senderId);
        User receiver = userService.getUserById(request.getReceiverId());
        Announcement announcement = announcementService.getAnnouncementById(request.getAnnouncementId());

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .announcement(announcement)
                .content(request.getContent())
                .read(false)
                .build();

        return messageRepository.save(message);
    }

    @Override
    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + id));
    }

    @Override
    public MessageDto convertToDto(Message message) {
        MessageDto dto = modelMapper.map(message, MessageDto.class);
        return dto;
    }

    @Override
    public List<Message> getMessagesByReceiver(Long receiverId) {
        User receiver = userService.getUserById(receiverId);
        return messageRepository.findByReceiver(receiver);
    }

    @Override
    public List<Message> getConversation(Long userId, Long announcementId) {
        User user = userService.getUserById(userId);
        Announcement announcement = announcementService.getAnnouncementById(announcementId);
        return messageRepository.findConversation(user, announcement);
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        Message message = getMessageById(messageId);

        if (!message.getReceiver().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to mark this message as read");
        }

        message.setRead(true);
        messageRepository.save(message);
    }

    @Override
    public int getUnreadCount(Long userId) {
        User user = userService.getUserById(userId);
        return messageRepository.countUnreadMessages(user);
    }
}
