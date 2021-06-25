package com.slr.app.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.models.Publications;
import com.slr.app.services.PublicationsServices;
import com.slr.app.services.ResearchGateService;

@RestController
@RequestMapping("/researchgate")
public class ResearchGateController {
	
	@Autowired
	private PublicationsServices publications_service;
	@Autowired
	private ResearchGateService rg_service;
	
	
	@GetMapping(value = "/test")
	public List<Publications> test(
			@RequestBody(required = true) Map<String, String> values
			) throws InterruptedException
	{
		List<Publications> publications = new ArrayList<>();
		publications.add( this.publications_service.getPublicationById(Long.valueOf(1)) );
		
		//return this.researchgate.researchgateUpdatePublications(publications);
		this.rg_service.researchgateUpdatePublications(publications.get(0));
		//this.rg_service.test(publications.get(0));
		return null;
	}
	
}
