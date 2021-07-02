package com.slr.app.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.helpers.SlrHibernateLuceneIndex;
import com.slr.app.models.Authors;
import com.slr.app.models.Publications;

@RestController
@RequestMapping("/lucene_index")
public class SlrHibernateLuceneIndexController {

	@Autowired
	private SlrHibernateLuceneIndex index;
	
	@PostMapping(value = "/indexing/{entity}")
	public String indexing(@PathVariable String entity) {
		if(Objects.isNull(entity))
			throw new NullPointerException("Error con el nombre de la clase enviada para su indexación: "+entity);
		
		return this.index.indexEntity(entity);
	}
	
	@GetMapping(value = "/authors")
	public List<Authors> findAuthorsByName(@RequestBody(required = true) Map<String, String> values){
		String names = values.containsKey("names") ? values.get("names") : "";
		return this.index.findAuthorsIndexedByName(names);
	}
	
	@GetMapping(value = "/list_authors")
	public List<Authors> findAuthorsList(@RequestBody(required = true) Map<String, String> values){
		String names = values.containsKey("names") ? values.get("names") : "";
		List<String> authors = new ArrayList<>(Arrays.asList("Qianhao Fang","Yihe Huang","Kai Xu"));
		return this.index.findAuthorsIndexedByListAuthors(authors);
	}
	
	
	//start functions to search with hibernate search
	@GetMapping(value = "/search_publications")
	public List<Publications> searchInPublications(@RequestBody(required = true) Map<String, String> values) throws Exception{
		
		if(values.isEmpty())
			throw new Exception("Input values to search are null");
		
		
		
		
		return this.index.searchPublicationMatchingTitleAbstract(
				values.get("title"),
				Integer.valueOf(values.get("limit"))
				);
	}
	
}
