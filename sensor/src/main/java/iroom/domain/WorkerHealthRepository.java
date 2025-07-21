package iroom.domain;

import iroom.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(
    collectionResourceRel = "workerHealths",
    path = "workerHealths"
)
public interface WorkerHealthRepository
    extends PagingAndSortingRepository<WorkerHealth, Long> {}
