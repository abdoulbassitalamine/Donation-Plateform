package fr.isima.glia.donoplatform.model;

import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import fr.isima.glia.donoplatform.enums.ItemCondition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "announcements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCondition itemCondition;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime publicationDate;

    @Column(nullable = false, length = 100)
    private String geographicArea;

    @Column(nullable = false)
    private Boolean handDelivery = true;

    @Column(nullable = false)
    private Boolean canShip = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AnnouncementStatus status = AnnouncementStatus.AVAILABLE;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    private User owner;

    @ManyToMany
    @JoinTable(
        name = "announcement_keywords",
        joinColumns = @JoinColumn(name = "announcement_id"),
        inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    @Builder.Default
    private Set<Keyword> keywords = new HashSet<>();

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private Set<Favorite> favorites = new HashSet<>();

    @ManyToMany(mappedBy = "announcements")
    @ToString.Exclude
    @Builder.Default
    private Set<Batch> batches = new HashSet<>();

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private Set<Message> messages = new HashSet<>();

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private Set<Image> images = new HashSet<>();

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, status, publicationDate, itemCondition);
    }
}
