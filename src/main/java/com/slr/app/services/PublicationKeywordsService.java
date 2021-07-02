package com.slr.app.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.Keywords;
import com.slr.app.models.PublicationKeywords;
import com.slr.app.models.Publications;
import com.slr.app.repositories.PublicationKeywordsRepository;

@Service
public class PublicationKeywordsService {

	@Autowired
	private PublicationKeywordsRepository publication_keyword;
	@Autowired
	private KeywordsService keyword_service;
	
	public List<PublicationKeywords> findByPublicationKeywordId(long keyword_id, long publication_id)
	{
		return this.publication_keyword.findByPublicationKeywordId(Long.valueOf(keyword_id).intValue()
				,Long.valueOf(publication_id).intValue());
	}
	
	public PublicationKeywords savePublicationKeyword(Publications publication,Keywords keyword)
	{
		PublicationKeywords pk = new PublicationKeywords();
		pk.setKeywords(keyword);
		pk.setPublications(publication);
		pk.setCreatedAt(new Date());
		
		return publication_keyword.save(pk);
	}
	
	public List<PublicationKeywords> findByPublicationId(long publication_id){
		return this.publication_keyword.findByPublicationId(Long.valueOf(publication_id).intValue());
	}
	
	public List<PublicationKeywords> registerPublicationsKeywords(List<String> keywords,Publications publication)
	{
		List<PublicationKeywords> response = new ArrayList<>();
		
		for (String key : keywords) {
			
			Keywords keyword = this.keyword_service.save(	new Keywords(0, key, new Date(), null)	);
				
			response.add( this.publication_keyword.save( 
					new PublicationKeywords(0, keyword, publication, new Date())
					) );
		}
		
		return response;
	}
}
