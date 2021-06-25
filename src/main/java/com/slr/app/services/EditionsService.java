package com.slr.app.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.Editions;
import com.slr.app.repositories.EditionsRepository;

@Service
public class EditionsService {

	@Autowired
	private EditionsRepository edition;
	
	public Optional<Editions> findById(Long id)
	{
		return edition.findById(id);
	}
	
	public Editions save(Editions e){
		return edition.saveAndFlush(e);
	}
	
}
