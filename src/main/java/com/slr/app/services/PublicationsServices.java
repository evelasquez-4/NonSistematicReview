package com.slr.app.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.DblpPublications;
import com.slr.app.models.Publications;
import com.slr.app.repositories.PublicationsRepository;

@Service
public class PublicationsServices {

	@Autowired
	private PublicationsRepository publication_service;
	
	
	public Publications getPublicationById(Long id) {
		Optional<Publications> res = this.publication_service.findById(id);
		
		if(res.isPresent())
			return res.get();
		else
			throw new RuntimeException("Publication id: "+id+" does not exists.");
	}
	
	public Publications savePublications(Publications publications) {
		return this.publication_service.saveAndFlush(publications);
	}
	
	public List<Publications> savePublicationsList(Iterable<Publications> publications){
		return this.publication_service.saveAll(publications);
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
}
