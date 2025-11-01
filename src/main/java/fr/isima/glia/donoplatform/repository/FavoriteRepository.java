package fr.isima.glia.donoplatform.repository;

import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.Favorite;
import fr.isima.glia.donoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUser(User user);
    Optional<Favorite> findByUserAndAnnouncement(User user, Announcement announcement);
    boolean existsByUserAndAnnouncement(User user, Announcement announcement);
    void deleteByUserAndAnnouncement(User user, Announcement announcement);
}
