package fr.isima.glia.donoplatform.service.savedsearch;

import fr.isima.glia.donoplatform.dto.SavedSearchDto;
import fr.isima.glia.donoplatform.model.SavedSearch;
import fr.isima.glia.donoplatform.request.CreateSavedSearchRequest;
import fr.isima.glia.donoplatform.request.UpdateSavedSearchRequest;

import java.util.List;

public interface ISavedSearchService {
    SavedSearch createSavedSearch(CreateSavedSearchRequest request, Long userId);
    SavedSearch getSavedSearchById(Long id);
    SavedSearchDto convertToDto(SavedSearch savedSearch);
    SavedSearch updateSavedSearch(Long id, UpdateSavedSearchRequest request, Long userId);
    void deleteSavedSearch(Long id, Long userId);
    List<SavedSearch> getSavedSearchesByUser(Long userId);
}
