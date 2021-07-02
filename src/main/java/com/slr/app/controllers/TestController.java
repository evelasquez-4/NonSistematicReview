package com.slr.app.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import com.slr.app.models.Authors;
import com.slr.app.models.Publications;
import com.slr.app.services.ApiManagerService;
import com.slr.app.services.AuthorPublicationsService;
import com.slr.app.services.AuthorsService;
import com.slr.app.services.PublicationsServices;

@RestController
@RequestMapping("/test")
public class TestController {
	
	@Autowired
	private AuthorPublicationsService ap_service;
	@Autowired
	private PublicationsServices pub_service;
	@Autowired
	private AuthorsService auth_service;
	@Autowired
	private ApiManagerService manager_service;
	
	
	@GetMapping("/test1")
	public List<Authors> test1(@RequestBody(required = true) Map<String, String> values) {		
		return this.auth_service.getAuthorsFromPublicationId(Long.valueOf( values.get("publication_id")));
	}
	
	@GetMapping("/test2")
	public Publications test2(@RequestBody(required = true) Map<String, String> values) {
		
		return this.pub_service.getPublicationsByDblpKey(
				values.get("dblp_key")
				);
	}
	
	@GetMapping("/test3")
	public List<Publications> test3(@RequestBody(required = true) Map<String, String> values) {
		List<Publications> res = this.pub_service.getPublicationsFromAuthorId(
				Long.valueOf(values.get("author_id")),values.get("updated_state") );
		return res;
	}
	
	@GetMapping("/test4")
	public List<Authors> test4(@RequestBody(required = true) Map<String, String> values)  {
		 return this.auth_service.getAuthorsByGroupPublicationUpdated(
				Boolean.parseBoolean( values.get("updated_state") ), Integer.valueOf( values.get("limit") ) );
	}
	
	@GetMapping("/test5")
	public void test5(@RequestBody(required = true) Map<String, String> values)
	{
		Authors a = this.auth_service.findById(Long.valueOf(21));
//		this.manager_service.updatePublicationsByAuthorNameFromMendeley( a, 
//				(values.get("publication_state")),
//				values.get("mendeley_key"));
	}
}
