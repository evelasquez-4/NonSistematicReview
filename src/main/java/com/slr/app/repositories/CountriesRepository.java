package com.slr.app.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.jpa.repository.Query;

import com.slr.app.models.Countries;

@RepositoryRestResource
public interface CountriesRepository extends JpaRepository<Countries, Long>{
	
	@Query("FROM Countries WHERE LOWER(country_name) = LTRIM( (LOWER(?1)) )")
	public Optional<Countries> findCountryByName(String name);

	@Query("FROM Countries WHERE code = ?1")
	public Optional<Countries> findCountryByCode(String code);


}
