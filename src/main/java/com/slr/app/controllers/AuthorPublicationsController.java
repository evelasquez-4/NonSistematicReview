package com.slr.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.services.AuthorPublicationsService;
import com.slr.app.services.AuthorsService;
import com.slr.app.services.PublicationsServices;

@RestController
@RequestMapping("/auth_publication")
public class AuthorPublicationsController {

	@Autowired
	private AuthorPublicationsService ap_service;
	@Autowired
	private PublicationsServices publication_service;
	@Autowired
	private AuthorsService author_service;
	
	@PostMapping("test")
	public void test() {
		this.publication_service.getPublicationById(Long.valueOf(670) );
		//List<Authors>
	}
}
