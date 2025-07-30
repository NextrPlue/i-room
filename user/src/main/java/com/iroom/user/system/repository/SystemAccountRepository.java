package com.iroom.user.system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import com.iroom.user.system.entity.SystemAccount;

import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface SystemAccountRepository extends JpaRepository<SystemAccount, Long> {
    Optional<SystemAccount> findByApiKey(String apiKey);
    Boolean existsByName(String name);
}
