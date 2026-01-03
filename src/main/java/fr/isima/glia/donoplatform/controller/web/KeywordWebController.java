package fr.isima.glia.donoplatform.controller.web;

import fr.isima.glia.donoplatform.model.Keyword;
import fr.isima.glia.donoplatform.service.keyword.IKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTML views for keyword operations
 */
@Controller
@RequestMapping("/keywords")
@RequiredArgsConstructor
public class KeywordWebController {

    private final IKeywordService keywordService;

    // List all keywords
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String getAllKeywords(Model model) {
        List<Keyword> keywords = keywordService.getAllKeywords();
        model.addAttribute("keywords", keywords);
        return "keywords/list";
    }

    // View keyword detail
    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String getKeywordById(@PathVariable Long id, Model model) {
        Keyword keyword = keywordService.getKeywordById(id);
        model.addAttribute("keyword", keyword);
        return "keywords/detail";
    }
}