package com.slr.app.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.DblpPublications;
import com.slr.app.models.Editions;
import com.slr.app.models.Journals;
import com.slr.app.models.Publishers;
import com.slr.app.repositories.EditionsRepository;

@Service
public class EditionsService {

	@Autowired
	private EditionsRepository edition;
	@Autowired
	private JournalsService journal_service;
	
	public Optional<Editions> findById(Long id)
	{
		return edition.findById(id);
	}
	
	public Editions save(Editions e){
		return edition.saveAndFlush(e);
	}
	
	public Editions saveEditionFromDblp(DblpPublications dblp, Publishers publisher) {
		
		Journals journal = dblp.getJournal().isEmpty() ?
				null : this.journal_service.save( new Journals(0, dblp.getJournal(), 
						"", new Date(), null)
						);
		return null;
	}
}
