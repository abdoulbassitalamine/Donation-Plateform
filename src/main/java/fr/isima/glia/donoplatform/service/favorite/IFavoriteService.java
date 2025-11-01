package fr.isima.glia.donoplatform.service.favorite;

import fr.isima.glia.donoplatform.dto.FavoriteDto;
import fr.isima.glia.donoplatform.model.Favorite;

import java.util.List;

public interface IFavoriteService {
    Favorite addFavorite(Long announcementId, Long userId);
    void removeFavorite(Long announcementId, Long userId);
    List<Favorite> getFavoritesByUser(Long userId);
    FavoriteDto convertToDto(Favorite favorite);
    boolean isFavorite(Long announcementId, Long userId);
}
