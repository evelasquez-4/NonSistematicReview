package com.slr.app.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.DblpPublications;
import com.slr.app.models.Publications;
import com.slr.app.repositories.PublicationsRepository;

@Service
public class PublicationsServices {

	@Autowired
	private PublicationsRepository publication_repository;
	
	public List<Publications> getPublicationsFromAuthorId(Long author_id, String updated_state){
		return this.publication_repository.getPublicationsFromAuthorId(author_id, updated_state);
	}
	
	public Publications getPublicationById(Long id) {
		Optional<Publications> res = this.publication_repository.findById(id);
		
		if(res.isPresent())
			return res.get();
		else
			throw new RuntimeException("Publication id: "+id+" does not exists.");
	}
	
	public Publications getPublicationsByDblpKey(String key) {
		
		Optional<Publications> res = this.publication_repository.getPublicationsByDblpKey(key);
		if(res.isPresent())
			return res.get();
		else
			throw new RuntimeException("Publication key: "+key+" does not exists.");
	}

	public List<Publications> getPublicationsNoAuthors(Long publication_id){
		return this.publication_repository.getPublicationsNoAuthors(publication_id);
	}
	
	public boolean hasAuthors(Long publication_id) {
		return getPublicationsNoAuthors(publication_id).isEmpty();
	}
	
	public Publications savePublications(Publications publications) {
		return this.publication_repository.saveAndFlush(publications);
	}
	
	public List<Publications> savePublicationsList(Iterable<Publications> publications){
		return this.publication_repository.saveAll(publications);
	}
	
	public List<Publications> getPublicationsByTypeState(String doc_type,String state, int limit){
		return this.publication_repository.getPublicationsByTypeState(doc_type, state, limit);
	}
	
	public Publications saveFromDblpPublication(DblpPublications dblp) throws ParseException {
		return new Publications( Long.valueOf(0), 
				"",//abstract_ 
				dblp.getTitle(),//title,
				dblp.getKeyDblp(),//dblpKey
				dblp.getYear(),//year
				dblp.getUrl(), //url
				dblp.getEe(), //ee
				dblp.getNote(), //note
				dblp.getCrossref(), //crossref, 
				new SimpleDateFormat("YYYY-mm-dd").parse(dblp.getMdate()),//mdate
				"1.inserted", //updatedState
				dblp.getDocType(),// docType
				new Date(), //regDate
				null, //bookChapters
				null, //journalEditorials
				null, //conferenceEditorials
				null, //authorPublicationses
				null, //conferencePapers
				null, //books
				null, //journalPapers
				null // publicationKeywordses
				);
			
	}
	
	/*
	 * Funcion que obtiene el parametro de busqueda (doi , isbn), en las apis donde se actualiza una publicacion
	 * { "key" : String, dblp_key,
	 * 	"has_doi" : boolean,
	 * 	"doi" : String, publication doi,
	 * 	"has_isbn" : boolean,
	 * 	"isbn" : String, publication isbn
	 * }
	 */
	public JSONObject getParameterToApiUpdateMendeleySpringer(Publications pub) {
		JSONObject json = new JSONObject();
		String isbn = "";
		
		boolean has_doi = pub.hasDoi();
		boolean has_isbn = false;
		
		String doi = has_doi ? pub.extractDOI() : "";
		
		if(pub.getDocType().equals("book") ) {
			has_isbn = pub.getBooks().getIsbn().isEmpty() ? false : true;
			isbn = has_isbn ? pub.getBooks().getIsbn() : "";
			
		}else if(pub.getDocType().equals("incollection") ) {
			has_isbn = pub.getBookChapters().getIsbn().isEmpty() ? false : true;
			isbn = has_isbn ? pub.getBookChapters().getIsbn() : "";
			
		}else if(pub.getDocType().contentEquals("proceedings") ) {
			switch (pub.getProceedingInfo()) 
			{
				case "incollection":
					has_isbn = pub.getBookChapters().getIsbn().isEmpty() ? false : true;
					isbn = has_isbn ? pub.getBookChapters().getIsbn() : "";
				break;
				case "journal_editorial":
					has_isbn = pub.getJournalEditorials().getIsbn().isEmpty() ? false : true;
					isbn = has_isbn ? pub.getJournalEditorials().getIsbn() : "";
				break;
				case "conference_editorial":
					has_isbn = pub.getConferenceEditorials().getIsbn().isEmpty() ? false : true;
					isbn = has_isbn ? pub.getConferenceEditorials().getIsbn() : "";
				break;
			}
		}
		
		return json.put("key", pub.getDblpKey())
				.put("has_doi", has_doi)
				.put("doi", doi)
				.put("has_isbn", has_isbn)
				.put("isbn", isbn);
		
	}
	
}
