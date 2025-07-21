package iroom.infra;

import iroom.domain.*;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(
    collectionResourceRel = "workerReadModels",
    path = "workerReadModels"
)
public interface WorkerReadModelRepository
    extends PagingAndSortingRepository<WorkerReadModel, Long> {}
