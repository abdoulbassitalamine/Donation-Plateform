package fr.isima.glia.donoplatform.service.announcement;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.dto.ImageDto;
import fr.isima.glia.donoplatform.dto.KeywordDto;
import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import fr.isima.glia.donoplatform.exceptions.AnnouncementNotFoundException;
import fr.isima.glia.donoplatform.exceptions.UnauthorizedAccessException;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.Keyword;
import fr.isima.glia.donoplatform.model.User;
import fr.isima.glia.donoplatform.repository.AnnouncementRepository;
import fr.isima.glia.donoplatform.request.CreateAnnouncementRequest;
import fr.isima.glia.donoplatform.request.SearchAnnouncementRequest;
import fr.isima.glia.donoplatform.request.UpdateAnnouncementRequest;
import fr.isima.glia.donoplatform.service.keyword.IKeywordService;
import fr.isima.glia.donoplatform.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService implements IAnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final IUserService userService;
    private final IKeywordService keywordService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Announcement createAnnouncement(CreateAnnouncementRequest request, Long userId) {
        User owner = userService.getUserById(userId);

        Set<Keyword> keywords = new HashSet<>();
        if (request.getKeywords() != null && !request.getKeywords().isEmpty()) {
            keywords = request.getKeywords().stream()
                    .map(keywordService::getOrCreateKeyword)
                    .collect(Collectors.toSet());
        }

        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .itemCondition(request.getItemCondition())
                .geographicArea(request.getGeographicArea())
                .handDelivery(request.getHandDelivery())
                .canShip(request.getCanShip())
                .status(AnnouncementStatus.AVAILABLE)
                .owner(owner)
                .keywords(keywords)
                .build();

        return announcementRepository.save(announcement);
    }

    @Override
    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new AnnouncementNotFoundException("Announcement not found with id: " + id));
    }

    @Override
    public AnnouncementDto convertToDto(Announcement announcement) {

        return modelMapper.map(announcement, AnnouncementDto.class);
    }

    @Override
    @Transactional
    public Announcement updateAnnouncement(Long id, UpdateAnnouncementRequest request, Long userId) {
        Announcement announcement = getAnnouncementById(id);

        if (!announcement.getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this announcement");
        }

        if (request.getTitle() != null) {
            announcement.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            announcement.setDescription(request.getDescription());
        }
        if (request.getItemCondition() != null) {
            announcement.setItemCondition(request.getItemCondition());
        }
        if (request.getGeographicArea() != null) {
            announcement.setGeographicArea(request.getGeographicArea());
        }
        if (request.getHandDelivery() != null) {
            announcement.setHandDelivery(request.getHandDelivery());
        }
        if (request.getCanShip() != null) {
            announcement.setCanShip(request.getCanShip());
        }
        if (request.getStatus() != null) {
            announcement.setStatus(request.getStatus());
        }
        if (request.getKeywords() != null) {
            Set<Keyword> keywords = request.getKeywords().stream()
                    .map(keywordService::getOrCreateKeyword)
                    .collect(Collectors.toSet());
            announcement.setKeywords(keywords);
        }

        return announcementRepository.save(announcement);
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id, Long userId) {
        Announcement announcement = getAnnouncementById(id);

        if (!announcement.getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this announcement");
        }

        announcementRepository.delete(announcement);
    }

    @Override
    public List<Announcement> getAnnouncementsByUser(Long userId) {
        User user = userService.getUserById(userId);
        return announcementRepository.findByOwner(user);
    }

    @Override
    public Page<Announcement> getAllAnnouncements(Pageable pageable) {
        return announcementRepository.findAll(pageable);
    }

    @Override
    public Page<Announcement> searchAnnouncements(SearchAnnouncementRequest request, Pageable pageable) {
        if (request.getSearchText() != null && !request.getSearchText().isEmpty()) {
            return announcementRepository.searchByText(request.getSearchText(), pageable);
        }

        return announcementRepository.searchAnnouncements(
                request.getGeographicArea(),
                request.getItemCondition(),
                request.getStatus(),
                request.getHandDelivery(),
                request.getCanShip(),
                pageable
        );
    }

    @Override
    @Transactional
    public Announcement updateAnnouncementStatus(Long id, AnnouncementStatus status, Long userId) {
        Announcement announcement = getAnnouncementById(id);

        if (!announcement.getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this announcement");
        }

        announcement.setStatus(status);
        return announcementRepository.save(announcement);
    }
}
