package com.slr.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.slr.app.models.Conferences;



@RepositoryRestResource
public interface ConferencesRepository extends JpaRepository<Conferences, Long> {

	@Query("FROM Conferences WHERE id = ?1")
	public Optional<Conferences> findById(Long id);
	
	@Query("FROM Conferences WHERE LOWER(description) = ?1")
	public Optional<Conferences> findByDescription(String description);
}
