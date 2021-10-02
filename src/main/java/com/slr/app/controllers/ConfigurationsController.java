package com.slr.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.services.AuthorsService;
import com.slr.app.services.OrganizationsService;
import com.slr.app.services.SlrConfigurationService;

@RestController
@RequestMapping("/configurations")
public class ConfigurationsController {

	@Autowired
	private OrganizationsService organization_service;
	@Autowired
	private AuthorsService author_service;
	@Autowired
	private SlrConfigurationService configuration_service;
	
	
	
	//update author affiliations in slr.organizations
	@PostMapping("/author_organizations/{limit}")
	public String updateAuthorsOrganizations(
			@PathVariable() Integer limit) {

		return this.organization_service.updateAuthorsAffiliation(
					this.author_service.getAuthorsToUpdateAffiliation(
							false,
							this.configuration_service.getValidateConfiguration("active").getGroupState(), 
							limit > 0 ? limit : 1)
				);
	}

}
