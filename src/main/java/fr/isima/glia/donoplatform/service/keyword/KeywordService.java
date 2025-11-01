package fr.isima.glia.donoplatform.service.keyword;

import fr.isima.glia.donoplatform.dto.KeywordDto;
import fr.isima.glia.donoplatform.exceptions.ResourceNotFoundException;
import fr.isima.glia.donoplatform.model.Keyword;
import fr.isima.glia.donoplatform.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordService implements IKeywordService {

    private final KeywordRepository keywordRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Keyword getOrCreateKeyword(String name) {
        return keywordRepository.findByName(name.toLowerCase())
                .orElseGet(() -> {
                    Keyword keyword = Keyword.builder()
                            .name(name.toLowerCase())
                            .build();
                    return keywordRepository.save(keyword);
                });
    }

    @Override
    public Keyword getKeywordById(Long id) {
        return keywordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Keyword not found with id: " + id));
    }

    @Override
    public List<Keyword> getAllKeywords() {
        return keywordRepository.findAll();
    }

    @Override
    public KeywordDto convertToDto(Keyword keyword) {
        return modelMapper.map(keyword, KeywordDto.class);
    }
}
