package fr.isima.glia.donoplatform.service.batch;

import fr.isima.glia.donoplatform.dto.BatchDto;
import fr.isima.glia.donoplatform.exceptions.ResourceNotFoundException;
import fr.isima.glia.donoplatform.exceptions.UnauthorizedAccessException;
import fr.isima.glia.donoplatform.model.Announcement;
import fr.isima.glia.donoplatform.model.Batch;
import fr.isima.glia.donoplatform.model.User;
import fr.isima.glia.donoplatform.repository.BatchRepository;
import fr.isima.glia.donoplatform.request.CreateBatchRequest;
import fr.isima.glia.donoplatform.service.announcement.IAnnouncementService;
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
public class BatchService implements IBatchService {

    private final BatchRepository batchRepository;
    private final IUserService userService;
    private final IAnnouncementService announcementService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Batch createBatch(CreateBatchRequest request, Long userId) {
        User owner = userService.getUserById(userId);

        Set<Announcement> announcements = request.getAnnouncementIds().stream()
                .map(announcementService::getAnnouncementById)
                .peek(announcement -> {
                    if (!announcement.getOwner().getId().equals(userId)) {
                        throw new UnauthorizedAccessException("You can only batch your own announcements");
                    }
                })
                .collect(Collectors.toSet());

        Batch batch = Batch.builder()
                .owner(owner)
                .announcements(announcements)
                .build();

        return batchRepository.save(batch);
    }

    @Override
    public Batch getBatchById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found with id: " + id));
    }

    @Override
    public BatchDto convertToDto(Batch batch) {
        return modelMapper.map(batch, BatchDto.class);
    }

    @Override
    @Transactional
    public void deleteBatch(Long id, Long userId) {
        Batch batch = getBatchById(id);

        if (!batch.getOwner().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this batch");
        }

        batchRepository.delete(batch);
    }

    @Override
    public List<Batch> getBatchesByUser(Long userId) {
        User user = userService.getUserById(userId);
        return batchRepository.findByOwner(user);
    }
}
