package com.slr.app.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.Keywords;
import com.slr.app.repositories.KeywordsRepository;

@Service
public class KeywordsService {

	@Autowired
	private KeywordsRepository keyword_repository;
	
	public Keywords findById(Long id) {
		Optional<Keywords> response =  this.keyword_repository.findById(id);
		if(!response.isPresent())
			throw new RuntimeException("Keywords id "+ id +" does not exists");
		return response.get();
	}
	
	public List<Keywords> getKeywordsFromPublicationId(Long publication_id){
		return this.keyword_repository.getKeywordsFromPublicationId(publication_id);
	}
	
	public boolean publicationHasKeywords(Long publication_id) {
		return getKeywordsFromPublicationId(publication_id).isEmpty();
	}
	
	public Keywords save(Keywords keywords) {
		return this.keyword_repository.save(keywords);
	}
}
