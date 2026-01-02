package fr.isima.glia.donoplatform.controller;

import fr.isima.glia.donoplatform.dto.KeywordDto;
import fr.isima.glia.donoplatform.model.Keyword;
import fr.isima.glia.donoplatform.response.ApiResponse;
import fr.isima.glia.donoplatform.service.keyword.IKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final IKeywordService keywordService;

    // ========================================
    // GET ALL KEYWORDS - Version JSON
    // ========================================
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getAllKeywordsJson() {
        List<KeywordDto> keywords = keywordService.getAllKeywords().stream()
                .map(keywordService::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(new ApiResponse("Keywords retrieved", keywords));
    }

    // ========================================
    // GET ALL KEYWORDS - Version HTML
    // ========================================
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String getAllKeywordsHtml(Model model) {
        List<Keyword> keywords = keywordService.getAllKeywords();
        model.addAttribute("keywords", keywords);
        return "keywords/list";
    }

    // ========================================
    // GET KEYWORD BY ID - Version JSON
    // ========================================
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getKeywordByIdJson(@PathVariable Long id) {
        Keyword keyword = keywordService.getKeywordById(id);
        KeywordDto dto = keywordService.convertToDto(keyword);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(new ApiResponse("Keyword found", dto));
    }

    // ========================================
    // GET KEYWORD BY ID - Version HTML
    // ========================================
    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String getKeywordByIdHtml(@PathVariable Long id, Model model) {
        Keyword keyword = keywordService.getKeywordById(id);
        model.addAttribute("keyword", keyword);
        return "keywords/detail";
    }
}