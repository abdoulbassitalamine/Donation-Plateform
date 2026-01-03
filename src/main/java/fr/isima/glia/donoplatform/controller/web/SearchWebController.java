package fr.isima.glia.donoplatform.controller.web;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.enums.AnnouncementStatus;
import fr.isima.glia.donoplatform.enums.ItemCondition;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.SavedSearch;
import fr.isima.glia.donoplatform.request.CreateSavedSearchRequest;
import fr.isima.glia.donoplatform.request.SearchAnnouncementRequest;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import fr.isima.glia.donoplatform.service.keyword.IKeywordService;
import fr.isima.glia.donoplatform.service.savedsearch.ISavedSearchService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HTML views for search operations
 */
@Controller
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchWebController {

    private final IAnnouncementService announcementService;
    private final IKeywordService keywordService;
    private final ISavedSearchService savedSearchService;

    // Show search form
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String showSearchForm(Model model) {
        model.addAttribute("searchRequest", new SearchAnnouncementRequest());
        model.addAttribute("itemConditions", ItemCondition.values());
        model.addAttribute("statuses", AnnouncementStatus.values());
        model.addAttribute("keywords", keywordService.getAllKeywords());
        return "search/form";
    }

    // Search results via POST form
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String searchResults(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String geographicArea,
            @RequestParam(required = false) ItemCondition itemCondition,
            @RequestParam(required = false) AnnouncementStatus status,
            @RequestParam(required = false) Boolean handDelivery,
            @RequestParam(required = false) Boolean canShip,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        SearchAnnouncementRequest request = new SearchAnnouncementRequest();
        request.setSearchText(searchText);
        request.setGeographicArea(geographicArea);
        request.setItemCondition(itemCondition);
        request.setStatus(status);
        request.setHandDelivery(handDelivery);
        request.setCanShip(canShip);

        Pageable pageable = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        Page<Announcement> announcementPage = announcementService.searchAnnouncements(request, pageable);
        Page<AnnouncementDto> dtoPage = announcementPage.map(announcementService::convertToDto);

        model.addAttribute("announcements", dtoPage);
        model.addAttribute("searchRequest", request);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", dtoPage.getTotalPages());

        return "search/results";
    }



    /**
     * Afficher le formulaire pour sauvegarder une recherche
     */
    @GetMapping(value = "/save", produces = MediaType.TEXT_HTML_VALUE)
    public String showSaveSearchForm(
            @RequestParam(required = false) String searchText,
            @RequestParam(required = false) String geographicArea,
            @RequestParam(required = false) ItemCondition itemCondition,
            @RequestParam(required = false) String keywords,
            Model model) {

        CreateSavedSearchRequest request = new CreateSavedSearchRequest();
        request.setGeographicArea(geographicArea);
        request.setItemCondition(itemCondition);

        // Convertir les mots-clés depuis la chaîne
        if (keywords != null && !keywords.isBlank()) {
            Set<String> keywordSet = Arrays.stream(keywords.split(","))
                    .map(String::trim)
                    .filter(k -> !k.isEmpty())
                    .collect(Collectors.toSet());
            request.setKeywords(keywordSet);
        }

        model.addAttribute("savedSearchRequest", request);
        model.addAttribute("searchText", searchText);

        return "search/save";
    }

    /**
     * Sauvegarder une recherche
     */
    @PostMapping(value = "/save",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String saveSearch(
            @RequestParam String name,
            @RequestParam(required = false) String geographicArea,
            @RequestParam(required = false) ItemCondition itemCondition,
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "true") Boolean notificationsEnabled,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            CreateSavedSearchRequest request = new CreateSavedSearchRequest();
            request.setName(name);
            request.setGeographicArea(geographicArea);
            request.setItemCondition(itemCondition);
            request.setNotificationsEnabled(notificationsEnabled);

            // Convertir les mots-clés
            if (keywords != null && !keywords.isBlank()) {
                Set<String> keywordSet = Arrays.stream(keywords.split(","))
                        .map(String::trim)
                        .filter(k -> !k.isEmpty())
                        .collect(Collectors.toSet());
                request.setKeywords(keywordSet);
            }

            SavedSearch savedSearch = savedSearchService.createSavedSearch(request, userDetails.getId());

            redirectAttributes.addFlashAttribute("success",
                    "Recherche '" + savedSearch.getName() + "' sauvegardée avec succès !");

            return "redirect:/users/" + userDetails.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la sauvegarde : " + e.getMessage());
            return "redirect:/search";
        }
    }
}