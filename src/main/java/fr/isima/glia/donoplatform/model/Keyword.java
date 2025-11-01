package fr.isima.glia.donoplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "keywords")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    // Relations
    @ManyToMany(mappedBy = "keywords")
    @ToString.Exclude
    @Builder.Default
    private Set<Announcement> announcements = new HashSet<>();

    @ManyToMany(mappedBy = "keywords")
    @ToString.Exclude
    @Builder.Default
    private Set<SavedSearch> savedSearches = new HashSet<>();
}
