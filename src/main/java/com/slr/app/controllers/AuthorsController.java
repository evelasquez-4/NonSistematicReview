package com.slr.app.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.models.Authors;
import com.slr.app.services.AuthorsService;

@RestController
@RequestMapping("/authors")
public class AuthorsController {
	
	@Autowired
	private AuthorsService authors_service; 
	
	@GetMapping(value = "/find/{id}")
	public Authors testing(@PathVariable("id") Long id) 
	{
		return this.authors_service.findById(id);
	}
	
	@PostMapping(value = "/parse_authors")
	@Transactional
	public void parseAuthors(@RequestBody(required = true) Map<String, String> values) {
		String xmlFile = values.getOrDefault("xml_file", "");
		String dtdFile = values.getOrDefault("dtd_file", "");
		this.authors_service.parseAuthors(xmlFile, dtdFile);
	}
}
