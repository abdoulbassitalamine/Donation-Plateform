package fr.isima.glia.donoplatform.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateBatchRequest {
    @NotNull(message = "Announcement IDs are required")
    @NotEmpty(message = "At least one announcement is required")
    private List<Long> announcementIds;
}
