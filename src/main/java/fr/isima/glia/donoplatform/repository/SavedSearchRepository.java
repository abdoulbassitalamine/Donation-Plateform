package fr.isima.glia.donoplatform.repository;

import fr.isima.glia.donoplatform.model.SavedSearch;
import fr.isima.glia.donoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {
    List<SavedSearch> findByUser(User user);
    List<SavedSearch> findByUserAndNotificationsEnabled(User user, boolean notificationsEnabled);
}
