package fr.isima.glia.donoplatform.controller.web;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import fr.isima.glia.donoplatform.enums.ItemCondition;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.request.CreateAnnouncementRequest;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import fr.isima.glia.donoplatform.service.favorite.IFavoriteService;
import fr.isima.glia.donoplatform.service.image.IImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HTML views for announcement operations
 */
@Controller
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementWebController {

    private final IAnnouncementService announcementService;
    private final IImageService imageService;
    private final IFavoriteService favoriteService;

    // List announcements
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String listAnnouncements(
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

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String viewAnnouncement(
            @PathVariable("id") Long announcementId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        Announcement announcement = announcementService.getAnnouncementById(announcementId);
        AnnouncementDto dto = announcementService.convertToDto(announcement);

        boolean isFavorite = false;
        if (userDetails != null) {
            isFavorite = favoriteService.isFavorite(announcementId, userDetails.getId());
        }

        model.addAttribute("announcement", dto);
        model.addAttribute("isFavorite", isFavorite);
        model.addAttribute("userId", userDetails != null ? userDetails.getId() : null);

        return "announcements/detail";
    }

    // Show create form
    @GetMapping(value = "/create", produces = MediaType.TEXT_HTML_VALUE)
    public String showCreateForm(Model model) {
        model.addAttribute("announcement", new CreateAnnouncementRequest());
        model.addAttribute("itemConditions", ItemCondition.values());
        model.addAttribute("statuses", AnnouncementStatus.values());
        return "announcements/create";
    }

    // Create announcement via form
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String createAnnouncement(
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

            Announcement announcement = announcementService.createAnnouncement(request, userDetails.getId());

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
}