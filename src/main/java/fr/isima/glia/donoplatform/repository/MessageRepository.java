package fr.isima.glia.donoplatform.repository;

import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.Message;
import fr.isima.glia.donoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySender(User sender);
    List<Message> findByReceiver(User receiver);
    List<Message> findByAnnouncement(Announcement announcement);

    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender = :user OR m.receiver = :user) AND m.announcement = :announcement " +
           "ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("user") User user, @Param("announcement") Announcement announcement);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver = :receiver AND m.read = false")
    int countUnreadMessages(@Param("receiver") User receiver);

    List<Message> findByReceiverAndReadFalse(User receiver);
}
