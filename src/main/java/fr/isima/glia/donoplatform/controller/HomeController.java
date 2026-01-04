package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final IAnnouncementService announcementService;

    /**
     * Page d'accueil HTML
     */
    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String homeHtml(Model model) {
        // Récupérer les 8 annonces les plus récentes pour la page d'accueil
        Pageable pageable = PageRequest.of(0, 8, Sort.by("publicationDate").descending());
        List<AnnouncementDto> recentAnnouncements = announcementService.getAllAnnouncements(pageable)
                .stream()
                .map(announcementService::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("recentAnnouncements", recentAnnouncements);

        return "index";
    }
}