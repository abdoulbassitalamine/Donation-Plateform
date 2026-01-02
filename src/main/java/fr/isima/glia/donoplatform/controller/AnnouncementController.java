package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import fr.isima.glia.donoplatform.enums.ItemCondition;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.request.CreateAnnouncementRequest;
import fr.isima.glia.donoplatform.request.UpdateAnnouncementRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import fr.isima.glia.donoplatform.service.image.IImageService;
import fr.isima.glia.donoplatform.service.favorite.IFavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Contrôleur gérant les annonces (API JSON et Vues HTML).
 */
@Controller
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final IAnnouncementService announcementService;
    private final IImageService imageService;
    private final IFavoriteService favoriteService;

    // ============================================================
    // LISTE DES ANNONCES
    // ============================================================

    /**
     * API : Liste des annonces au format JSON.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> listJson(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "publicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Announcement> announcementPage = announcementService.getAllAnnouncements(pageable);
        Page<AnnouncementDto> dtoPage = announcementPage.map(announcementService::convertToDto);

        String etag = "\"" + Integer.toHexString(dtoPage.getContent().hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(new ApiResponse("Announcements retrieved", dtoPage));
    }

    /**
     * WEB : Liste des annonces au format HTML.
     */
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String listHtml(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "publicationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            Model model) {

        Sort sort = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Announcement> announcementPage = announcementService.getAllAnnouncements(pageable);
        Page<AnnouncementDto> dtoPage = announcementPage.map(announcementService::convertToDto);

        model.addAttribute("announcements", dtoPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", dtoPage.getTotalPages());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortOrder", sortOrder);

        return "announcements/list";
    }

    // ============================================================
    // DÉTAIL D'UNE ANNONCE
    // ============================================================

    /**
     * API : Détail d'une annonce en JSON.
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getByIdJson(
            @PathVariable Long id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Announcement announcement = announcementService.getAnnouncementById(id);
        AnnouncementDto dto = announcementService.convertToDto(announcement);

        String etag = "\"" + Integer.toHexString(announcement.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES).cachePublic())
                .body(new ApiResponse("Announcement found", dto));
    }

    /**
     * WEB : Détail d'une annonce en HTML (avec gestion des favoris).
     */
    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String getByIdHtml(
            @PathVariable Long id,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        Announcement announcement = announcementService.getAnnouncementById(id);
        AnnouncementDto dto = announcementService.convertToDto(announcement);

        // Vérification si l'annonce est en favori pour l'utilisateur connecté
        boolean isFavorite = false;
        if (userDetails != null) {
            isFavorite = favoriteService.isFavorite(userDetails.getId(), id);
        }

        model.addAttribute("announcement", dto);
        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("userId", userDetails != null ? userDetails.getId() : null);

        return "announcements/detail";
    }

    // ============================================================
    // CRÉATION D'ANNONCE
    // ============================================================

    /**
     * WEB : Affiche le formulaire de création.
     */
    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    public String showCreateForm(Model model) {
        model.addAttribute("announcement", new CreateAnnouncementRequest());
        model.addAttribute("itemConditions", ItemCondition.values());
        model.addAttribute("statuses", AnnouncementStatus.values());
        return "announcements/create";
    }

    /**
     * WEB : Traitement du formulaire de création (Multipart pour les images).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String createHtml(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam ItemCondition itemCondition,
            @RequestParam String geographicArea,
            @RequestParam(defaultValue = "false") Boolean handDelivery,
            @RequestParam(defaultValue = "false") Boolean canShip,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) List<MultipartFile> images,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            CreateAnnouncementRequest request = new CreateAnnouncementRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setItemCondition(itemCondition);
            request.setGeographicArea(geographicArea);
            request.setHandDelivery(handDelivery);
            request.setCanShip(canShip);

            if (keywords != null && !keywords.isBlank()) {
                Set<String> keywordSet = Arrays.stream(keywords.split(","))
                        .map(String::trim)
                        .filter(k -> !k.isEmpty())
                        .collect(Collectors.toSet());
                request.setKeywords(keywordSet);
            }

            // Création de l'annonce
            Announcement announcement = announcementService.createAnnouncement(request, userDetails.getId());

            // Sauvegarde des images si présentes
            if (images != null && !images.isEmpty()) {
                for (MultipartFile file : images) {
                    if (!file.isEmpty()) {
                        imageService.saveImage(file, announcement.getId());
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success", "Annonce créée avec succès !");
            return "redirect:/announcements/" + announcement.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création : " + e.getMessage());
            return "redirect:/announcements/create";
        }
    }

    /**
     * API : Création via JSON.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> createJson(
            @Valid @RequestBody CreateAnnouncementRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Announcement announcement = announcementService.createAnnouncement(request, userDetails.getId());
        AnnouncementDto dto = announcementService.convertToDto(announcement);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Announcement created successfully", dto));
    }

    // ============================================================
    // ACTIONS DIVERSES (UPDATE, DELETE, etc.)
    // ============================================================

    @PutMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> updateJson(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAnnouncementRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Announcement updated = announcementService.updateAnnouncement(id, request, userDetails.getId());
        AnnouncementDto dto = announcementService.convertToDto(updated);

        return ResponseEntity.ok(new ApiResponse("Announcement updated successfully", dto));
    }

    @PatchMapping(value = "/{id}/status",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> updateStatusJson(
            @PathVariable Long id,
            @RequestParam AnnouncementStatus status,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Announcement updated = announcementService.updateAnnouncementStatus(id, status, userDetails.getId());
        AnnouncementDto dto = announcementService.convertToDto(updated);

        return ResponseEntity.ok(new ApiResponse("Status updated successfully", dto));
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> deleteJson(
            @PathVariable Long id,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        announcementService.deleteAnnouncement(id, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Announcement deleted successfully", null));
    }

    @GetMapping(value = "/user/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getUserAnnouncementsJson(@PathVariable Long userId) {
        var announcements = announcementService.getAnnouncementsByUser(userId).stream()
                .map(announcementService::convertToDto)
                .toList();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(2, TimeUnit.MINUTES))
                .body(new ApiResponse("User announcements retrieved", announcements));
    }
}