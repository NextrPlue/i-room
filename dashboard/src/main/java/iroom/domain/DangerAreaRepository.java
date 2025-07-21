package iroom.domain;

import iroom.domain.*;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(
    collectionResourceRel = "dangerAreas",
    path = "dangerAreas"
)
public interface DangerAreaRepository
    extends PagingAndSortingRepository<DangerArea, Long> {}
