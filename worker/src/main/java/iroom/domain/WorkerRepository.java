package iroom.domain;

import iroom.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "workers", path = "workers")
public interface WorkerRepository
    extends PagingAndSortingRepository<Worker, Long> {}
