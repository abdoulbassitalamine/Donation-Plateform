package fr.isima.glia.donoplatform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageDto {
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
