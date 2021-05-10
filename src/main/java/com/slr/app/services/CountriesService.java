package com.slr.app.services;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.models.Countries;
import com.slr.app.repositories.CountriesRepository;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class CountriesService {

	@Autowired
	private CountriesRepository country_repo;
	@Autowired
	private EntityManager em;
	
	public Countries findById(Long id){
		Optional<Countries> res = this.country_repo.findById(id);
		
		if(!res.isPresent())
			throw new RuntimeException("Country id: "+id+" does not exists");
		
		
		return res.get();
	}
	
	@SuppressWarnings("static-access")
	@Transactional
	public String indexCountriesTable() {
		try {
			FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(this.em);
			fullTextEntityManager.createIndexer(Countries.class).startAndWait();

			
		} catch (InterruptedException e) {
			System.err.println("function indexEntity(): "+e.getMessage());
		}
		return "Entity: Countries, was indexed.";
	}
	
	
	public void loadCountriesFromAPI(String continent) throws Exception
	{
		OkHttpClient client = new OkHttpClient().newBuilder().build();
		Request request = new Request.Builder()
				.url("https://restcountries.eu/rest/v2/region/"+continent)
				.get()
				.build();
		
		JSONArray json = new JSONArray();
		
		try(Response response = client.newCall(request).execute())
		{
			if (!response.isSuccessful()) 
				throw new IOException("Unexpected code  -> " + response.code()+" "+response);
			
			json = new JSONArray(response.body().string());
			json.iterator().forEachRemaining(country->{
				
				Countries c = new Countries();
				String name = ((JSONObject) country).getString("name");
				String code = ((JSONObject) country).getString("alpha2Code");
				
				c.setCountryName( new String(name.getBytes(Charset.forName("ISO-8859-1")), Charset.forName("UTF-8")));
				c.setCode(new String(code.getBytes(Charset.forName("ISO-8859-1")),Charset.forName("UTF-8")));
				c.setCreatedAt(new Date());
				
				this.country_repo.saveAndFlush(c);
				
			});
			
			response.close();
			
		} catch (JSONException e) 
		{
			System.out.println("Error json -> "+e.getMessage());
			e.printStackTrace();
		}
		
	}
}
