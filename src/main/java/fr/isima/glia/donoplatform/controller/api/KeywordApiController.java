package fr.isima.glia.donoplatform.controller.api;

import fr.isima.glia.donoplatform.dto.KeywordDto;
import fr.isima.glia.donoplatform.model.Keyword;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.service.keyword.IKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * JSON API for keyword operations
 */
@RestController
@RequestMapping("/keywords")
@RequiredArgsConstructor
public class KeywordApiController {

    private final IKeywordService keywordService;

    // Get all keywords
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getAllKeywords() {
        List<KeywordDto> keywords = keywordService.getAllKeywords().stream()
                .map(keywordService::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(new ApiResponse("Keywords retrieved", keywords));
    }

    // Get keyword by ID
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getKeywordById(@PathVariable Long id) {
        Keyword keyword = keywordService.getKeywordById(id);
        KeywordDto dto = keywordService.convertToDto(keyword);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(new ApiResponse("Keyword found", dto));
    }
}