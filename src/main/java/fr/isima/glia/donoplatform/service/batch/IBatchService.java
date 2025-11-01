package fr.isima.glia.donoplatform.service.batch;

import fr.isima.glia.donoplatform.dto.BatchDto;
import fr.isima.glia.donoplatform.model.Batch;
import fr.isima.glia.donoplatform.request.CreateBatchRequest;

import java.util.List;

public interface IBatchService {
    Batch createBatch(CreateBatchRequest request, Long userId);
    Batch getBatchById(Long id);
    BatchDto convertToDto(Batch batch);
    void deleteBatch(Long id, Long userId);
    List<Batch> getBatchesByUser(Long userId);
}
