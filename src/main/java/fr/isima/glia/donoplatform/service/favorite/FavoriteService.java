package fr.isima.glia.donoplatform.service.favorite;

import fr.isima.glia.donoplatform.dto.FavoriteDto;
import fr.isima.glia.donoplatform.exceptions.AlreadyExistsException;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.Favorite;
import fr.isima.glia.donoplatform.model.User;
import fr.isima.glia.donoplatform.repository.FavoriteRepository;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import fr.isima.glia.donoplatform.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import fr.isima.glia.donoplatform.controller.FavoriteController;
import fr.isima.glia.donoplatform.controller.AnnouncementController;

@Service
@RequiredArgsConstructor
public class FavoriteService implements IFavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final IUserService userService;
    private final IAnnouncementService announcementService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Favorite addFavorite(Long announcementId, Long userId) {
        User user = userService.getUserById(userId);
        Announcement announcement = announcementService.getAnnouncementById(announcementId);

        if (favoriteRepository.existsByUserAndAnnouncement(user, announcement)) {
            throw new AlreadyExistsException("Announcement is already in favorites");
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .announcement(announcement)
                .build();

        return favoriteRepository.save(favorite);
    }

    @Override
    @Transactional
    public void removeFavorite(Long announcementId, Long userId) {
        User user = userService.getUserById(userId);
        Announcement announcement = announcementService.getAnnouncementById(announcementId);

        favoriteRepository.deleteByUserAndAnnouncement(user, announcement);
    }

    @Override
    public List<Favorite> getFavoritesByUser(Long userId) {
        User user = userService.getUserById(userId);
        return favoriteRepository.findByUser(user);
    }

    @Override
    public FavoriteDto convertToDto(Favorite favorite) {
        FavoriteDto dto = modelMapper.map(favorite, FavoriteDto.class);

        // ============================================================
        // HATEOAS - Ajout des liens hypermedia
        // ============================================================
        try {
            // Self link
            dto.add(linkTo(methodOn(FavoriteController.class)
                    .getUserFavoritesJson(favorite.getUser().getId(), null))
                    .withSelfRel());

            // Announcement link
            dto.add(linkTo(methodOn(AnnouncementController.class)
                    .getByIdJson(favorite.getAnnouncement().getId(), null))
                    .withRel("announcement"));

            // Delete link
            dto.add(linkTo(methodOn(FavoriteController.class)
                    .removeFavoriteJson(favorite.getAnnouncement().getId(), null))
                    .withRel("delete"));

        } catch (Exception e) {
            // Si erreur de génération de lien, on continue sans bloquer
        }

        return dto;
    }

    @Override
    public boolean isFavorite(Long announcementId, Long userId) {
        User user = userService.getUserById(userId);
        Announcement announcement = announcementService.getAnnouncementById(announcementId);
        return favoriteRepository.existsByUserAndAnnouncement(user, announcement);
    }
}