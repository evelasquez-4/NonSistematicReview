package com.slr.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

}
