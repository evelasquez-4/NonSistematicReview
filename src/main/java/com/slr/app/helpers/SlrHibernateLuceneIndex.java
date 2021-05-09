package com.slr.app.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.hibernate.search.exception.EmptyQueryException;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import com.slr.app.models.Authors;
import com.slr.app.models.Countries;
import com.slr.app.models.DblpPublications;
import com.slr.app.models.Editions;
import com.slr.app.models.Publishers;
import com.slr.app.services.AuthorsService;
import com.slr.app.services.SlrConfigurationService;

@Component
public class SlrHibernateLuceneIndex//implements ApplicationListener<ContextRefreshedEvent> 
{
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private AuthorsService authors_service;
	@Autowired
	private SlrConfigurationService configuration;
	
	
	
//	@Override
//	@Transactional
//	public void onApplicationEvent(ContextRefreshedEvent event) {
//		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
//		try {
//			fullTextEntityManager.createIndexer().startAndWait();
//		} catch (InterruptedException e) {
//			System.out.println("Error occured trying to build Hibernate Search indexes "
//					+ e.toString());
//		}
//	}
	
	@Transactional
	public String indexEntity(String entity) {
		FullTextEntityManager fullTextEntityManager  = null;
		try 
		{
			fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
			switch (entity) 
			{
				case "authors":
					fullTextEntityManager.createIndexer(Authors.class).startAndWait();
				break;
				case "dblp_publications":
					fullTextEntityManager.createIndexer(DblpPublications.class).startAndWait();
				break;
				case "countries":
					fullTextEntityManager.createIndexer(Countries.class).startAndWait();
				break;
				case "editions":
					fullTextEntityManager.createIndexer(Editions.class).startAndWait();
					break;
				case "publishers":
					fullTextEntityManager.createIndexer(Publishers.class).startAndWait();
					break;
				default:
					throw new NullPointerException("Error verificar el nombre de la clase a indexar: "+entity);
			}

		} catch (InterruptedException e) {
			System.out.println("Error occured trying to build Hibernate Search indexes "
					+ e.toString());
		}
		return "Successful entity indexed: "+entity;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public List<Authors> findAuthorsIndexedByName(String names){
		
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(this.entityManager);
		List<Authors> res = new ArrayList<Authors>();
		try {
			QueryBuilder qb = fullTextEntityManager
					.getSearchFactory()
					.buildQueryBuilder().forEntity(Authors.class).get();
			
			/*
			org.apache.lucene.search.Query lucene = qb.phrase()
					.withSlop(0)
					.onField("names")
					.sentence(names)
					.createQuery();
			
			org.apache.lucene.search.Query lucene = qb
					.keyword()
					.onFields("names","homonyns")
					.matching(names)
					.createQuery();
			*/
			org.apache.lucene.search.Query lucene = qb.bool()
					.should( qb.keyword().onField("names").ignoreFieldBridge().matching(names).createQuery()  )
					.should( qb.keyword().onField("homonyns").ignoreFieldBridge().matching(names).createQuery() )
					.createQuery();
			
			javax.persistence.Query fullTextQuery = fullTextEntityManager
					 .createFullTextQuery(lucene,Authors.class);
			
			res = fullTextQuery.getResultList();
			
			entityManager.close();
		}catch (EmptyQueryException  e) {
			System.err.println("function findAuthorsIndexedByName(): "+e.getMessage());
		}
		
		return res;
	}
	
	@Transactional
	public List<Authors> findAuthorsIndexedByListAuthors(List<String> authors){
		
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(this.entityManager);
		List<Authors> res = new ArrayList<Authors>();
		try {
			QueryBuilder qb = fullTextEntityManager
					.getSearchFactory()
					.buildQueryBuilder().forEntity(Authors.class).get();
			
			for(String au : authors)
			{
				org.apache.lucene.search.Query lucene = qb.bool()
						.should( qb.keyword().onField("names").ignoreFieldBridge().matching(au).createQuery()  )
						.should( qb.keyword().onField("homonyns").ignoreFieldBridge().matching(au).createQuery() )
						.createQuery();
				
				javax.persistence.Query fullTextQuery = fullTextEntityManager
						 .createFullTextQuery(lucene,Authors.class);
				
				if( fullTextQuery.getResultList().isEmpty()) {
					//throw new IndexOutOfBoundsException("function findAuthorsIndexedByListAuthors() : name, "+au+" not found in slr.authors");
					System.err.println("function findAuthorsIndexedByListAuthors() : name, "+au+" not found in slr.authors");
					System.err.println("inserting author: "+au);
					int group = this.configuration.getValidateConfiguration("active").getGroupState();
					Authors author = new Authors();
					author.setNames(au);
					author.setCreatedAt(new Date());
					author.setGroup(group);
					
					res.add(this.authors_service.saveAuthors(author));
					
				}else
					res.add((Authors)fullTextQuery.getResultList().get(0));
			}
			
		}catch (EmptyQueryException  e) {
			System.err.println("function findAuthorsIndexedByName(): "+e.getMessage());
		}
		entityManager.close();
		return res;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	public List<DblpPublications> findIndexedDblpPublicationByCrossref(String dblpKey){
		List<DblpPublications> res = new ArrayList<DblpPublications>();
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(this.entityManager);
		
		try {
			QueryBuilder qb = fullTextEntityManager
					.getSearchFactory()
					.buildQueryBuilder().forEntity(DblpPublications.class).get();
			
			org.apache.lucene.search.Query lucene = qb
					.keyword()
					.onField("crossref")
					.matching(dblpKey)
					.createQuery();
			
			javax.persistence.Query fullTextQuery = fullTextEntityManager
					 .createFullTextQuery(lucene,Authors.class);
			
			res = fullTextQuery.getResultList();
			
		}
		catch (EmptyQueryException  e) {
			System.err.println("function findDblpByCrossref(): "+e.getMessage());
		}
		entityManager.close();
		return res;
	}
}
