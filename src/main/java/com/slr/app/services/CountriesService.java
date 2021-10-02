package com.slr.app.services;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.helpers.SlrHibernateLuceneIndex;
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
	private SlrHibernateLuceneIndex lucene_index;
	
	public Countries findById(Long id){
		Optional<Countries> res = this.country_repo.findById(id);
		
		if(!res.isPresent())
			throw new RuntimeException("Country id: "+id+" does not exists");
		
		
		return res.get();
	}
	
	public Countries getDefaultCountrie() {
		return findById(Long.valueOf(0));
	}
	
	public Countries save(Countries countries) {
		return this.country_repo.save(countries);
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
				
				this.country_repo.save(new Countries(0, 
						((JSONObject) country).getString("name"),
						((JSONObject) country).getString("alpha2Code"), 
						new Date(),
						((JSONObject) country).getString("alpha3Code"),
						null));
				
			});
			
			response.close();
			
		} catch (JSONException e) 
		{
			System.out.println("Error json -> "+e.getMessage());
			e.printStackTrace();
		}
		
	}


	public Countries getCountrieOrDefault(String country_name) {
		List<Countries> list = this.lucene_index.findCountriesByName(country_name, 1);
		
		if(list.isEmpty())
			return getDefaultCountrie();

		return list.get(0);
	}
}
