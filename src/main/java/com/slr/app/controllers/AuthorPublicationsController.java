package com.slr.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.services.AuthorPublicationsService;

@RestController
@RequestMapping("/auth_publication")
public class AuthorPublicationsController {

	@Autowired
	private AuthorPublicationsService ap_service;
	
	@PostMapping("test")
	public void test() {
		
		//List<Authors>
	}
}
