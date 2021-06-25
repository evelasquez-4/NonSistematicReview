package com.slr.app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.slr.app.models.Authors;

@RepositoryRestResource
public interface AuthorsRepository extends JpaRepository<Authors, Long>  
{

	@Query(value = "SELECT a.* FROM slr.author_publications ap"
			+ " INNER JOIN slr.authors a ON a.id = ap.author_id"
			+ " WHERE ap.publication_id = :publication_id"
			+ " ORDER BY ap.herarchy ASC", nativeQuery = true)
	public List<Authors> getAuthorsFromPublicationId(
			@Param("publication_id") Long publication_id);
	
	@Query(value = "SELECT * FROM slr.authors WHERE insert_group = :grupo AND publications_updated = :publication_updated"
			+ " LIMIT :limit", nativeQuery = true)
	public List<Authors> getAuthorsByGroupPublicationUpdated(@Param("grupo") int grupo,
			@Param("publication_updated") boolean publication_updated,
			@Param("limit") int limit);
}
