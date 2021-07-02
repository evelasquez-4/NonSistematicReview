package com.slr.app.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.TmpApis;
import com.slr.app.repositories.TmpApisRepository;

@Service
public class TmpApisService {

	@Autowired
	private TmpApisRepository repository;
	
	
	public TmpApis save(TmpApis tmp) {
		return this.repository.saveAndFlush(tmp);
	}
	
	public TmpApis findById(Long id) {
		Optional<TmpApis> res =  this.repository.findById(id);
		if(!res.isPresent())
			throw new RuntimeException("Authors id "+ id +" does not exists");
		return res.get();
	}
}
