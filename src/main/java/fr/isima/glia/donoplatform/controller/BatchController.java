package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.AnnouncementDto;
import fr.isima.glia.donoplatform.dto.BatchDto;
import fr.isima.glia.donoplatform.model.Batch;
import fr.isima.glia.donoplatform.request.CreateBatchRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
import fr.isima.glia.donoplatform.service.batch.IBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/batches")
@RequiredArgsConstructor
public class BatchController {

    private final IBatchService batchService;
    private final IAnnouncementService announcementService;

    // ============================================================
    // CREATE BATCH - JSON
    // ============================================================
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> createBatchJson(
            @Valid @RequestBody CreateBatchRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Batch batch = batchService.createBatch(request, userDetails.getId());
        BatchDto dto = batchService.convertToDto(batch);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Batch created successfully", dto));
    }

    // ============================================================
    // GET BATCH BY ID - JSON (avec cache)
    // ============================================================
    @GetMapping(value = "/{batchId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getBatchByIdJson(
            @PathVariable Long batchId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Batch batch = batchService.getBatchById(batchId);
        BatchDto dto = batchService.convertToDto(batch);

        // Générer ETag
        String etag = "\"" + Integer.toHexString(batch.hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(new ApiResponse("Batch found", dto));
    }

    // ============================================================
    // GET BATCH BY ID - HTML
    // ============================================================
    @GetMapping(value = "/{batchId}",
            produces = MediaType.TEXT_HTML_VALUE)
    public String viewBatchHtml(
            @PathVariable Long batchId,
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        BatchDto batch = batchService.convertToDto(batchService.getBatchById(batchId));

        model.addAttribute("batch", batch);
        model.addAttribute("userId", userDetails.getId());

        return "batches/detail";
    }

    // ============================================================
    // GET BATCHES BY USER - JSON (avec cache)
    // ============================================================
    @GetMapping(value = "/user/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getBatchesByUserJson(
            @PathVariable Long userId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<Batch> batches = batchService.getBatchesByUser(userId);
        List<BatchDto> dtos = batches.stream()
                .map(batchService::convertToDto)
                .collect(Collectors.toList());

        // Générer ETag
        String etag = "\"" + Integer.toHexString(dtos.hashCode()) + "\"";

        // Vérifier cache
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(3, TimeUnit.MINUTES))
                .body(new ApiResponse("Batches retrieved", dtos));
    }

    // ============================================================
    // LIST USER BATCHES - HTML
    // ============================================================
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String listBatchesHtml(
            @AuthenticationPrincipal DonoUserDetails userDetails,
            Model model) {

        List<BatchDto> batches = batchService.getBatchesByUser(userDetails.getId())
                .stream()
                .map(batchService::convertToDto)
                .collect(Collectors.toList());

        // Récupérer les annonces de l'utilisateur pour le modal de création
        List<AnnouncementDto> userAnnouncements = announcementService.getAnnouncementsByUser(userDetails.getId())
                .stream()
                .map(announcementService::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("batches", batches);
        model.addAttribute("userAnnouncements", userAnnouncements);
        model.addAttribute("userId", userDetails.getId());

        return "batches/list";
    }

    // ============================================================
    // CREATE BATCH - HTML (via formulaire)
    // ============================================================
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String createBatchHtml(
            @RequestParam List<Long> announcementIds,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        CreateBatchRequest request = new CreateBatchRequest();
        request.setAnnouncementIds(announcementIds);

        batchService.createBatch(request, userDetails.getId());

        return "redirect:/batches";
    }

    // ============================================================
    // DELETE BATCH - JSON
    // ============================================================
    @DeleteMapping(value = "/{batchId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> deleteBatchJson(
            @PathVariable Long batchId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        batchService.deleteBatch(batchId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Batch deleted successfully", null));
    }

    // ============================================================
    // DELETE BATCH - HTML
    // ============================================================
    @PostMapping(value = "/{batchId}/delete",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public String deleteBatchHtml(
            @PathVariable Long batchId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        batchService.deleteBatch(batchId, userDetails.getId());
        return "redirect:/batches";
    }
}