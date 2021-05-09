package com.slr.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.models.SlrConfiguration;
import com.slr.app.services.SlrConfigurationService;

@RestController
@RequestMapping("/slr_configuration")
public class SlrConfigurationController {

	@Autowired
	private SlrConfigurationService config_service;
	
	
	@GetMapping("/find/{id}")
    public SlrConfiguration getConfigurationById(@PathVariable("id") Long id) {
        return this.config_service.getConfigurationById(id);
    }
	
	@GetMapping(value = "/get_validate") 
	public SlrConfiguration getValidateConfiguration(){
		return this.config_service.getValidateConfiguration("active");
	}
	
	@PostMapping("/add")
    public SlrConfiguration addConfiguration(@RequestBody SlrConfiguration configuration) {
        return this.config_service.saveConfiguration(configuration);
    }
	
	@PutMapping("/update")
    public SlrConfiguration updateConfiguration(@RequestBody SlrConfiguration configuration) {
        return this.config_service.updateConfiguration(configuration);
    }
	
}
