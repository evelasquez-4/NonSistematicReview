package com.slr.app.services;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.Journals;

import com.slr.app.repositories.JournalsRepository;


@Service
public class JournalsService {

	@Autowired
	private JournalsRepository journal;
	
	public Journals save(Journals journal)
	{
		return this.journal.saveAndFlush(journal);
	}
	
	public Optional<Journals> findById(Long id)
	{
		return this.journal.findById(id);
	}
	
	public Optional<Journals> findByDescription(String description)
	{
		return this.journal.findByDescription(description.toLowerCase());
	}
	
	public Journals obtainJournalByDescription(String description) {
		Optional<Journals> response = null;
		
		if(description.equals("") || Objects.isNull(description))
			response = findById(Long.valueOf(0));
		else {
			response = this.journal.findByDescription(description);
			
			if(!response.isPresent()) {
				return save(
						new Journals(0, description, "", new Date(), null)
					);
			}
		}
		
		return response.get();
	}
}
