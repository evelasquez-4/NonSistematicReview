package com.slr.app.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.helpers.AuthorPublicationDistinct;
import com.slr.app.helpers.AuthorPublicationsIndexedQueries;
import com.slr.app.helpers.DblpPublicationsQueries;
import com.slr.app.helpers.SlrHibernateLuceneIndex;
import com.slr.app.models.AuthorPublications;
import com.slr.app.models.Authors;
import com.slr.app.models.Countries;
import com.slr.app.models.DblpPublications;
import com.slr.app.models.IndexedAuthorPublications;
import com.slr.app.models.Publications;

@CrossOrigin
@RestController
@RequestMapping("/indexed_queries")
public class IndexedQueriesController {

	@Autowired
	private DblpPublicationsQueries dblp_queries;
	@Autowired
	private SlrHibernateLuceneIndex index_service;
	@Autowired
	private AuthorPublicationsIndexedQueries auth_pub;
	
	@Autowired
	private AuthorPublicationDistinct response_service;
	
	
	
	@GetMapping(value = "/dblp_find/{field}")
	public List<DblpPublications> findIndexedDblpByAuthorNames(
			@PathVariable(required = true) String field,
			@RequestBody(required = true) Map<String, String> values)
	{
		List<DblpPublications> response = new ArrayList<DblpPublications>();
		List<String> fields = Arrays.asList("author","key_dblp","title","crossref");
		
		if(!fields.contains(field))
			throw new RuntimeException("Field: "+field+" not indexed in DblpPublication");
		
		String search_text = values.getOrDefault("search_text", "author");
		int limit = Integer.valueOf( values.getOrDefault("limit", "1") );
		
		switch (field) {
			case "author":
					response = this.dblp_queries.findIndexedDblpByAuthorNames(search_text, limit);
			break;
			
			case "key_dblp":
				
					response = this.dblp_queries.findIndexedDblpBykeyDblp(search_text, limit);
				break;
			
		}
		
		return response;
	}
	
	//indexed queries in slr.authors
	@GetMapping(value = "/author_names")
	public List<Authors> findIndexedAuthorsByNameHomonyns(@RequestParam(required = true) Map<String, String> values){
		System.out.println("INGRESA\n"+values.toString());
		String text = values.getOrDefault("text_search", "");
		String table = "authors";
		int cant = Integer.valueOf(values.getOrDefault("limit", "0"));

		return this.index_service.findIndexedAuthorsByNamesHomonyns(text, table, cant);
	}
	
	
	//indexed queries in slr.author_publications
	//1, busca los nombre de los autores
	@GetMapping(value = "/authors")
	public List<Authors> findIndexedAuthorsByNameFromAuthorPublications(
			@RequestParam(required = true) Map<String, String> values){
		String searchText = values.getOrDefault("text_search", "");
		int limit = Integer.valueOf(values.getOrDefault("limit", "0"));
		
		return this.index_service.findIndexedAuthorsByNamesHomonyns(searchText	, "author_publications", limit);

	}
	
	//2, retorna las publicaciones en las que aparece el nombre del autor
	@GetMapping(value = "/publications")
	public List<AuthorPublications> getIndexedPublicationAuthorsFromAuthorPublications(
			@RequestParam(required = true) Map<String, String> values){
		String text_search = values.getOrDefault("text_search", "");
		int limit = Integer.valueOf(values.getOrDefault("limit", "0"));
		
		List<AuthorPublications> ap = this.auth_pub.getIndexedPublicationAuthorsFromAuthorPublications(text_search, limit);
		return ap;
	}
	
	
	
	//2.1 retorna autores, dado dblp_key
	@GetMapping(value = "/get_authors")
	public List<AuthorPublications> getIndexedAuthorsFromAuthorPublications(
			@RequestParam("limit") int limit,
			@RequestBody(required = true) Map<String, String> values )
	{
		int cant = limit > 0 ? limit : 0;
		String text_search = values.getOrDefault("text_search", "");
		
		//List<AuthorPublications> ap = 
		return this.auth_pub.findIndexedAuthorFromDblpKey(text_search, cant);
//		List<Authors> res = new ArrayList<Authors>();
//		
//		for (AuthorPublications a : ap)
//			res.add(a.getAuthors());
//		
//		
//		return res;
		
	}
	
	//3 busqueda en titulo y abstract de un publicacion
	@GetMapping(value = "/author/publications")
	public List<IndexedAuthorPublications> getIndexedPublicationsFromAuthorPublications(
			@RequestParam(required = true) Map<String, String> values){
		String text_search = values.getOrDefault("text_search", "");
		int limit = Integer.valueOf(values.getOrDefault("limit", "0"));
		
		System.out.println("Params:"+text_search+"\nLimit:"+limit);
		
		List<AuthorPublications> ap =  this.index_service
				.findPublicationsByTitleAbstract(text_search, limit);

		 return this.response_service.filterDistinctObjects(ap);
		// return new ArrayList<IndexedAuthorPublications>();
	}
	
	
	//4, busqueda por doi o isbn
	@GetMapping(value = "/find/publication/filtered")
	public List<AuthorPublications> getIndexedPublicationsByDoiIsbn(
			@RequestParam("filter") String filter,
			@RequestBody(required = true) Map<String, String> values){
		
		String text_search = values.getOrDefault("text_search", "");
		int limit = Integer.valueOf(values.getOrDefault("limit", "0"));
		List<AuthorPublications> response = new ArrayList<AuthorPublications>();
		
		switch (filter) {
		case "doi":
			response = this.index_service.findIndexedPublicationsByFieldName(text_search, "ee", limit);		
			break;
		case "isbn":
			 response = this.index_service.findIndexedPublicationsByNestedTypeDocument(text_search, "book", "isbn", limit);
			 
			 if(response.isEmpty())
				 response = this.index_service.findIndexedPublicationsByNestedTypeDocument(text_search, "incollection", "isbn", limit);
		 
		break;

		default:
			System.err.println("Unknow filter: "+filter);
			break;
		}
				 
		 return response;
	}
	
	
	
	
	@GetMapping(value = "/countries")
	public List<AuthorPublications> findIndexedCountriesByName(@RequestBody(required = true) Map<String, String> values){
		String searchText = values.getOrDefault("text_search", "");
		int limit = Integer.valueOf(values.getOrDefault("limit", "0"));
		
		return this.auth_pub.findIndexedCountries(searchText, limit);
	}
	
	@GetMapping(value = "/countries2")
	public List<Countries> findIndexedCountriesByName2(@RequestBody(required = true) Map<String, String> values){
		String searchText = values.getOrDefault("text_search", "");
		int limit = Integer.valueOf(values.getOrDefault("limit", "0"));
		
		return this.index_service.findCountriesByName(searchText, limit);
	}
	@GetMapping(value = "")
	public void findIndexedPublication() {
		
	}
}
