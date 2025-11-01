package fr.isima.glia.donoplatform.repository;

import fr.isima.glia.donoplatform.model.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    Optional<Keyword> findByName(String name);
    boolean existsByName(String name);
}
