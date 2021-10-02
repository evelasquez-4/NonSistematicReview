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
	
	
	//query que actualiza la afiliacion de los autores registrados en slr.author_publications
	@Query(value = "SELECT  DISTINCT au.* "
			+"FROM slr.authors au " 
			+"LEFT JOIN slr.author_publications ap ON ap.author_id = au.id" 
			+" WHERE ap.author_id IS NOT NULL AND au.publications_updated = :publication_updated " 
			+" AND au.insert_group = :grupo" 
			+" LIMIT :limit ", nativeQuery = true)
	public List<Authors> getAuthorsToUpdateAffiliation(
			@Param("publication_updated") boolean publication_updated,
			@Param("grupo") int grupo,
			@Param("limit") int limit );
}
