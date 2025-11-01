package fr.isima.glia.donoplatform.repository;

import fr.isima.glia.donoplatform.model.Batch;
import fr.isima.glia.donoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findByOwner(User owner);
}
