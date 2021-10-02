package com.slr.app.helpers;

import java.util.List;


import javax.persistence.EntityManager;

import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.slr.app.models.DblpPublications;

@Component
public class DblpPublicationsQueries {

	@Autowired
	private EntityManager entityManager;
	private final int cant = 10;
	
	@Transactional
	public List<DblpPublications> findIndexedDblpByAuthorNames(String names, Integer...limit ){
		
		SearchSession searchSession = Search.session(this.entityManager);
		try{
			
//			SearchResult<DblpPublications> results = searchSession.search(DblpPublications.class)
//					.where(data->data.bool()
//							//.should(data.phrase().field("author").matching(names))
//							.should(data.match().field("author").matching(names))
//							.should(data.wildcard().field("author").matching("*"+names+"*"))
//							)
//					.fetch(limit.length > 0 ? limit[0] : cant);
			SearchResult<DblpPublications> results = searchSession.search(DblpPublications.class)
					.where(f -> f.match().field("author").matching(names))
					.fetch(limit.length > 0 ? limit[0] : cant);
			
			if(results.total().hitCount() < 1) {
				
				results = searchSession.search(DblpPublications.class)
						.where(d->d.wildcard().field("author").matching("*"+names+"*"))
						.fetch(limit.length > 0 ? limit[0] : cant);
			}
			
					
			return results.hits();
			
		}
		catch (Exception e) {
			System.err.println("function findIndexedAuthorsByName(): "+e.getMessage());
			return null;
		}
	}
	
	@Transactional
	public List<DblpPublications> findIndexedDblpBykeyDblp(String text,Integer ...limit){
		SearchSession searchSession = Search.session(this.entityManager);
		
		try{
			
			SearchResult<DblpPublications> results = searchSession.search(DblpPublications.class)
					.where(data->data
							.match()
							.field("keyDblp").matching(text)
							)
					.fetch(limit.length > 0 ? limit[0] : cant);
			
			return results.hits();
			
		}
		catch (Exception e) {
			System.err.println("function findIndexedAuthorsByName(): "+e.getMessage());
			return null;
		}
	}
}
