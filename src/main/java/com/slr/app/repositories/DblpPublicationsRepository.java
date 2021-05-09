package com.slr.app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.slr.app.models.DblpPublications;

@RepositoryRestResource
public interface DblpPublicationsRepository extends JpaRepository<DblpPublications, Long>{
	
	@Query(value = "SELECT * FROM slr.dblp_publications "
			+ " WHERE grupo = :grupo AND updated_state = :state AND doc_type = :doc_type "
			+ " ORDER BY id ASC "
			+ " LIMIT :limit", nativeQuery=true)
	public List<DblpPublications> getDblpPublicationsByTypeStateGroup(
			@Param("doc_type") String doc_type, 
			@Param("state") String state, 
			@Param("grupo") int grupo,
			@Param("limit") int limit);
		
}
