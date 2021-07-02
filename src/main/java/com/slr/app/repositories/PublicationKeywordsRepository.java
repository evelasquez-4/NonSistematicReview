package com.slr.app.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.slr.app.models.PublicationKeywords;

@RepositoryRestResource
public interface PublicationKeywordsRepository extends JpaRepository<PublicationKeywords,Long>
{
	
	@Query(value = "SELECT pk.* FROM slr.publication_keywords pk WHERE pk.publication_id = ?1", nativeQuery = true)
	public List<PublicationKeywords> findByPublicationId(Integer pub_id);
	
	@Query(value="SELECT * FROM slr.publication_keywords WHERE keyword_id = ?1 AND publication_id = ?2", nativeQuery = true)
	public List<PublicationKeywords> findByPublicationKeywordId(int keyword_id, int publication_id);
}
