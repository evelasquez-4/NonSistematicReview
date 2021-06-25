package com.slr.app.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.models.Publications;
import com.slr.app.services.PublicationsServices;

@RestController
@RequestMapping("/publication")
public class PublicationsController {
	
	@Autowired
	private PublicationsServices publications_service;
	
	@GetMapping("/find/{id}")
	public Publications getPublicationById(@PathVariable("id") Long id) {
		return this.publications_service.getPublicationById(id);
	}
	
	@PostMapping("/api_aupdate")
	public List<Publications> springerMendeleyApiUpdate(@RequestBody(required = true)  Map<String, String> values) {
		String doc = values.containsKey("doc_type") ? values.get("doc_type") : "";
		String state = values.containsKey("updated_state") ? values.get("updated_state") : "";
		int limit = values.containsKey("limit") ? Integer.valueOf( values.get("limit") ): 0;
		
		if(doc.isEmpty() || state.isEmpty() || limit == 0)
			throw new RuntimeException("Verifique los parametros enviados:\ndoc_type: "+doc+"\nstate: "+state+"\nlimit: "+limit);
		
		//return this.publications_service.springerMendeleyApiUpdate(doc,state,limit);
		return null;
	}

}
