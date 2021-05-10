package com.slr.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.slr.app.models.Editions;

@RepositoryRestResource
public interface EditionsRepository extends JpaRepository<Editions, Long> {
	
	@Query("FROM Editions WHERE LOWER(description) = ?1")
	public Optional<Editions> findByDescription(String description);
	
	@Query("FROM Editions WHERE LOWER(volume) = ?1 AND number = ?2 ")
	public Optional<Editions> findByVolumeNumber(String volume, String number);
	
	@Query(value = "SELECT * "
			+ "FROM slr.editions WHERE LOWER(volume) = :volume AND "
			+ "LOWER(number) = :number AND publisher_id = :publisher ORDER BY id ASC LIMIT 1"
			,nativeQuery = true)
	public Optional<Editions> findByVolumeNumberPublisher(
			@Param("volume") String volume,
			@Param("number") String number,
			@Param("publisher") int publisher);
}
