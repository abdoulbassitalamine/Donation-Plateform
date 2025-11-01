package fr.isima.glia.donoplatform.service.savedsearch;

import fr.isima.glia.donoplatform.dto.KeywordDto;
import fr.isima.glia.donoplatform.dto.SavedSearchDto;
import fr.isima.glia.donoplatform.exceptions.ResourceNotFoundException;
import fr.isima.glia.donoplatform.exceptions.UnauthorizedAccessException;
import fr.isima.glia.donoplatform.model.Keyword;
import fr.isima.glia.donoplatform.model.SavedSearch;
import fr.isima.glia.donoplatform.model.User;
import fr.isima.glia.donoplatform.repository.SavedSearchRepository;
import fr.isima.glia.donoplatform.request.CreateSavedSearchRequest;
import fr.isima.glia.donoplatform.request.UpdateSavedSearchRequest;
import fr.isima.glia.donoplatform.service.keyword.IKeywordService;
import fr.isima.glia.donoplatform.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavedSearchService implements ISavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final IUserService userService;
    private final IKeywordService keywordService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public SavedSearch createSavedSearch(CreateSavedSearchRequest request, Long userId) {
        User user = userService.getUserById(userId);

        Set<Keyword> keywords = new HashSet<>();
        if (request.getKeywords() != null && !request.getKeywords().isEmpty()) {
            keywords = request.getKeywords().stream()
                    .map(keywordService::getOrCreateKeyword)
                    .collect(Collectors.toSet());
        }

        SavedSearch savedSearch = SavedSearch.builder()
                .user(user)
                .name(request.getName())
                .geographicArea(request.getGeographicArea())
                .keywords(keywords)
                .itemCondition(request.getItemCondition())
                .notificationsEnabled(request.getNotificationsEnabled() != null ? request.getNotificationsEnabled() : true)
                .build();

        return savedSearchRepository.save(savedSearch);
    }

    @Override
    public SavedSearch getSavedSearchById(Long id) {
        return savedSearchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Saved search not found with id: " + id));
    }

    @Override
    public SavedSearchDto convertToDto(SavedSearch savedSearch) {


        return modelMapper.map(savedSearch, SavedSearchDto.class);
    }

    @Override
    @Transactional
    public SavedSearch updateSavedSearch(Long id, UpdateSavedSearchRequest request, Long userId) {
        SavedSearch savedSearch = getSavedSearchById(id);

        if (!savedSearch.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this saved search");
        }

        if (request.getName() != null) {
            savedSearch.setName(request.getName());
        }
        if (request.getGeographicArea() != null) {
            savedSearch.setGeographicArea(request.getGeographicArea());
        }
        if (request.getItemCondition() != null) {
            savedSearch.setItemCondition(request.getItemCondition());
        }
        if (request.getNotificationsEnabled() != null) {
            savedSearch.setNotificationsEnabled(request.getNotificationsEnabled());
        }
        if (request.getKeywords() != null) {
            Set<Keyword> keywords = request.getKeywords().stream()
                    .map(keywordService::getOrCreateKeyword)
                    .collect(Collectors.toSet());
            savedSearch.setKeywords(keywords);
        }

        return savedSearchRepository.save(savedSearch);
    }

    @Override
    @Transactional
    public void deleteSavedSearch(Long id, Long userId) {
        SavedSearch savedSearch = getSavedSearchById(id);

        if (!savedSearch.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this saved search");
        }

        savedSearchRepository.delete(savedSearch);
    }

    @Override
    public List<SavedSearch> getSavedSearchesByUser(Long userId) {
        User user = userService.getUserById(userId);
        return savedSearchRepository.findByUser(user);
    }
}
