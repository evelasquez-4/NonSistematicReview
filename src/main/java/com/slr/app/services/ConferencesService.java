package com.slr.app.services;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.Conferences;
import com.slr.app.repositories.ConferencesRepository;


@Service
public class ConferencesService {
	
	@Autowired
	private ConferencesRepository conference;
	
	public Conferences save(Conferences conference)
	{
		return this.conference.saveAndFlush(conference);
	}
	
	public Optional<Conferences> findById(Long id)
	{
		return this.conference.findById(id);
	}
	
	public Optional<Conferences> findByDescription(String description)
	{
		return this.conference.findByDescription(description.toLowerCase());
	}
	
	public Conferences obtainConferenceByDescription(String description,String title)
	{
		Optional<Conferences> response = null;
		
		if(description.equals("") || Objects.isNull(description))
			response = findById(Long.valueOf(0));
		else 
		{
			response = findByDescription(description);
			
			if(!response.isPresent()) {
				return save( new Conferences(0, description, title, new Date(), null) );
			}
				
		}
		
		return response.get();
	}

}
