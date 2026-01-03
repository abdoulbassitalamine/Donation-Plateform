package fr.isima.glia.donoplatform.service.notification;

import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.SavedSearch;
import fr.isima.glia.donoplatform.repository.SavedSearchRepository;
import fr.isima.glia.donoplatform.request.SearchAnnouncementRequest;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final SavedSearchRepository savedSearchRepository;
    private final IAnnouncementService announcementService;
    private final NotificationService notificationService;

    private LocalDateTime lastCheckTime = LocalDateTime.now();


    @Scheduled(fixedDelay = 30000)
    public void checkNewAnnouncementsForSavedSearches() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("🔍 Vérification des nouvelles annonces depuis {}", lastCheckTime);

        List<SavedSearch> activeSearches = savedSearchRepository.findAll().stream()
                .filter(s -> s.getNotificationsEnabled() != null && s.getNotificationsEnabled())
                .toList();

        log.debug("📋 {} recherche(s) active(s) avec notifications", activeSearches.size());

        for (SavedSearch savedSearch : activeSearches) {
            try {
                checkSavedSearch(savedSearch, lastCheckTime, now);
            } catch (Exception e) {
                log.error("❌ Erreur lors de la vérification de la recherche {}: {}",
                        savedSearch.getId(), e.getMessage());
            }
        }

        lastCheckTime = now;
    }

    private void checkSavedSearch(SavedSearch savedSearch, LocalDateTime since, LocalDateTime until) {

        SearchAnnouncementRequest request = new SearchAnnouncementRequest();
        request.setGeographicArea(savedSearch.getGeographicArea());
        request.setItemCondition(savedSearch.getItemCondition());
        request.setHandDelivery(savedSearch.getHandDelivery());
        request.setCanShip(savedSearch.getCanShip());


        Page<Announcement> results = announcementService.searchAnnouncements(
                request,
                PageRequest.of(0, 100)
        );


        List<Announcement> newAnnouncements = results.getContent().stream()
                .filter(a -> a.getPublicationDate().isAfter(since) &&
                        a.getPublicationDate().isBefore(until))
                .filter(a -> !a.getOwner().getId().equals(savedSearch.getUser().getId())) // Pas ses propres annonces
                .toList();


        for (Announcement announcement : newAnnouncements) {
            notificationService.createNotification(
                    savedSearch.getUser(),
                    announcement,
                    savedSearch
            );
        }

        if (!newAnnouncements.isEmpty()) {
            log.info("✅ Créé {} notification(s) pour la recherche '{}' (user {})",
                    newAnnouncements.size(),
                    savedSearch.getName(),
                    savedSearch.getUser().getId());
        }
    }
}