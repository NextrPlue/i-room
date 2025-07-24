package com.iroom.user.repository;

import com.iroom.user.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface AdminRepository extends JpaRepository<Admin,Long> {
    boolean existsByEmail(String email);
    Optional<Admin> findByEmail(String email);
}
