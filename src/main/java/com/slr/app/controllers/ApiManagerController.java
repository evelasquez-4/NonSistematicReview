package com.slr.app.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.services.ApiManagerService;

@RestController
@RequestMapping("/api")
public class ApiManagerController {
	
	@Autowired
	private ApiManagerService api_service;
	
	@GetMapping("/test")
	public void test(@RequestBody(required = true)  Map<String, String> values) {
		
		String key = values.containsKey("mendeley_key")? values.get("mendeley_key") : "";
		String doc = values.containsKey("doc_type") ? values.get("doc_type") : "";
		String state = values.containsKey("updated_state") ? values.get("updated_state") : "";
		int limit = values.containsKey("limit") ? Integer.valueOf( values.get("limit") ): 0;
		
		//this.api_service.updatePublicationFromSpringerMendeleyApi(doc,state, limit,key);
	}

}
