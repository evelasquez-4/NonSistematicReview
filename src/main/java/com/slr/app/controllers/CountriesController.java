package com.slr.app.controllers;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slr.app.models.Countries;
import com.slr.app.services.CountriesService;

@RestController
@RequestMapping("/countries")
public class CountriesController {
	
	@Autowired
	private CountriesService country_serv;
	
	@GetMapping("/find/{id}")
    public Countries getPublicationById(@PathVariable("id") Long id) {
        return this.country_serv.findById(id);
    }
	
	// Search by region: africa, americas, asia, europe, oceania
	//https://restcountries.eu/#api-endpoints-region
	//curl --location --request POST 'http://localhost:8081/countries/load_countries' --header 'Content-Type: application/json' --data-raw '{"continent":"americas"}'
	@PostMapping(value = "/load_countries", produces = MediaType.APPLICATION_JSON_VALUE)
	public void loadCountries(
	@RequestBody(required = true) Map<String, String> values) throws IOException, Exception
	{
		String region = values.containsKey("continent")?values.get("continent"):"";
		this.country_serv.loadCountriesFromAPI(region);
	}
	
	@GetMapping("/search_country")
	public Countries getCountrieOrDefault(@RequestBody(required = true) Map<String, String> values){
		
		return this.country_serv.getCountrieOrDefault(values.get("country_name"));
	}

}
