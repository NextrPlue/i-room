package com.iroom.dashboard.dashboard.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import com.iroom.dashboard.dashboard.entity.Incident;

@RepositoryRestResource(exported = false)
public interface IncidentRepository extends JpaRepository<Incident, Long> {
	@Query("SELECT MAX(i.id) FROM Incident i")
	Long findLatestIncidentId();
}
