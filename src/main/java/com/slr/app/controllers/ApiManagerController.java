package com.slr.app.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.models.Publications;
import com.slr.app.services.ApiManagerService;
import com.slr.app.services.PublicationsServices;

@RestController
@RequestMapping("/api_controller")
public class ApiManagerController {
	
	@Autowired
	private ApiManagerService api_service;
	@Autowired
	private PublicationsServices publication_service;
	
	@PostMapping("/api_search")
	public String updatePublicationsFromMendeleySpringerAPI(@RequestBody(required = true)  Map<String, String> values) {
		
		String mendeley_key = values.containsKey("mendeley_key")? values.get("mendeley_key") : "";
		String doc_type = values.containsKey("doc_type") ? values.get("doc_type") : "";
		String updated_state = values.containsKey("updated_state") ? values.get("updated_state") : "";
		int limit = values.containsKey("limit") ? Integer.valueOf( values.get("limit") ): 0;
		
		return this.api_service.
				updatePublicationsFromMendeleySpringerAPI(doc_type,updated_state,
						mendeley_key,limit);
	}
	
	@GetMapping("/springer_api")
	public List<Publications> updatePublicationsFromSpringerAPI(
			@RequestParam("limit") int limit,
			@RequestParam("doc_type") String doc_type
			) throws Exception { 
			
		List<Publications> publications = this.publication_service.getPublicationsForSpringerApiUPdate(doc_type, limit);
		
		if(publications.isEmpty())
			throw new Exception("Error, publications must not be empty.");
		
		return this.api_service.updatePublicationsFromSpringerApi(publications);
		
	}
	
	@GetMapping("/mendeley_api")
	public List<Publications> updatePublicationsFromMendeleyAPI(@RequestBody(required = true)  Map<String, String> values){
		String mendeley_key = values.containsKey("mendeley_key")? values.get("mendeley_key") : "";
		String doc_type = values.containsKey("doc_type") ? values.get("doc_type") : "";
		int limit = Integer.valueOf( values.getOrDefault("limit", "0") );
		
		
		return this.api_service
				.updatePublicationsFromMendeleyApi(this.publication_service
						.getPublicationsForMendeleyApiUpdate(doc_type, limit), mendeley_key);
	}
	
	@GetMapping("/ieee_api")
	public String updatePublicationsFromIEEEAPI(
			@RequestParam("limit") int limit,
			@RequestParam("doc_type") String doc_type
			) throws Exception { 
		
		List<Publications> publications = this.publication_service.getPublicationsForIEEEApiUpdate(doc_type, limit);
		
		if(publications.isEmpty())
			throw new Exception("Error, publications must not be empty.");
		
		return this.api_service.updatePublicationsFromIEEEApi(publications).toString();
		
	}

}
