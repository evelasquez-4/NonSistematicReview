package com.slr.app.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.SlrConfiguration;
import com.slr.app.repositories.SlrConfigurationRepository;

@Service
public class SlrConfigurationService {
	
	@Autowired
	private SlrConfigurationRepository configuration_repo;
	
	public SlrConfiguration getValidateConfiguration(String state){
		//return this.configuration_repo.getValidateConfiguration(state);
		
		List<SlrConfiguration> confs = this.configuration_repo.getValidateConfiguration(state);
		
		if(confs.size() > 1 || confs.isEmpty())
			throw new RuntimeException("Verified validate configurations");
		else
			return confs.get(0);
	}
	
	public SlrConfiguration getConfigurationById(Long id){
		Optional<SlrConfiguration> res = this.configuration_repo.findById(id);
		if(res.isPresent()) 
			return res.get();
		else	
			throw new RuntimeException("Configuration id :"+id+" does not exists.");
	}
	
	public SlrConfiguration saveConfiguration(SlrConfiguration configuration) {
		return this.configuration_repo.saveAndFlush(configuration);	
	}
	
	public SlrConfiguration updateConfiguration(SlrConfiguration configuration) {
		return this.configuration_repo.save(configuration);
	}
	
	public void deleteConfiguration(Long id) {
		this.configuration_repo.deleteById(id);
	}
	
}
