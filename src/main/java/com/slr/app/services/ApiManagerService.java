package com.slr.app.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
	
	/*
	 * Detail: funcion que busca información de una publicación,
	 * 			de acuerdo al nombre de autor, usando la API de MENDELEY
	 */
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

		/*
		int j = 0;
		for (int i = 0; i < pubs.size(); i++) {
			
			json_array.put(this.publication_service
						.getParameterToApiUpdateMendeleySpringer(pubs.get(i)));
			j++;
			if( j % CONSTANTE == 0 ) {
				params = this.springer_service.getPublicationUpdateParameters(json_array);
				//llamada a la api de springer para actualizar publicacion
				if(!params.isEmpty()) {
					springer = this.springer_service.findSpringerPublication(params);
				}
				
			}
		}*/
	
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
