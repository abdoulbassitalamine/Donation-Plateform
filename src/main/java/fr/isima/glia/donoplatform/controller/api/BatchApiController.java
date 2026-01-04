package fr.isima.glia.donoplatform.controller.api;

import fr.isima.glia.donoplatform.dto.BatchDto;
import fr.isima.glia.donoplatform.model.Batch;
import fr.isima.glia.donoplatform.request.CreateBatchRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.security.user.DonoUserDetails;
import fr.isima.glia.donoplatform.service.batch.IBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JSON API for batch operations
 */
@RestController
@RequestMapping("/batches")
@RequiredArgsConstructor
public class BatchApiController {

    private final IBatchService batchService;

    // Create batch
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> createBatch(
            @Valid @RequestBody CreateBatchRequest request,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        Batch batch = batchService.createBatch(request, userDetails.getId());
        BatchDto dto = batchService.convertToDto(batch);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Batch created successfully", dto));
    }

    // Get batch by ID
    @GetMapping(value = "/{batchId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getBatchById(
            @PathVariable Long batchId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        Batch batch = batchService.getBatchById(batchId);
        BatchDto dto = batchService.convertToDto(batch);

        String etag = "\"" + Integer.toHexString(batch.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                .body(new ApiResponse("Batch found", dto));
    }

    // Get batches by user
    @GetMapping(value = "/user/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getBatchesByUser(
            @PathVariable Long userId,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        List<Batch> batches = batchService.getBatchesByUser(userId);
        List<BatchDto> dtos = batches.stream()
                .map(batchService::convertToDto)
                .collect(Collectors.toList());

        String etag = "\"" + Integer.toHexString(dtos.hashCode()) + "\"";

        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(3, TimeUnit.MINUTES))
                .body(new ApiResponse("Batches retrieved", dtos));
    }

    // Delete batch
    @DeleteMapping(value = "/{batchId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> deleteBatch(
            @PathVariable Long batchId,
            @AuthenticationPrincipal DonoUserDetails userDetails) {

        batchService.deleteBatch(batchId, userDetails.getId());
        return ResponseEntity.ok(new ApiResponse("Batch deleted successfully", null));
    }
}