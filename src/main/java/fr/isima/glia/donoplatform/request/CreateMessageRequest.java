package fr.isima.glia.donoplatform.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMessageRequest {
    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotNull(message = "Announcement ID is required")
    private Long announcementId;

    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content must not exceed 2000 characters")
    private String content;
}
