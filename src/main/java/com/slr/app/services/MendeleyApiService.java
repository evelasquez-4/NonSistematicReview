package com.slr.app.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.slr.app.models.Publications;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


@Service
public class MendeleyApiService{
	
	private String urlBase = "https://api.mendeley.com/";
	private OkHttpClient client = new OkHttpClient
			.Builder()
			.connectTimeout(60,TimeUnit.SECONDS)
			.writeTimeout(60, TimeUnit.SECONDS)
			.readTimeout(60, TimeUnit.SECONDS)
			.build();
	private String key = "";
		
	
	public JSONObject searchMendeleyPublicationsByTitleDOI(Publications pub,String key) {
		
		this.key = key;
		
		JSONObject res = new JSONObject();
		String doi = pub.extractDOI(); 
		
		//String isbn = pub.getIsbn();
		JSONArray array = new JSONArray();
		
		JSONObject element = new JSONObject();
		JSONArray records = new JSONArray();
		
		String titulo = analyseTitle(pub.getTitle());
		//titulo =titulo.endsWith(".")?titulo.substring(0,titulo.length()-1):titulo;
		String pub_doi = pub.extractDOI().isEmpty()? "0" : pub.extractDOI();
		
		try
		{
			if(!doi.equals("") ) {
				array = findMendeleyPublicationByDOI(doi);
			}
			
			
			if(array.length() == 0) {
				//SpecialCharacter characters  = new SpecialCharacter();
				//String title = pub.getTitle();
				//title = title.endsWith(".") ? title.substring(0,title.length()-1) : title;
				//title = characters.replaceSpecialCharacters(title);
				
				
				array = findMendeleyPublicationByTitle(titulo);
			}
			
			res.put("api", "mendeley");
			
			if(array.length() > 0)
			{
				for (int i = 0; i < array.length(); i++) 
				{
					JSONObject json = array.getJSONObject(i);
		
					String api_title = json.has("title")? json.getString("title"):"";
					String api_doi = "";
					
					if(json.has("identifiers")){
						JSONObject obj = json.getJSONObject("identifiers");
						api_doi = obj.has("doi") ? obj.getString("doi") : "";
						//api_isbn = obj.has("isbn") ? obj.getString("isbn"): "";
					}
					
					String abstract_ = json.has("abstract") ? json.getString("abstract") : "";
					List<String> authors = json.has("authors") ? obtainAuthors(json):new ArrayList<String>();
					List<String> keywords = json.has("keywords")?obtainKeywords(json):new ArrayList<String>();
					
					if(	titulo.equalsIgnoreCase(api_title) 
							|| pub_doi.equalsIgnoreCase(api_doi) ) 
					{					
						res.put("results", 1);
						
						element.put("title", api_title);
						element.put("abstract", abstract_);
						element.put("authors", new JSONArray( authors ));
						element.put("keywords", new JSONArray( keywords ));
						element.put("doi", api_doi);
						//element.put("isbn",api_isbn);
						
						break;
					}
					else {
						res.put("results", 0);
					}
				}
				
				records.put(element);
				
			}else
			{
				res.put("results", 0);
			}
			
			res.put("records", records);
			
		}catch (Exception e) {
			System.err.println("Function : searchMendeleyPublicationsByTitleDOI(),"+e.getMessage());
		}
		
		//LOG.info("Results Mendeley API:\n"+res.toString());
		return res;
	}
	

	//begin mendeley api functions
	public List<String> obtainAuthors(JSONObject json)
	{
		List<String> autores = new ArrayList<String>();
		if(json.has("authors"))
		{
			json.getJSONArray("authors").iterator().forEachRemaining(author->{
				//String auth = ((JSONObject) author).getString("first_name")+" "+((JSONObject) author).getString("last_name");
				String first_name = ((JSONObject) author).has("first_name") ? ((JSONObject) author).getString("first_name") : "";
				String last_name = ((JSONObject) author).has("last_name") ? ((JSONObject) author).getString("last_name") : "";
				
				autores.add(first_name+" "+last_name);
			});
		}
		return autores;
	}
	
	public Map<String,String> obtainIdentifiers(JSONObject json){
		Map<String,String> identifiers = new HashMap<String, String>();
		if(json.has("identifiers")) {
			JSONObject obj = json.getJSONObject("identifiers");
			obj.keySet().iterator().forEachRemaining(id->{
				identifiers.put( id, obj.getString(id) );
			});
		}
		return identifiers;
	}
	
	public List<String> obtainKeywords(JSONObject json)
	{
		List<String> res = new ArrayList<String>();
		if(json.has("keywords"))
		{
			json.getJSONArray("keywords").iterator().forEachRemaining(key->{
				res.add(key.toString());
			});
		}
		return res;
	}
	//end mendeley api functions
	
	public JSONArray findMendeleyPublicationByDOI(String doi) throws Exception
	{
		setUrlBase("https://api.mendeley.com/catalog?doi="+doi+"&limit=1");
		Request request = new Request.Builder()
				.url(this.urlBase)
				.method("GET", null)
				.addHeader("Authorization", "Bearer "+getKey())
				.addHeader("Accept", "application/vnd.mendeley-document.1+json")
				.build();
		
		JSONArray res = new JSONArray();
		try(Response response = client.newCall(request).execute())
		{
			if (!response.isSuccessful()) 
				throw new IOException("Unexpected code  -> " + response.code()+" "+response);
		
			
			res = new JSONArray(response.body().string());			
			response.close();
			
		} catch (JSONException e) 
		{
			System.err.println("Function: findMendeleyPublicationByDOI(), "+e.getMessage());
		}
		
		
		return res;
	}
	
	public JSONArray findMendeleyPublicationByTitle(String title)
	{
		
		setUrlBase("https://api.mendeley.com/search/catalog?title="+ title +"&limit=5");
		Request request = new Request.Builder()
				.url( this.urlBase )
				.method("GET", null)
				.addHeader("Authorization", "Bearer "+getKey())
				.addHeader("Accept", "application/vnd.mendeley-document.1+json")
				.build();
		
		JSONArray res = new JSONArray();
		
		try(Response response = client.newCall(request).execute())
		{
			if (!response.isSuccessful()) 
				throw new IOException("Unexpected code  -> " + response.code()+" "+response);
		
			
			res = new JSONArray(response.body().string());			
			
			response.close();
			
		} catch (JSONException | IOException  e) {
			System.err.println("Function: findMendeleyPublicationByDOI(), "+e.getMessage());
		}
		
		return res;
	}
	
	public JSONArray findMendeleyPublicationByISBN(String isbn)
	{
		setUrlBase("https://api.mendeley.com/catalog?isbn="+isbn+"&limit=1");
		
		Request request = new Request.Builder()
				.url(this.urlBase)
				.method("GET", null)
				.addHeader("Authorization", "Bearer "+getKey())
				.addHeader("Accept", "application/vnd.mendeley-document.1+json")
				.build();
		
		JSONArray res = new JSONArray();
		try(Response response = client.newCall(request).execute())
		{
			if (!response.isSuccessful()) 
				throw new IOException("Unexpected code  -> " + response.code()+" "+response);
		
			res = new JSONArray(response.body().string());			
			response.close();
			
		} catch (JSONException | IOException e) {
			System.err.println("Function: findMendeleyPublicationByISBN(), "+e.getMessage());
			
		}
		return res;
	}
	/*@param: name:String, author name.
	 * 		  limit:int, results
	 * @return JSONArray
	 * [{id: string , title :string,type:string,abstract:string, 	
		source :string ,year :integer ,authors :array 	
		identifiers : array , keywords :array ,link :string}]
	 */
	public JSONArray findMendeleyPublicationByAuthorName(String name,int limit, String mendeley_key) {
		JSONArray res = new JSONArray();
		setKey(mendeley_key);
		
		String name_param = name.replaceAll(" ", "+");
		setUrlBase("https://api.mendeley.com/search/catalog?author="+name_param+"&limit="+limit);
		
		Request request = new Request.Builder()
				.url(this.urlBase)
				.method("GET", null)
				.addHeader("Authorization", "Bearer "+getKey())
				.addHeader("Accept", "application/vnd.mendeley-document.1+json")
				.build();
		
		try(Response response = client.newCall(request).execute()){
			if (!response.isSuccessful()) 
				throw new IOException("Unexpected code  -> " + response.code()+" "+response);
			
			res = new JSONArray(response.body().string());			
			response.close();
		}catch (JSONException | IOException e) {
			System.err.println("Function: findMendeleyPublicationByAuthorName(), "+e.getMessage());
		}
		
		return res;
	}
	public String getUrlBase() {
		return urlBase;
	}

	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}


	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}
	
	
	public String updateMendeleyKey() {
		String response = null;
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
		
		@SuppressWarnings("deprecation")
		RequestBody body = RequestBody
				.create(mediaType, "username=7265&password=woXajqQD6KDf0ZJo&grant_type=client_credentials&scope=all");

		Request request  = new Request.Builder()
				.url("https://api.mendeley.com/oauth/token")
				.method("POST", body)				
				.addHeader("Authorization", "Basic NzI2NTp3b1hhanFRRDZLRGYwWkpv")
				.addHeader("Content-Type", "application/x-www-form-urlencoded")
				.build();
		
		try(Response resp = client.newCall(request).execute())
		{
			if (!resp.isSuccessful()) 
				throw new IOException("Unexpected code Springer API: " + resp.code()+" "+resp);
			response = new JSONObject(resp.body().string()).toString();
			System.out.println(response);
			resp.close();
		
		}catch (Exception  e) {
			System.out.println("Function: updateMendeleyKey(),"+e.getMessage());
		}

		return response;
	}
	
	public String analyseTitle(String title)
	{
		try {
			String value = title.endsWith(".")?"'"+title.substring(0,title.length() - 1)+"'":"'"+title+"'";
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
        	System.err.println("Function: analyseTitle(),"+title);
            throw new RuntimeException(ex.getCause());
        }
	}
	
	/*
	 * @params: JSONArray: array con resultados de busqueda,
	 * 			String doi, String isbn, String title
	 * @return: JSONObject,
	 * {  id, title, type (journal, book, generic, book_section, conference_proceedings, working_paper, report, web_page, thesis, magazine_article, statute, patent, newspaper_article, computer_program, hearing, television_broadcast, encyclopedia_article, case, film, bill.), 
	 *    profile_id, group_id, created,last_modified, abstract,source,year, authors::array,
	 *    identifiers::object, keywords::array,
	 *    pub_doi,pub_isbn,pub_title,pub_key,api
	 * }
	 */
	public JSONObject findPublicationFromJSONArray(JSONArray array, String pub_doi,
			String pub_isbn, String pub_title) {
		JSONObject res = new JSONObject();
		
		//criteria: ['doi','isbn','doi-isbn','title'] 
		String criteria = Objects.nonNull(pub_doi) ? "doi":null;
		if(Objects.nonNull(criteria) && Objects.nonNull(pub_isbn)) criteria = "doi-isbn";
		else if(Objects.nonNull(pub_isbn)) criteria = "isbn";
		else if(Objects.isNull(criteria)) criteria = "title";
		
		for (int i = 0; i < array.length(); i++) {
			
			JSONObject obj = array.getJSONObject(i);
			
			if(obj.has("identifiers")) {
				/*"identifiers":{ arxiv:String, doi: String, isbn:String, issn:String,
				 *  				pmid:String (PubMed), scopus:String ,ssrn:String	}
				 */
				JSONObject identifiers = obj.getJSONObject("identifiers");
				
				if(identifiers.has("doi") && criteria.equals("doi")) {
					res = identifiers.getString("doi").equalsIgnoreCase(pub_doi) ? obj : new JSONObject();
					break;
				}
				else if(criteria.equals("isbn") && identifiers.has("isbn")) {
					res = identifiers.getString("isbn").equalsIgnoreCase(pub_isbn) ? obj : new JSONObject();
					break;
				}
				else if(criteria.equals("doi-isbn")) {
					if(identifiers.has("doi")) { 
						res = identifiers.getString("doi").equalsIgnoreCase(pub_doi) ? obj : new JSONObject();
						break;
					}
					else if(identifiers.has("isbn")) {
						res = identifiers.getString("isbn").equalsIgnoreCase(pub_isbn) ? obj : new JSONObject();
						break;
					}
				}
				
			}else {//publication title compare
				String title = obj.has("title") ? analyseTitle(obj.getString("title")):null;
				pub_title = analyseTitle(pub_title);
				
				if(Objects.nonNull(title) &&
						pub_title.equalsIgnoreCase(title)) {
					res = obj;
					break;
				}
			}
		}
		
		
		return res;
	}
}
