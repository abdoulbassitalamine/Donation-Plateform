package fr.isima.glia.donoplatform.service.keyword;

import fr.isima.glia.donoplatform.dto.ImageDto;
import fr.isima.glia.donoplatform.dto.KeywordDto;
import fr.isima.glia.donoplatform.model.Image;
import fr.isima.glia.donoplatform.model.Keyword;

import java.util.List;

public interface IKeywordService {
    Keyword getOrCreateKeyword(String name);
    Keyword getKeywordById(Long id);
    List<Keyword> getAllKeywords();
    KeywordDto convertToDto(Keyword keyword);
}
