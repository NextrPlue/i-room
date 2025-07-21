package iroom.infra;

import iroom.domain.*;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(
    collectionResourceRel = "readAlarms",
    path = "readAlarms"
)
public interface ReadAlarmRepository
    extends PagingAndSortingRepository<ReadAlarm, Long> {}
