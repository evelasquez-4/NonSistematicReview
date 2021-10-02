package com.slr.app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.slr.app.models.AuthorPublications;

@RepositoryRestResource
public interface AuthorPublicationsRepository extends JpaRepository<AuthorPublications, Long> {

	@Query(value = "SELECT * FROM slr.author_publications "
			+ " WHERE author_id = :id", nativeQuery = true)
	public List<AuthorPublications> findAuthorPublicationsByAuthorId(@Param("id") Long id);
	
	@Query("FROM AuthorPublications WHERE publication_id = :publication_id ORDER BY herarchy ASC")
	public List<AuthorPublications> findAuthorPublicationsByPublicationId(
			@Param("publication_id") Long publication_id);	
	
	
}
