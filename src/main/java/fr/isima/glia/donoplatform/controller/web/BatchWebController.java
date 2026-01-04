package fr.isima.glia.donoplatform.controller.web;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.dto.BatchDto;
import fr.isima.glia.donoplatform.request.CreateBatchRequest;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import fr.isima.glia.donoplatform.service.batch.IBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * HTML views for batch operations
 */
@Controller
@RequestMapping("/batches")
@RequiredArgsConstructor
public class BatchWebController {

    private final IBatchService batchService;
    private final IAnnouncementService announcementService;

    // List user batches
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String listBatches(
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        List<BatchDto> batches = batchService.getBatchesByUser(userDetails.getId())
                .stream()
                .map(batchService::convertToDto)
                .collect(Collectors.toList());

        List<AnnouncementDto> userAnnouncements = announcementService.getAnnouncementsByUser(userDetails.getId())
                .stream()
                .map(announcementService::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("batches", batches);
        model.addAttribute("userAnnouncements", userAnnouncements);
        model.addAttribute("userId", userDetails.getId());

        return "batches/list";
    }

    // View batch detail
    @GetMapping(value = "/{batchId}",
            produces = MediaType.TEXT_HTML_VALUE)
    public String viewBatch(
            @PathVariable Long batchId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        BatchDto batch = batchService.convertToDto(batchService.getBatchById(batchId));

        model.addAttribute("batch", batch);
        model.addAttribute("userId", userDetails.getId());

        return "batches/detail";
    }

    // Create batch via form
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String createBatch(
            @RequestParam List<Long> announcementIds,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        CreateBatchRequest request = new CreateBatchRequest();
        request.setAnnouncementIds(announcementIds);

        batchService.createBatch(request, userDetails.getId());

        return "redirect:/batches";
    }

    // Delete batch via form
    @DeleteMapping(value = "/{batchId}",
            produces = MediaType.TEXT_HTML_VALUE)
    public String deleteBatch(
            @PathVariable Long batchId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        batchService.deleteBatch(batchId, userDetails.getId());
        return "redirect:/batches";
    }
}