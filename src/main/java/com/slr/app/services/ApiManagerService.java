package com.slr.app.services;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.Authors;
import com.slr.app.models.Publications;

@Service
public class ApiManagerService {

	@Autowired
	private PublicationsServices publication_service;
	@Autowired
	private SpringerApiService springer_service;
	@Autowired
	private MendeleyApiService mendeley_service;
	
	
	
	
	
	/*
	 * Detail: funcion que busca información de una publicación,
	 * 			de acuerdo al nombre de autor, usando la API de MENDELEY
	 */
	public void updatePublicationsFromMendeleySpringerAPI(Authors author,String publication_state, String mendeley_key) 
	{
		List<Publications> publications = this.publication_service.getPublicationsFromAuthorId(author.getId(), publication_state);

		try {
		
//			for (Publications publication : publications) {
//				JSONObject{"key":String,"api":"mendeley","results":int,"abstract":string,"authors":array,"keywords":array}
//				JSONObject
//			}
		}catch(JSONException  e) {
			System.err.println("function updatePublicationsFromAuthorNames(), "+e.getMessage());
		}
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
					
				}
			}
		}catch(Exception e) {
			System.err.println("function mendeleyApiUpdate(), "+e.getMessage());
		}
		
		
		return response;
	}
	
	/*
	 * @return JSONObject{"id":String,"key":String,"api":"mendeley","results":int,"abstract":string,"authors":array,"keywords":array}
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
				res.put("results", springer_result);
				
				JSONArray results = springer.getJSONArray("records");
				
				for (int i = 0; i < results.length(); i++) 
				{
					JSONObject rec = results.getJSONObject(i);
					String abstract_ = rec.has("abstract") ? rec.getString("abstract"):"";
					
					res.put("abstract", abstract_);
					res.put("authors", new JSONArray(this.springer_service.obtainAuthors(rec)));
					res.put("keywords", new JSONArray(this.springer_service.obtainKeywords(rec)));
				}
				
			}else {
				System.out.println("Publication: "+pub.getDblpKey()+", not founded in springer API.");
				res.put("results", springer_result);
			}
			
		}else {
			System.out.println("Publication: "+pub.getDblpKey()+", not founded in springer API.");
			res.put("results", 0);
		}

		return res;
	}
}
