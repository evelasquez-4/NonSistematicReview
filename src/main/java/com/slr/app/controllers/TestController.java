package com.slr.app.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.helpers.SlrHibernateLuceneIndex;
import com.slr.app.models.AuthorPublications;
import com.slr.app.models.Authors;
import com.slr.app.models.Countries;
import com.slr.app.models.Publications;
import com.slr.app.services.ApiManagerService;
import com.slr.app.services.AuthorPublicationsService;
import com.slr.app.services.AuthorsService;
import com.slr.app.services.DblpPublicationsService;
import com.slr.app.services.OrganizationsService;
import com.slr.app.services.PublicationsServices;
import com.slr.app.services.TmpApisService;



@RestController
@RequestMapping("/test")
@SuppressWarnings("unused")
public class TestController {
	
	@Autowired
	private AuthorPublicationsService ap_service;
	@Autowired
	private PublicationsServices pub_service;
	@Autowired
	private AuthorsService auth_service;
	@Autowired
	private ApiManagerService manager_service;
	@Autowired
	private SlrHibernateLuceneIndex index;
	@Autowired
	private DblpPublicationsService dblp;
	@Autowired
	private TmpApisService tmp_service;
	@Autowired
	private OrganizationsService organization_service;
	@Autowired
	private AuthorPublicationsService auth_pub_service;
	
	
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
	
//	@GetMapping("/test4")
//	public List<Authors> test4(@RequestBody(required = true) Map<String, String> values)  {
//		 return this.auth_service.getAuthorsByGroupPublicationUpdated(
//				Boolean.parseBoolean( values.get("updated_state") ), Integer.valueOf( values.get("limit") ) );
//	}
	
//	@GetMapping("/test4")
//	public String test4(@RequestBody(required = true) Map<String, String> values)  {
//		 return this.organization_service.getOrganizationCountry(
//				 values.get("text")
//				 );
//	}
	
	@GetMapping("/test5")
	public List<Countries> test5(@RequestBody(required = true) Map<String, String> values)
	{
		System.out.println(values.get("country_name"));
		return this.index.findCountriesByName(values.get("country_name"), 10);
	}
	
	@GetMapping("/test6")
	public List<Authors> test6(@RequestBody(required = true) Map<String, String> values)
	{
		return this.index.findIndexedAuthorsByNamesHomonyns(values.get("names"),"authors");
	}
	@GetMapping("/test7")
	public List<AuthorPublications> test7(@RequestBody(required = true) Map<String, String> values){
		return this.index.findPublicationsByTitleAbstract(
				values.get("text_search")
				);
	}
	@GetMapping("/test8")
	public List<AuthorPublications> test8(@RequestBody(required = true) Map<String, String> values){
		return this.index.findIndexedPublicationsByFieldName(
				values.get("text_search"), 
				values.get("field"), 20);
	}
	@GetMapping("/test9")
	public List<Publications> test9(@RequestBody(required = true) Map<String, String> values){
		return this.pub_service.getPublicationsFromAuthorId(
				Long.valueOf( 	values.get("author_id")),
				values.get("updated_state"));
	}
	
	//busqueda por nombre homonimo
	@GetMapping("/test10")
	public List<Authors> test10(@RequestBody(required = true) Map<String, String> values){
		return this.index
				.findIndexedAuthorsByNamesHomonyns(
						values.get("names"),
						"author_publications",
						Integer.valueOf(values.get("limit"))
						);
	}
	//busqueda publicacion por texto
	@GetMapping("/test11")
	public List<AuthorPublications> test11(@RequestBody(required = true) Map<String, String> values){
		return this.index
				.findPublicationsByTitleAbstract(
						values.get("search_text"),
						Integer.valueOf(values.get("limit"))
						);
	}
	
	//busqueda publicacion por otros elements
	@GetMapping("/test12")
	public List<AuthorPublications> test12(@RequestBody(required = true) Map<String, String> values){
		return this.index
				.findIndexedPublicationsByFieldName(
						values.get("search_text"),
						values.get("search_field"),
						Integer.valueOf(values.get("limit"))
						);
	}
	
	@GetMapping("/test13")
	public String test13() {
			List<Publications> p = new ArrayList<Publications>();
			p.add(this.pub_service.getPublicationById(Long.valueOf(2509)));
			p.add(this.pub_service.getPublicationById(Long.valueOf(2510)));
			p.add(this.pub_service.getPublicationById(Long.valueOf(9)));
			p.add(this.pub_service.getPublicationById(Long.valueOf(10)));
			p.add(this.pub_service.getPublicationById(Long.valueOf(1005)));
			
			
			return this.manager_service.updatePublicationsFromIEEEApi(p).toString();
		
	}
	
	@GetMapping("/test14")
	public List<Publications> test14() {
		List<Publications> p = this.pub_service.getPublicationsForSpringerApiUPdate("book", 5);
		
		return this.manager_service.updatePublicationsFromSpringerApi(p);
	}
	
	
}
