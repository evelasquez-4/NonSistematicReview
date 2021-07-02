package com.slr.app.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.services.ApiManagerService;

@RestController
@RequestMapping("/api")
public class ApiManagerController {
	
	@Autowired
	private ApiManagerService api_service;
	
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

}
