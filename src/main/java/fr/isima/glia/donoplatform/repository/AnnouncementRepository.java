package fr.isima.glia.donoplatform.repository;

import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import fr.isima.glia.donoplatform.enums.ItemCondition;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByOwner(User owner);
    List<Announcement> findByStatus(AnnouncementStatus status);
    Page<Announcement> findByStatus(AnnouncementStatus status, Pageable pageable);
    List<Announcement> findByGeographicArea(String geographicArea);
    List<Announcement> findByItemCondition(ItemCondition itemCondition);

    @Query("SELECT a FROM Announcement a WHERE a.status = :status AND a.owner = :owner")
    List<Announcement> findByStatusAndOwner(@Param("status") AnnouncementStatus status, @Param("owner") User owner);

    @Query("SELECT a FROM Announcement a JOIN a.keywords k WHERE k.name IN :keywords")
    List<Announcement> findByKeywords(@Param("keywords") List<String> keywords);

    @Query("SELECT a FROM Announcement a WHERE " +
           "(:geographicArea IS NULL OR a.geographicArea = :geographicArea) AND " +
           "(:itemCondition IS NULL OR a.itemCondition = :itemCondition) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:handDelivery IS NULL OR a.handDelivery = :handDelivery) AND " +
           "(:canShip IS NULL OR a.canShip = :canShip)")
    Page<Announcement> searchAnnouncements(
        @Param("geographicArea") String geographicArea,
        @Param("itemCondition") ItemCondition itemCondition,
        @Param("status") AnnouncementStatus status,
        @Param("handDelivery") Boolean handDelivery,
        @Param("canShip") Boolean canShip,
        Pageable pageable
    );

    @Query("SELECT a FROM Announcement a WHERE " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    Page<Announcement> searchByText(@Param("searchText") String searchText, Pageable pageable);
}
