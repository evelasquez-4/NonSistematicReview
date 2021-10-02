package com.slr.app.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.Publications;
import com.slr.app.models.TmpApis;

@Service
public class ApiManagerService {

	@Autowired
	private PublicationsServices publication_service;
	@Autowired
	private AuthorsService author_service;
	@Autowired
	private AuthorPublicationsService authpub_service;
	@Autowired
	private KeywordsService keyword_service;
	@Autowired
	private PublicationKeywordsService pubkey_service;
	@Autowired
	private SpringerApiService springer_service;
	@Autowired
	private MendeleyApiService mendeley_service;
	@Autowired
	private TmpApisService tmp_service;
	@Autowired
	private SlrConfigurationService configuration_service;
	@Autowired
	private IEEEApiService ieee_service;
	@Autowired
	private EntityManager entityManager;
	
	/*
	 * @return: List<Publications>, updated
	 */
	@Transactional
	public List<Publications> updatePublicationsFromIEEEApi(List<Publications> publications) 
	{
		List<Publications> response = new ArrayList<Publications>();
		String param = this.ieee_service.obtainStringParamQuery(publications);
		System.out.println("Search Params: "+param);
		
		if(param.isEmpty()) {
			System.out.println("Publications: "+publications.size()+", not founded in ieee API.");
			for (Publications p : publications) {
				p.setApiState(p.getApiState()+1);
				
				this.entityManager.merge(p);
			}
			return response;
		}
					
		JSONObject ieee =this.ieee_service.searchIEEEPublicationByParam(param);
			
		if(ieee.getInt("total_records") == 0) {
			for (Publications p : publications) {
				p.setApiState(p.getApiState()+1);
				
				this.entityManager.merge(p);
			}
			return response;
		}else {
			
			for (Publications publication : publications) 
			{
				JSONObject pub_param = this.publication_service.getParameterToApiUpdateMendeleySpringer(publication);
				List<String> search_params = new ArrayList<String>();
				
				if(pub_param.getBoolean("has_doi") )
					search_params.add(pub_param.getString("doi"));
				
				if(pub_param.getBoolean("has_isbn"))
					search_params.add( pub_param.getString("isbn") );
				
				//obj : response from ieee api
				JSONObject pub_founded = this.ieee_service
						.searchPublicationInParameterList(pub_param, search_params);
				
				if(!pub_founded.isEmpty())
				{
					response.add(publication);
					
					this.publication_service.updatePublicationDataFromAPIS(	publication,
							this.ieee_service.obtainAuthorsFullNameFromJSONObject(pub_founded),
							this.ieee_service.obtainKeywordsFromJSONArray(pub_founded)	);
					
					Map<String, String> affiliations = this.ieee_service.obtainAuthorsAffiliation(pub_founded);
				
					
					affiliations.forEach( (k,v)->{
						System.out.println("Affiliation:"+k+" : "+v);
					});
					
					if( pub_founded.has("abstract") )
						publication.setAbstract_( pub_founded.getString("abstract") );
					
					publication.setUpdatedState("2.api_updated");
					//registro en public.tmp_apis
					this.tmp_service.save( new TmpApis(0, publication.getDblpKey(),"ieee", pub_param.toString(),
							this.configuration_service.getValidateConfiguration("active").getGroupState(),
							true,ieee.toString(),new Date()) );
					
				}
				publication.setApiState(publication.getApiState()+1);
				this.entityManager.merge(publication);				
			}
			
		}			
		return response;
	}
	
	/*
	 * @return: List<Publications> founded and updated in Springer API
	 */
	@Transactional
	public List<Publications> updatePublicationsFromSpringerApi(List<Publications> publications) {
		List<Publications> res = new ArrayList<Publications>();
		String params = this.springer_service.obtainSpringerApiParam(publications);
		System.out.println("Search Params: "+params);
		
		
		if(params.isEmpty()) {
			for (Publications p : publications) {
				p.setApiState( p.getApiState()+1);
				//this.publication_service.savePublications(p);
				this.entityManager.merge(p);
			}
			return res;
		}
		
		System.out.println("Requesting to Springer API");
		JSONObject springer = this.springer_service.findSpringerPublication(params);
		int total = Integer.valueOf( springer.getJSONArray("result").getJSONObject(0).getString("total") ) ;
		
		if(total == 0)//no results founded in springer api
		{
			for (Publications p : publications) {
				p.setApiState( p.getApiState()+1);
				//this.publication_service.savePublications(p);
				this.entityManager.merge(p);
			}
			return res;
		}else 
		{
			for (Publications publication : publications) 
			{
				JSONObject publication_params = this.publication_service.getParameterToApiUpdateMendeleySpringer(publication);
				List<String> search_params = new ArrayList<String>();
				
				if(publication_params.getBoolean("has_doi"))
					search_params.add(publication_params.getString("doi"));
				
				if(publication_params.getBoolean("has_isbn"))
					search_params.add( publication_params.getString("isbn") );
				
				
				//found publication parameter in api results
				JSONObject data = this.springer_service
						.searchPublicationByListParameters(springer, search_params);
				
				if(!data.isEmpty())
				{
					res.add(publication);
					
					this.publication_service.updatePublicationDataFromAPIS(
							publication, 
							this.springer_service.obtainAuthors(data) ,
							this.springer_service.obtainKeywords(data)
					);
					
					if(data.has("abstract"))
						publication.setAbstract_( data.getString("abstract") );
					
					publication.setUpdatedState("2.api_updated");
					//insert into tmp_apis table
					this.tmp_service.save(
							new TmpApis(0,publication.getDblpKey(),"springer", publication_params.toString(),//params
								this.configuration_service.getValidateConfiguration("active").getGroupState(),
								true,springer.toString(),new Date() )
							);
				}
				
				publication.setApiState(publication.getApiState()+1);
				this.entityManager.merge(publication);
			}
		}
		return res;
	} 
	
	
	@Transactional
	public List<Publications> updatePublicationsFromMendeleyApi(List<Publications> publications,String mendeley_token){
		List<Publications> response = new ArrayList<Publications>();
	
		for (Publications publication : publications)
		{
			JSONObject pub_params = this.publication_service.getParameterToApiUpdateMendeleySpringer(publication);
			
			if( !pub_params.getBoolean("has_doi") 
					&& !pub_params.getBoolean("has_isbn")	) 
			{
				publication.setApiState(publication.getApiState()+1);
				this.entityManager.merge(publication);
				
				break;
			}
			
			JSONArray mendeley  = new JSONArray();
			this.mendeley_service.setKey(mendeley_token);
			
			if(pub_params.getBoolean("has_doi")) 
			{
				try {
					mendeley = this.mendeley_service.findMendeleyPublicationByDOI(
							URLEncoder.encode( pub_params.getString("doi"), 
							StandardCharsets.UTF_8.toString() )   );					
				} catch ( Exception  e) {
					System.err.println("Function updatePublicationsFromMendeleyApi(), "+e.getMessage());
				}
			}else if( pub_params.getBoolean("has_isbn") && !pub_params.getBoolean("has_doi")) 
			{
				try {
					mendeley = this.mendeley_service.findMendeleyPublicationByISBN(
							URLEncoder.encode( pub_params.getString("isbn"),
							StandardCharsets.UTF_8.toString() )		);
				} catch ( Exception e) {
					System.err.println("Function updatePublicationsFromMendeleyApi(), "+e.getMessage());
				} 
			}
			
			if(!mendeley.isEmpty()) {
				response.add(publication);
				
				for(int i=0;i<mendeley.length();i++) {
					JSONObject obj = mendeley.getJSONObject(i);
					
					if(obj.has("abstract"))
						publication.setAbstract_(obj.getString("abstract"));
					
					this.publication_service.updatePublicationDataFromAPIS(publication,
							this.mendeley_service.obtainAuthors(obj),
							this.mendeley_service.obtainKeywords(obj) );
					
				}
				
				//registro en public.tmp_apis
				this.tmp_service.save( new TmpApis(0, publication.getDblpKey(),"mendeley", pub_params.toString(),
						this.configuration_service.getValidateConfiguration("active").getGroupState(),
						true,mendeley.toString(),new Date()) );
				
				publication.setUpdatedState("2.api_updated");
			}
			
			publication.setApiState(publication.getApiState()+1);
			this.entityManager.merge(publication);
			
		}

		return response;
	}
	
	
	
	/*
	 * Detail: funcion que busca información de una publicación,
	 * 			de acuerdo al nombre de autor, usando la API de MENDELEY
	 */
	@Transactional
	public String updatePublicationsFromMendeleySpringerAPI(String publication_type,String publication_state, String mendeley_key, Integer ...cant) 
	{
		//List<Publications> publications = this.publication_service.getPublicationsFromAuthorId(author.getId(), publication_state);
		List<Publications> publications = this.publication_service.getPublicationsByTypeState(publication_type,
													publication_state, 
													cant.length > 0 ? cant[0].intValue() : 50);
		System.out.println("Limit:"+cant[0]+"\nPublication: "+publications.size());
	
		int finded = 0; int nofinded = 0 ;
		try {
		
			for (Publications publication : publications) 
			{
				//JSONObject{"key":String,"api":"mendeley","results":int,"abstract":string,"authors":array,"keywords":array}
				JSONObject mendeley = mendeleyApiUpdate(publication, mendeley_key);

				//JSONObject{"id":String,"key":String,"api":"mendeley","results":int,"abstract":string,"authors":array,"keywords":array}
				JSONObject springer = mendeley.getInt("results") < 1 ?  
							new JSONObject()
							.put("id", publication.getId())
							.put("api", "springer")
							.put("results", 0)	
							.put("abstract", "")
							.put("authors", new JSONArray())
							.put("keywords", new JSONArray())
							
							: springerApiUpdate(publication);
				
				//JSONObject{"results":int,"abstract":string,"authors":array,"keywords":array}
				JSONObject api = mergeJSONObjectList(Arrays.asList(mendeley,springer));
				
				if(api.getInt("results") > 0) {
					for (String keys : api.keySet()) {
						
						switch (keys) {
						case "abstract":
								if(!api.getString("abstract").isEmpty())
									publication.setAbstract_(api.getString("abstract"));
							break;
						case "authors":
								if( !this.publication_service.hasAuthors(publication.getId())
									&& api.getJSONArray("authors").length() > 0	) {
									
									List<String> authors = new ArrayList<String>();
									
									api.getJSONArray("authors").forEach(a->{
										authors.add((String)a);
									});
									
									//insert in slr.author_publications
									this.authpub_service.saveAuthorPublications(publication, 
											this.author_service.findAuthorsIndexedByListAuthors(authors) );
									
								}
							break;
						case "keywords":
							if( !this.keyword_service.publicationHasKeywords(publication.getId())
								&&	api.getJSONArray("keywords").length() > 0 ) {
								List<String> keywords = new ArrayList<String>();
								
								api.getJSONArray("keywords").forEach(k->{
									keywords.add((String) k);
								});
								
								this.pubkey_service.registerPublicationsKeywords(keywords, publication);
							}
							
							break;
						}
					}
					
					//update publication state
					publication.setUpdatedState("2.api_finded");
					finded++;
				}else {
					publication.setUpdatedState("3.api_nofinded");
					nofinded++;
				}
				
				this.publication_service.savePublications(publication);
				
			}
			return "Publications updated: "+finded+"\nPublications no finded: "+nofinded;
		}catch(JSONException  e) {
			System.err.println("function updatePublicationsFromMendeleySpringerAPI(), "+e.getMessage());
		}
		return "Publications updated: "+finded+"\nPublications no finded: "+nofinded;
	}
	
	/*
	 * @return JSONObject{"key":String,"api":"mendeley","results":int,"abstract":string,"authors":array,"keywords":array}
	 */
	public JSONObject mergeJSONObjectList(List<JSONObject> objects) {
		JSONObject response = new JSONObject();
		String abstract_ = "";
		List<String> keywords = new ArrayList<String>();
		List<String> authors = new ArrayList<String>();
		int results = 0;
		
		
		for (JSONObject obj : objects) {
			if(obj.getInt("results") > 0) {
				abstract_ = abstract_.isEmpty() ? obj.getString("abstract") : "";
				
				if( keywords.isEmpty() && obj.getJSONArray("keywords").length() >  0 ){
					
					obj.getJSONArray("keywords").forEach( k->{
						keywords.add((String) k);
					});
				}
				
				if(authors.isEmpty() && obj.getJSONArray("authors").length() > 0) {
					
					obj.getJSONArray("authors").forEach(a->{
						authors.add((String) a);
					});
				}
				
				results++;
			}
		}
		
		return response.put("abstract", abstract_)
				.put("authors", authors)
				.put("keywords", keywords)
				.put("results", results);
	}
	
	/*
	 * @return JSONObject{"key":String,"api":"mendeley","results":int,"abstract":string,"authors":array,"keywords":array}
	 */
	public JSONObject mendeleyApiUpdate(Publications pub, String key) {
		
		JSONObject response = new JSONObject();
		JSONArray json_array = new JSONArray();
		//@Param: JSONObject {key:"","has_doi":boolean,"doi":"","has_isbn":boolean,"isbn":""}
		JSONObject json = this.publication_service.getParameterToApiUpdateMendeleySpringer(pub);
		
		try
		{
			this.mendeley_service.setKey(key);
			
			if(json.getBoolean("has_doi") ) {
				System.out.println("Searching by doi: "+json.getString("doi"));
				json_array = this.mendeley_service
					.findMendeleyPublicationByDOI(json.getString("doi"));
			}
			
			if(json.getBoolean("has_isbn") && json_array.isEmpty()) {
				System.out.println("Searching by isbn: "+json.getString("isbn"));
				json_array = this.mendeley_service
					.findMendeleyPublicationByISBN(json.getString("isbn"));
			}
			
			
			
			if(json_array.isEmpty()) {
				System.out.println("Publication: "+pub.getDblpKey()+", not founded in mendeley API.");
				response.put("key", pub.getDblpKey())
				.put("api", "mendeley")
				.put("results", 0);
			}
			else {			
				int grupo = this.configuration_service.getValidateConfiguration("active").getGroupState();
				
				response.put("key", pub.getDblpKey())
				.put("api", "mendeley")
				.put("results", json_array.length());
				
				for(int i = 0;i < json_array.length(); i++) {
					JSONObject obj = json_array.getJSONObject(i);
					
					//authors list
					List<String> authors = obj.has("authors") ? this.mendeley_service.obtainAuthors(obj):new ArrayList<String>();
					//publication keywords
					List<String> keywords = obj.has("keywords")?this.mendeley_service.obtainKeywords(obj):new ArrayList<String>();
					
					//obtain abstract
					response.put("abstract", obj.has("abstract") ? obj.getString("abstract") : "")
						.put("authors", new JSONArray(authors))
						.put("keywords", new JSONArray(keywords));
					
					
					//insert into tmp_apis table
					this.tmp_service.save(
							new TmpApis(0,pub.getDblpKey(),"mendeley",json.toString(),//params
								grupo,true,obj.toString(),new Date() )
							);
				}
			}
		}catch(Exception e) {
			System.err.println("function mendeleyApiUpdate(), "+e.getMessage());
		}
		System.out.println("Mendeley API: "+response.toString());
		return response;
	}
	
	/*
	 * @return JSONObject{"id":String,"key":String,"api":"mendeley","results":int,"abstract":string,"authors":array(String),"keywords":array(String)}
	 */
	public JSONObject springerApiUpdate(Publications pub) 
	{
		JSONObject res =new JSONObject();
		JSONArray json_array = new JSONArray();
		JSONObject springer = new JSONObject();
	
		json_array.put(this.publication_service.getParameterToApiUpdateMendeleySpringer(pub));
		String params = this.springer_service.getPublicationUpdateParameters(json_array);
		System.out.println("Searching params: "+params);
		
		res.put("id", pub.getId())
		.put("key", pub.getDblpKey())
		.put("api", "springer");
		
		//llamada a la api de springer para actualizar publicacion
		if(!params.isEmpty()) 
		{
			springer = this.springer_service.findSpringerPublication(params);
			int springer_result = Integer.parseInt(  ((JSONArray)springer.get("result")).getJSONObject(0).getString("total") );
			
			if(springer_result > 0) 
			{
				int grupo = this.configuration_service.getValidateConfiguration("active").getGroupState();
				res.put("results", springer_result);
				
				JSONArray results = springer.getJSONArray("records");
				
				for (int i = 0; i < results.length(); i++) 
				{
					JSONObject rec = results.getJSONObject(i);
					String abstract_ = rec.has("abstract") ? rec.getString("abstract"):"";
					
					res.put("abstract", abstract_);
					res.put("authors", new JSONArray(this.springer_service.obtainAuthors(rec)));
					res.put("keywords", new JSONArray(this.springer_service.obtainKeywords(rec)));
					
					
					//insert into tmp_apis table
					this.tmp_service.save(
							new TmpApis(0,pub.getDblpKey(),"springer",params,//params
								grupo,true,rec.toString(),new Date() )
							);
				}
				
			}else {
				System.out.println("Publication: "+pub.getDblpKey()+", not founded in springer API.");
				res.put("results", springer_result);
			}
			
		}else {
			System.out.println("Publication: "+pub.getDblpKey()+", not founded in springer API.");
			res.put("results", 0);
		}
		System.out.println("Springer API: "+res.toString());
		return res;
	}
}
