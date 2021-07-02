package com.slr.app.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.slr.app.models.Publications;

@RepositoryRestResource
public interface PublicationsRepository extends JpaRepository<Publications, Long>{

	@Query(value = "SELECT * FROM slr.publications"
			+ " WHERE updated_state = :state  AND doc_type = :doc_type"
			+ " ORDER BY id ASC"
			+ " LIMIT :limit", nativeQuery = true)
	public List<Publications> getPublicationsByTypeState(
			@Param("doc_type") String doc_type, 
			@Param("state") String state, 
			@Param("limit") int limit);
	
	@Query("FROM Publications WHERE dblp_key = ?1")
	public Optional<Publications> getPublicationsByDblpKey(String key);
	
	
	
	@Query(value = "SELECT p.* FROM slr.author_publications ap"
			+ " JOIN slr.publications p ON p.id = ap.publication_id"
			+ " WHERE ap.author_id = :author_id AND p.updated_state = :updated_state"
			+ " ORDER BY p.id ASC ", nativeQuery = true)
	public List<Publications> getPublicationsFromAuthorId(
			@Param("author_id")Long author_id,
			@Param("updated_state")String updated_state);
	
	
	@Query(value = "SELECT p.* FROM slr.publications p "
			+ " INNER JOIN slr.author_publications ap ON ap.publication_id = p.id"
			+ " WHERE ap.publication_id = :publication_id  "
			+ " AND (ap.author_id::::CHARACTER VARYING <> '' OR ap.author_id IS NOT NULL)", nativeQuery = true)
	public List<Publications> getPublicationsNoAuthors(
			@Param("publication_id") Long publication_id);
}
