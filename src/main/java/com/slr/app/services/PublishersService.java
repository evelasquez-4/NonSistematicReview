package com.slr.app.services;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.Publishers;
import com.slr.app.repositories.PublishersRepository;

@Service
public class PublishersService 
{
	@Autowired
	private PublishersRepository publisher;
	
	public List<Publishers> findAll() {
		return this.publisher.findAll();
	}
	
	public Optional<Publishers> findById(Long id) {
		return this.publisher.findById(id);
	}
	
	public Publishers save(Publishers publisher) {
		return this.publisher.saveAndFlush(publisher);
	}
	
	public Publishers registerPublisher(String desc) {
		if (Objects.isNull(desc) || desc.isEmpty())
			return null;
		
		return save(new Publishers(Long.valueOf(0), desc, "active", new Date(), null, null, null));
	}
	
}
