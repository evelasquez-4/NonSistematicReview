package com.slr.app.models;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class IndexedAuthorPublications {

	private Publications publication;
	private List<Authors> authors = null;
	
	
	public IndexedAuthorPublications() {
		this.publication = new Publications();
		this.authors = new ArrayList<Authors>();
	}
	
	public IndexedAuthorPublications(Publications publications, List<Authors> authors) {
		this.publication = publications;
		this.authors = authors;
	}
	
	public Publications getPublication() {
		return publication;
	}
	public void setPublication(Publications publication) {
		this.publication = publication;
	}
	public List<Authors> getAuthors() {
		return authors;
	}
	public void setAuthors(List<Authors> authors) {
		this.authors = authors;
	}
	
}
