package iroom.domain;

import iroom.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "admins", path = "admins")
public interface AdminRepository
    extends PagingAndSortingRepository<Admin, Long> {}
