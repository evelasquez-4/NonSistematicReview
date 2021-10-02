package com.slr.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.Publications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;

@Service
public class SpringerApiService
{	
	private OkHttpClient client = new OkHttpClient
			.Builder()
			.connectTimeout(60,TimeUnit.SECONDS)
			.writeTimeout(60, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.build();
	private String api_key = "api_key=de56a248cf6ecfa4f5dbce1342c57e81&s=1";
	private String urlBase;
	@Autowired
	private PublicationsServices pub_service;
	

	
	public JSONObject findSpringerPublicationByDOI(String doi) throws Exception
	{
		setUrlBase("http://api.springernature.com/meta/v2/json?q=doi:"+doi+"&p=1"+api_key);
		
		Request request = new Request.Builder()
				.url(this.urlBase)
				.method("GET", null)
				.build();
		
		JSONObject res = new JSONObject();
		try(Response response = client.newCall(request).execute())
		{
			if (!response.isSuccessful()) {
				throw new IOException("Unexpected code  -> " + response.code()+" "+response);
			}
		
			res = new JSONObject(response.body().string());
			response.close();
		}catch (Exception  e) {
			System.err.println("Function: findSpringerPublicationByDOI(), "+e.getMessage());
		}
		return res;
	}
	
	
	public JSONObject findSpringerPublication(String param)
	{
		JSONObject res = new JSONObject();
		setUrlBase("http://api.springernature.com/meta/v2/json?"+api_key+param);
		
		Request request = new Request.Builder()
				.url(this.urlBase)
				.method("GET", null)
				.build();
		
		try(Response response = client.newCall(request).execute())
		{
			if (!response.isSuccessful()) {
				//throw new IOException("Unexpected code Springer API: " + response.code()+" "+response);
				//json.getJSONArray("result").getJSONObject(0).getString("total") );
				return res.put("result", new JSONArray().put(0, new JSONObject().put("total", "0")) );
			}
		
			res = new JSONObject(response.body().string());
			response.close();
			
		}catch (Exception  e) {
			System.err.println("Function :findSpringerPublication(), springer api error"+e.getMessage());
		}
		
		return res;
	}

	
	public JSONObject searchSpringerPublicationsByTitleDoi(Publications pub)
	{
		JSONObject res = new JSONObject();
		//String isbn = isbn_data.equals("") ? "0":isbn_data;//doc_isbn.equals("") ? "0" : pub.getIsbn();
		
		String doi = pub.extractDOI().equals("") ? "0" : pub.extractDOI();
		String title = pub.getTitle();
		title = title.endsWith(".") ? title.substring(0,title.length()-1):title;
		
		JSONArray records = new JSONArray();
		JSONObject element = new JSONObject();
		
		String params = "q=(title:"+analyseTitle(pub.getTitle());
		int total = 0;
		
		if (!doi.equals("0")){
			//añade doi a la busqueda
			//params += " OR doi:"+doi;
			params = "q=(title:"+analyseTitle(pub.getTitle())+" OR doi:"+doi+")";
		} else
			params = "q=title:"+analyseTitle(pub.getTitle());
		
		//if( !isbn_data.equals("")){
		//	params += " OR isbn:"+isbn_data;
		//}
		
		//params += ")"; 
		System.out.println("Springer Search Params: "+params);
		
		try 
		{
			JSONObject json = findSpringerPublication(params);
			res.put("api", "springer");
			
			if( Integer.parseInt( json.getJSONArray("result")
									.getJSONObject(0).getString("total") )  > 0) 
			{
				JSONArray results = json.getJSONArray("records");
				
				for (int i = 0; i < results.length(); i++) 
				{
					JSONObject rec = results.getJSONObject(i);
					
					String api_doi = rec.has("doi") ? rec.getString("doi") : "";
					//String api_isbn = rec.has("isbn") ? rec.getString("isbn") : "";
					String api_title = rec.has("title") ? rec.getString("title") : "";
					
					if( 
							api_title.equalsIgnoreCase(title) 
							|| api_doi.equalsIgnoreCase(doi)
							//|| api_isbn.equalsIgnoreCase(isbn)
						) 
					{
						total += results.length();
						String abstract_ = rec.has("abstract") ? rec.getString("abstract"):"";
						
						element.put("title", api_title);
						element.put("abstract", abstract_);
						element.put("authors", new JSONArray( obtainAuthors(rec) ));
						element.put("keywords", new JSONArray( obtainKeywords(json) ));
						element.put("doi", api_doi);
						//element.put("isbn", api_isbn);
						
					}
					else {
						res.put("results", 0);
					}
					
				}
				res.put("results", total);
				records.put(element);
				
			}else {
				res.put("results", 0);
			}
			
			res.put("records", records);
			
		} catch (JSONException | NullPointerException  e) {
			System.err.println("Function :springerPubliactions(), "+e.getMessage());
		}
		//System.out.println("Results Springer API:\n"+res.toString());
		return res;
	}
	

	public JSONObject searchSpringerPublicationByAuthorName(String name, int limit) {
		JSONObject res = new JSONObject();
		String nombre = name.replace(" ", "%20"); 
				
		setUrlBase("http://api.springernature.com/meta/v2/json?q=name:"+nombre+"&s=1&p="+limit+this.api_key);
		Request request = new Request.Builder()
				.url(this.urlBase)
				.method("GET", null)
				.build();
		
		try(Response response = client.newCall(request).execute()){
			if (!response.isSuccessful()) {
				return res.put("result", new JSONArray().put(0, new JSONObject().put("total", "0")) );
			}
		
			res = new JSONObject(response.body().string());
			response.close();
		}
		catch(IOException e) {
			System.err.println("Function searchSpringerPublicationByAuthorName(), "+e.getMessage());
		}
		return res;
	}
	
	//añade " ' " al inicio y final del titulo de la publicacion
	public String analyseTitle(String title)
	{
//		SpecialCharacter characters  = new SpecialCharacter();
//		int tam = title.length();
//		String res = title.endsWith(".") ? "%27"+title.substring(0,tam - 1)+"%27" : "%27"+title+"%27";
//		
//		res = characters.replaceSpecialCharacters(res);
//
//		return res;
		
		try {
			String value = title.endsWith(".")?"'"+title.substring(0,title.length() - 1)+"'":"'"+title+"'";
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
        	System.err.println("Function: analyseTitle(),"+title);
            throw new RuntimeException(ex.getCause());
        }
	}
	
	//json->elemento record
	public List<String> obtainAuthors(JSONObject json)
	{
		List<String> res = new ArrayList<>();
		try {
			JSONArray creators = json.has("creators") ? json.getJSONArray("creators") : new JSONArray();
			creators.iterator().forEachRemaining(c->{
				res.add( reverseAuthorNames( ((JSONObject) c).getString("creator")) ); 
			});
			
			
		}catch (JSONException e) {
			System.err.println("Function : obtainAuthors(),"+e.getMessage());
			e.printStackTrace();
		}
		return res;
	}
	
	//json -> jsonrespuesta del api
	public List<String> obtainKeywords(JSONObject json)
	{
		List<String> res = new ArrayList<>();
		try {
//			JSONArray facets = json.getJSONArray("facets");
//			
//			facets.iterator().forEachRemaining(facet->{
//				if( ((JSONObject) facet).getString("name").equals("keyword") ) {
//					
//					((JSONObject) facet).getJSONArray("values")
//					.iterator().forEachRemaining( keywords -> {
//						
//						res.add( ((JSONObject) keywords).getString("value") );
//					});
//				}
//			});
			
			JSONArray keywords = json.getJSONArray("keyword");
			
			keywords.iterator().forEachRemaining(key->{
				res.add(key.toString());
			});
			
		} catch (JSONException e) {
			System.err.println("Function: obtainKeywords(), "+e.getMessage());
			e.printStackTrace();
		}
	
		return res;
	}
	

	public String reverseAuthorNames(String names)
	{
		String res = "";
		if(names.contains(",")) 
		{
			List<String> lista = Arrays.asList(names.split(","));
			Collections.reverse(lista);
			for (String cad : lista) {
				res += cad+" ";
			}
			return res.substring(0, res.length()-1);
		}else
			return names;
	}
		
	public String getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}
	
	public String getKey() {
		return api_key;
	}
	public void setKey(String key) {
		this.api_key = key;
	}
	/*
	 * @Param: jsonarray [{key:"","has_doi":boolean,"doi":"","has_isbn":boolean,"isbn":""}]
	 */
	public String getPublicationUpdateParameters(JSONArray jsonarray) {
		String params = "";
		List<String> doi = new ArrayList<String>();
		List<String> isbn = new ArrayList<String>();
		
		
		for (int i = 0; i < jsonarray.length(); i++) {
			JSONObject json = jsonarray.getJSONObject(i);
			
			if(json.getBoolean("has_doi") && json.getBoolean("has_isbn"))
				doi.add( json.getString("doi")+" OR isbn:"+json.getString("isbn"));
			else {
				if(json.getBoolean("has_doi"))
					doi.add(json.getString("doi"));
				else if( json.getBoolean("has_isbn"))
					isbn.add(json.getString("isbn"));
			}
		}
		
		if(!doi.isEmpty()) {
			params += "doi:"+doi.get(0);
			for (int i = 1; i < doi.size(); i++)
				params += " OR doi:"+doi.get(i);			
		}
		
		if(!isbn.isEmpty()) {
			params = doi.isEmpty() ? "isbn:"+isbn.get(0) : " OR isbn:"+isbn.get(0);
			for (int i = 1; i < isbn.size(); i++)
				params += " OR isbn:"+isbn.get(i);
		}

		params = params.isEmpty() ? "" : "q=("+params+")" ;
		
		return params;
	}
	
	
	public String obtainSpringerApiParam(List<Publications> publications) {
		String res = "";
		List<String> parametros = new ArrayList<String>();
		
		for (Publications publication : publications) {
			
			/* { "key" : String, dblp_key,
			 * 	"has_doi" : boolean,
			 * 	"doi" : String, publication doi,
			 * 	"has_isbn" : boolean,
			 * 	"isbn" : String, publication isbn
			 * }
			 */
			JSONObject json_param = this.pub_service.getParameterToApiUpdateMendeleySpringer(publication);
			
			
			if(json_param.getBoolean("has_doi") )
				 parametros.add( "doi:"+json_param.getString("doi") );
			
			if(json_param.getBoolean("has_isbn"))
					parametros.add( "isbn:"+json_param.getString("isbn") );
		}
		
		if(!parametros.isEmpty()) {
			res = parametros.get(0);
			
			for(int i=1;i<parametros.size();i++) 
				res += " OR "+parametros.get(i);
			
		}else
			return res = "";
		
		try {
			res = URLEncoder.encode(res,java.nio.charset.StandardCharsets.UTF_8.toString());
					
		}catch (Exception e) {
			System.err.println("Function obtainSpringerApiParam(), "+e.getMessage());
		}
		
		return "&p="+publications.size()+"&q=("+res+")";
	}

	
	public JSONObject searchPublicationByListParameters(JSONObject springer, List<String> params) {
		JSONObject response  = new JSONObject();
		
		try {
			JSONArray elements = springer.getJSONArray("records");
			
			for(int i =0;i<elements.length();i++) {
				JSONObject element = elements.getJSONObject(i);
				
				if(element.has("doi") 
						&& params.contains(element.getString("doi")) )
					return response = element;
				
				if(element.has("isbn") 
						&& params.contains(element.getString("isbn")) )
					return response = element;
			}
		} catch (Exception e) {
			System.err.println("Function searchPublicationByListParameters(), "+e.getMessage());
		}
		return response;
	}
}