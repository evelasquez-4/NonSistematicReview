package com.slr.app.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.slr.app.models.AuthorPublications;

@Component
public class AuthorPublicationsIndexedQueries {
	
	private static final int default_cant = 20;

	@Autowired
	private EntityManager entityManager;
	
	
	@Transactional
	public List<AuthorPublications> findIndexedCountries(String text, Integer ...limit){
		SearchSession searchSession = Search.session(this.entityManager);
		try{
			List<AuthorPublications> results = searchSession.search(AuthorPublications.class)
					.where(x -> x.bool()
								
								.should(x.match().field("authors.organizationses.countries.countryName").matching(text))
								.should(x.match().fields("authors.organizationses.countries.code","authors.organizationses.countries.codeAlpha").matching(text))
								
							).fetchHits(limit[0] > 0 ? limit[0] : default_cant);
			
			return results;
		} catch (Exception e) {
			System.out.println("Function findIndexedCountries(), "+e.getMessage());
		}
		return new ArrayList<AuthorPublications>();
	}
	
	@Transactional
	public List<AuthorPublications> getIndexedPublicationAuthorsFromAuthorPublications(String search_text, Integer ...limit){
		
		//List<Publications> res = new ArrayList<Publications>();
		SearchSession searchSession = Search.session(this.entityManager);
		System.out.println("Params: "+search_text+"\nLimit:"+limit[0]);
		try {
			List<AuthorPublications> results = searchSession.search(AuthorPublications.class)
					.where(x -> x.bool()
							.should(x.phrase().field("authors.names").matching(search_text))
							.should(x.match().field("authors.names").matching(search_text))
					).fetchHits(limit.length > 0 ? limit[0] : default_cant);
			
//			for (AuthorPublications ap : results) 
//				res.add(ap.getPublications());
			
//			return res.stream().distinct().collect(java.util.stream.Collectors.toList());
			
			return results;
			
		} catch (Exception e) {
			System.out.println("Function getIndexedPublicationAuthorsFromAuthorPublications(), "+e.getMessage());
		}
		
		return new ArrayList<AuthorPublications>();
	}
	
	@Transactional
	public List<AuthorPublications> findIndexedAuthorFromDblpKey(String text, Integer ...limit){
		SearchSession searchSession = Search.session(this.entityManager);
		
		try {
			List<AuthorPublications> results = searchSession.search(AuthorPublications.class)
					.where( x -> x.match().field("publications.dblpKey").matching(text))
					.fetchHits(limit.length > 0 ? limit[0] :default_cant);
			return results;
		} catch (Exception e) {
			System.out.println("Function findIndexedAuthorFromDblpKey(), "+e.getMessage());
		}
		return new ArrayList<AuthorPublications>();
	}
	
	
	@Transactional
	public List<AuthorPublications> findIndexedOrganizations(String search_text, Integer ...limit){
//		SearchSession searchSession = Search.session(this.entityManager);
//		try {
//			
//			List<AuthorPublications> results = searchSession.search(AuthorPublications.class)
//					.where( x->x.bool()
//								.should(x.phrase()
//										.field("authors.organizationses.description")
//										.matching(search_text).slop()
//										)
//							)
//					.fetchHits(limit[0] > 0 ? limit[0]:default_cant);
//			
//		} catch (Exception e) {
//			System.out.println("Function findIndexedOrganizations(), "+e.getMessage());
//		}
		
		return new ArrayList<AuthorPublications>();
	}
}
