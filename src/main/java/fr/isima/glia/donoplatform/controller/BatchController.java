package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.BatchDto;
import fr.isima.glia.donoplatform.model.Batch;
import fr.isima.glia.donoplatform.request.CreateBatchRequest;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.service.batch.IBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/batches")
@RequiredArgsConstructor
public class BatchController {

    private final IBatchService batchService;

    @PostMapping
    public ResponseEntity<ApiResponse> createBatch(
            @Valid @RequestBody CreateBatchRequest request,
            @RequestParam Long userId) {
        Batch batch = batchService.createBatch(request, userId);
        BatchDto dto = batchService.convertToDto(batch);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse("Batch created successfully", dto));
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<ApiResponse> getBatchById(@PathVariable Long batchId) {
        Batch batch = batchService.getBatchById(batchId);
        BatchDto dto = batchService.convertToDto(batch);
        return ResponseEntity.ok(new ApiResponse("Batch found", dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getBatchesByUser(@PathVariable Long userId) {
        List<BatchDto> batches = batchService.getBatchesByUser(userId).stream()
                .map(batchService::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse("Batches retrieved", batches));
    }

    @DeleteMapping("/{batchId}")
    public ResponseEntity<ApiResponse> deleteBatch(
            @PathVariable Long batchId,
            @RequestParam Long userId) {
        batchService.deleteBatch(batchId, userId);
        return ResponseEntity.ok(new ApiResponse("Batch deleted successfully", null));
    }
}
