package com.slr.app.controllers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.models.DblpPublications;
import com.slr.app.services.DblpPublicationsService;

@RestController
@RequestMapping("/dblp_publication")
public class DblpPublicationsController {
	
	
	@Autowired
	private DblpPublicationsService dblp_service;	
	
	@GetMapping("/find/{id}")
    public DblpPublications getPublicationById(@PathVariable("id") Long id) {
        return this.dblp_service.getPublicationById(id);
    }
	
	@PostMapping("/parse_file")
	@Transactional
    public void parseFile(@RequestBody(required = true) Map<String, String> values) {
        String xmlFileLocation = values.containsKey("xml_file") ? values.get("xml_file") : "";
        String dtdFileLocation = values.containsKey("dtd_file")? values.get("dtd_file") : "";
    
        this.dblp_service.parseDblpFiles(xmlFileLocation, dtdFileLocation);
    }
	
	@PostMapping("/divided_rows")
	public String parse(@RequestBody(required = true) Map<String, String> values) throws ParseException {
		String doc = values.getOrDefault("doc_type", "");
		String state = values.getOrDefault("updated_state","");
		int limit = values.containsKey("limit") ? Integer.valueOf( values.get("limit") ): 0;
		
		if(doc.isEmpty() || state.isEmpty() || limit == 0)
			throw new RuntimeException("Verifique los parametros enviados:\ndoc_type: "+doc+"\nstate: "+state+"\nlimit: "+limit);
			
		return this.dblp_service.insertIntoAuthorPublicationsByDoctypeState(doc, state, limit);
		
	}
	
	@PostMapping("/divided_rows/{id}")
	public String parseById(@RequestBody(required = true) Map<String, String> values,@PathVariable("id") Long id) throws ParseException {
		
		String state = values.containsKey("updated_state") ? values.get("updated_state") : "";
		int grupo = 1;//this.configuration.getValidateConfiguration("active").getGroupState();
			
		this.dblp_service.insertIntoAuthorPublications(new ArrayList<DblpPublications>(Arrays.asList(
				this.dblp_service.findByIdStateGroup(id, state, grupo)
				)));
		return "Publication inserted: "+id+"\nState: "+state;
		
	}
	
	@RequestMapping(value = "/divided_rows/list", method = RequestMethod.POST
			,consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public String parseByListId(@RequestBody(required = true) Map<String,Object> ids) throws ParseException {
		
		@SuppressWarnings("unchecked")
		List<Integer> identifiers = (List<Integer>) ids.get("ids"); 
		List<DblpPublications> dblp = new ArrayList<DblpPublications>();
		
		identifiers.forEach(i->{
			dblp.add(this.dblp_service.getPublicationById( Long.valueOf(i) ));
		}); 
		
		this.dblp_service.insertIntoAuthorPublications(dblp);
		
		return "Publications inserted: "+identifiers.size();
	}
	
	@GetMapping("/list_pageable")
	public ResponseEntity<Map<String, Object>> getDblpPublicationsByTypeStateGroupPageable(
			@RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        @RequestBody(required = true) Map<String, String> values)
	{
		String doc_type = values.getOrDefault("doc_type", "");
		String state = values.getOrDefault("state", "1.inserted");
		Long grupo = Long.valueOf(	values.getOrDefault("grupo", "1") );
		
		try {
			
			Pageable paging = PageRequest.of(page, size);
			Page<DblpPublications> pages = this.dblp_service.getDblpPublicationsByTypeStateGroupPageable(paging, doc_type, state, grupo);
			
			
			Map<String, Object> response = new HashMap<String, Object>();
			response.put("dblp_publications",pages.getContent());
			response.put("currentPage",	pages.getNumber() );
			response.put("totalItems", pages.getTotalElements());
			response.put("totalPages", pages.getTotalPages());
			
			
			return new ResponseEntity<>(response, HttpStatus.OK);
			
		} catch ( Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
