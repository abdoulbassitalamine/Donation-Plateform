package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.KeywordDto;
import fr.isima.glia.donoplatform.model.Keyword;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.service.keyword.IKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final IKeywordService keywordService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllKeywords() {
        List<KeywordDto> keywords = keywordService.getAllKeywords().stream()
                .map(keyword -> {
                    KeywordDto dto = new KeywordDto();
                    dto.setId(keyword.getId());
                    dto.setName(keyword.getName());
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse("Keywords retrieved", keywords));
    }

    @GetMapping("/{keywordId}")
    public ResponseEntity<ApiResponse> getKeywordById(@PathVariable Long keywordId) {
        Keyword keyword = keywordService.getKeywordById(keywordId);
        KeywordDto dto = new KeywordDto();
        dto.setId(keyword.getId());
        dto.setName(keyword.getName());
        return ResponseEntity.ok(new ApiResponse("Keyword found", dto));
    }
}
