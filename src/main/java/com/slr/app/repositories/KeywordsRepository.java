package com.slr.app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.slr.app.models.Keywords;

@RepositoryRestResource
public interface KeywordsRepository extends JpaRepository<Keywords, Long>{
	
	@Query(value = " SELECT k.* " + 
			" FROM slr.publication_keywords pk " + 
			" JOIN slr.keywords k on k.id = pk.keyword_id "
			+" WHERE pk.publication_id = :publication_id",nativeQuery = true)
	public List<Keywords> getKeywordsFromPublicationId(@Param("publication_id") Long publication_id);

}
