package fr.isima.glia.donoplatform.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageDto extends RepresentationModel<MessageDto> {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private Long receiverId;
    private String receiverUsername;
    private Long announcementId;
    private String announcementTitle;
    private String content;
    private LocalDateTime sentAt;
    private Boolean read;
}