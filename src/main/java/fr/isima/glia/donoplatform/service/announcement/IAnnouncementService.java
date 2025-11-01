package fr.isima.glia.donoplatform.service.announcement;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.request.CreateAnnouncementRequest;
import fr.isima.glia.donoplatform.request.SearchAnnouncementRequest;
import fr.isima.glia.donoplatform.request.UpdateAnnouncementRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IAnnouncementService {
    Announcement createAnnouncement(CreateAnnouncementRequest request, Long userId);
    Announcement getAnnouncementById(Long id);
    AnnouncementDto convertToDto(Announcement announcement);
    Announcement updateAnnouncement(Long id, UpdateAnnouncementRequest request, Long userId);
    void deleteAnnouncement(Long id, Long userId);
    List<Announcement> getAnnouncementsByUser(Long userId);
    Page<Announcement> getAllAnnouncements(Pageable pageable);
    Page<Announcement> searchAnnouncements(SearchAnnouncementRequest request, Pageable pageable);
    Announcement updateAnnouncementStatus(Long id, AnnouncementStatus status, Long userId);
}
