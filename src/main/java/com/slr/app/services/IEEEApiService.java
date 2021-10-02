package com.slr.app.services;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.Publications;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class IEEEApiService {

	private String urlBase = "https://ieeexploreapi.ieee.org/api/v1/search/articles";
	private String api_key = "apikey=ww38hnrucs89vggdqadcc6an";
	
	@Autowired
	private PublicationsServices publication_service;
	
	
	private OkHttpClient client = new OkHttpClient
			.Builder()
			.connectTimeout(60,TimeUnit.SECONDS)
			.writeTimeout(60, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.build();

	
	public JSONObject searchIEEEPublicationByParam(String param) {
		JSONObject res = new JSONObject();
		setUrlBase( "http://ieeexploreapi.ieee.org/api/v1/search/articles?"+api_key+"&format=json&start_record=1&sort_order=asc&sort_field=article_number&"+param );
		
		Request request = new Request.Builder()
				.url(this.urlBase)
				.method("GET", null)
				.build();
		
		try(Response response = client.newCall(request).execute())
		{
			if (!response.isSuccessful())
				throw new IOException("Unexpected code  -> " + response.code()+" "+response);
		
			res = new JSONObject(response.body().string());
			System.out.println(res.toString());
			response.close();
			
		}catch (Exception  e) {
			System.err.println("Function :searchIEEEPublicationByParam(), springer api error"+e.getMessage());
		}		
		return res;

	}
	
	//dado una lista de publicaciones obtiene la cadena de busqueda en ieee
	public String obtainStringParamQuery(List<Publications> publications) {
		List<String> params = new ArrayList<String>();
		
		for (Publications p : publications)
			params.add(
					getPublicationParamStringFromJSONObject(	
							this.publication_service.getParameterToApiUpdateMendeleySpringer(p)
							)
					);
		if(!params.isEmpty())
			return "max_records="+publications.size()+"&"+concatenateParamStringToQuery(params);
		else
			return "";
		
	}
	
	
	/*
	 * @Param: JSONObject [{key:"","has_doi":boolean,
	 * "doi":"",
	 * "has_isbn":boolean,
	 * "isbn":""}]
	 * @return :  cadena de busqueda en IEEE eg: doi = 10.2...., isbn=21890210
	 */
	public String getPublicationParamStringFromJSONObject(JSONObject obj) {
		String cadena = "";
		
		try {
			if(obj.getBoolean("has_doi"))
				cadena = "doi:"+obj.getString("doi");
			else {
				if(obj.getBoolean("has_isbn") && cadena.isEmpty())
					cadena = "isbn:"+obj.getString("isbn");
			}
						
		}catch (Exception e) {
			System.out.println("Function getPublicationParamsByArrayParams(), "+e.getMessage());
		}
		return cadena;
	}
	
	//funcion que forma la cadena de busqueda en la API de IEEE
	public String concatenateParamStringToQuery(List<String> params) {
		String res = "";
		
		if(!params.isEmpty()) {
			res = params.get(0);
			
			for(int i=1;i<params.size();i++)
				res += " OR "+params.get(i);
		}
		try {
			res = res.isEmpty() ? "": URLEncoder.encode( res,java.nio.charset.StandardCharsets.UTF_8.toString());
		}catch (Exception e) {
			System.err.println("Function concatenateParamStringToQuery(), "+e.getMessage());
		}
		
		return "querytext=("+res+")";
	}
	
	public JSONObject searchPublicationInParameterList(JSONObject json,List<String> params){
		JSONObject response =new JSONObject();
		try 
		{
			JSONArray array = json.getJSONArray("articles");
				
			for(int i= 0;i<array.length();i++) 
			{
				JSONObject obj = array.getJSONObject(i);
				
				if(obj.has("doi") && params.contains(obj.getString("doi")) ) 
					return obj;
				else if(	obj.has("isbn") && params.contains(obj.getString("isbn"))  )
					return obj;				
			}
		}catch (Exception e) {
			System.err.println("Function searchElements(), "+e.getMessage());
		}
		return response;
	}
	
	/*
	 * @param: json_publication :{ "authors":{"authors":[]}}
	 */
	public List<String> obtainKeywordsFromJSONArray(JSONObject json_publication) {
		List<String> res = new ArrayList<String>();
		try {
			JSONArray keywords_array = json_publication
					.getJSONObject("index_terms")
					.getJSONObject("ieee_terms").getJSONArray("terms");
			
			for(int i= 0;i<keywords_array.length();i++)
				res.add(  keywords_array.get(i).toString()  );
			
			return res;
		}catch (JSONException e) {
			System.out.println("Function obtainKeywordsFromJSONArray(), "+e.getMessage());
			return res;
		}
	}
	
	public Map<String, String> obtainAuthorsAffiliation(JSONObject json_publication){
		Map<String,String> res = new HashMap<String, String>();
		
		JSONArray authors_array = json_publication.getJSONObject("authors").getJSONArray("authors");
		for(int i = 0;i<authors_array.length();i++) 
		{
			JSONObject obj = authors_array.getJSONObject(i);
			if(obj.has("affiliation")) {
				res.put(
					obj.getString("full_name"),
					obj.getString("affiliation") );
			}
		}
		
		return res;
	} 
	
	/*
	 * @param: json_publication :{ "authors":{"authors":[]}}
	 */
	public List<String> obtainAuthorsFullNameFromJSONObject(JSONObject json_publication){
		List<String> res = new ArrayList<String>();
		try {
			JSONArray authors_array = json_publication.getJSONObject("authors").getJSONArray("authors");
			
			for(int i = 0;i<authors_array.length();i++) 
			{
				JSONObject obj = authors_array.getJSONObject(i);
				res.add(obj.getString("full_name"));
			}
			return res;
		}catch (JSONException e) {
			System.out.println("Function obtainAuthorsFullNameFromJSONObject(), "+e.getMessage());
			return res;
		}
	}
	
	public String getApi_key() {
		return api_key;
	}

	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}

	public String getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}	
}
