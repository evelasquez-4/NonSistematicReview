package com.slr.app.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.util.common.SearchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.slr.app.models.AuthorPublications;
import com.slr.app.models.Authors;
import com.slr.app.models.Countries;
import com.slr.app.models.DblpPublications;
import com.slr.app.models.PublicationKeywords;
import com.slr.app.models.Publications;

@CrossOrigin
@Component
public class SlrHibernateLuceneIndex//implements ApplicationListener<ContextRefreshedEvent> 
{
	@Autowired
	private EntityManager entityManager;
	
	private Integer limit = 10;
	
	@Transactional
	public String indexEntityElasticSearch(String entity) {
		SearchSession searchSession = Search.session(entityManager);
		
		try {
			MassIndexer indexer = null;
			switch (entity) {
				case "countries":
					indexer = searchSession.massIndexer( Countries.class )
						.idFetchSize(150)
						.batchSizeToLoadObjects(25)
						.threadsToLoadObjects(12);
					indexer.startAndWait();
				break;
				case "dblp_publications":
					indexer = searchSession.massIndexer( DblpPublications.class )
							.idFetchSize(150)
							.batchSizeToLoadObjects(25)
							.threadsToLoadObjects(12);
					indexer.startAndWait();
				break;
				case "authors":
					indexer = searchSession.massIndexer( Authors.class )
							.idFetchSize(150)
							.batchSizeToLoadObjects(25)
							.threadsToLoadObjects(12);
					indexer.startAndWait();
				break;
				
				case "publications":
					indexer = searchSession.massIndexer( Publications.class ).idFetchSize(150)
							.batchSizeToLoadObjects(25)
							.threadsToLoadObjects(12);
					indexer.startAndWait();
				break;
				
				case "author_publications":
					indexer = searchSession.massIndexer( AuthorPublications.class ).idFetchSize(150)
							.batchSizeToLoadObjects(25)
							.threadsToLoadObjects(12);
					indexer.startAndWait();
				break;
				
				case "publication_keywords":
					indexer = searchSession.massIndexer( PublicationKeywords.class ).idFetchSize(150)
							.batchSizeToLoadObjects(25)
							.threadsToLoadObjects(12);
					indexer.startAndWait();
				break;
			}
			this.entityManager.close();
		} catch (InterruptedException e) {
			System.out.println("indexEntityElasticSearch"+e.getMessage());
		}
		
		return entity+" indexed.";
	}
	
	//search indexed country by name
	
	@Transactional
	public List<Countries> findCountriesByName(String country,Integer ... cant){
		SearchSession searchSession = Search.session(this.entityManager);
		try {
			
			SearchResult<Countries> result = searchSession.search(Countries.class)
					.where(c->c.bool()
							.should(c.match().field("country_name").matching(country))
							.should(c.match().fields("country_code","country_code_alpha").matching(country))
					)
					.fetch( cant.length > 0 ? cant[0].intValue() : limit);
			return result.hits();
		}catch(SearchException e){
			System.out.println("function: findCountriesByName() "+e.getMessage());
		}
		return new ArrayList<Countries>();
	}

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
	
	
/*
	@Transactional
	public List<Authors> findAuthorsIndexedByName(String names){
		
		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(this.entityManager);
		List<Authors> res = new ArrayList<Authors>();
		try {
			QueryBuilder qb = fullTextEntityManager
					.getSearchFactory()
					.buildQueryBuilder().forEntity(Authors.class).get();
			
			
			org.apache.lucene.search.Query lucene = qb
					.bool()
//					.should( qb.keyword().onField("names").ignoreFieldBridge().matching(names).createQuery()  )
					.should( qb.keyword().onField("homonyns").matching(names).createQuery() )
					.createQuery();
			
			javax.persistence.Query fullTextQuery = fullTextEntityManager
					 .createFullTextQuery(lucene,Authors.class).setMaxResults(50);
			
			res = fullTextQuery.getResultList();
			
			entityManager.close();
		}catch (Exception  e) {
			System.err.println("function findAuthorsIndexedByName(): "+e.getMessage());
		}
		
		return res; 
	}
*/
	@Transactional
	public List<Authors> findIndexedAuthorsByNamesHomonyns(String author,String table, Integer ... cant){

		SearchSession searchSession = Search.session(this.entityManager);
		
		try {
			List<Authors> authors = new ArrayList<Authors>();
			switch (table) {
			case  "author_publications":
					System.err.println("Name: "+author+"\ntable: "+table);
					List<AuthorPublications> results = searchSession.search(AuthorPublications.class)
							.where(ap -> ap.bool() 
									.should(ap.phrase().field("authors.names").matching(author))
									.should(ap.match().field("authors.homonyns").matching(author))
							)
							.fetchHits( cant.length > 0 ? cant[0].intValue() : this.limit );
					
					if(results.isEmpty()) {
						
						results = searchSession.search(AuthorPublications.class)
								.where(ap->ap.bool()
										.should(ap.simpleQueryString().field("authors.names").matching("*"+author))
										.should(ap.wildcard().field("authors.homonyns").matching(author+"*"))
								).fetchHits(cant.length > 0 ? cant[0].intValue() : this.limit );
					}
						
					
					for (AuthorPublications ap : results) 
						authors.add(ap.getAuthors());

			break;
			
			case "authors":
				System.err.println("Name: "+author+"\n table: "+table);
				SearchResult<Authors> resp = searchSession.search(Authors.class)
				.where(auth -> auth.bool()
							.should(auth.match()
									.field("author_names")
									.matching(author))
							.should(	auth.match()
										.field("author_homonyns")
										.matching(author) )
						)
				.fetch( cant.length > 0 ? cant[0].intValue() : limit );
				authors = resp.hits();
				
			break;
			}
			
			return  authors.stream().distinct().collect(java.util.stream.Collectors.toList());
			
		} catch (Exception e) {
			System.err.println("function findIndexedAuthorsByName(): "+e.getMessage());
			throw new RuntimeException("function findIndexedAuthorsByName(): "+e.getMessage());
		}
	}
	
	/*
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
					 .createFullTextQuery(lucene,DblpPublications.class);
			
			res = fullTextQuery.getResultList();
			
		}
		catch (EmptyQueryException  e) {
			System.err.println("function findDblpByCrossref(): "+e.getMessage());
		}
		entityManager.close();
		return res;
	}
	*/	
	public List<DblpPublications> findIndexedDblpPublicationByCrossref(String key){
		
		SearchSession searchSession = Search.session(this.entityManager);
		try{
			SearchResult<DblpPublications> results = searchSession
					.search(DblpPublications.class)
					.where( data -> data.match()
							.field("crossref")
							.matching(key)
							)
					.fetchAll();
			
			return results.hits();
			
		} catch (SearchException e) {
			System.err.println("function findIndexedDblpPublicationByCrossref(): "+e.getMessage());
		}
		return new ArrayList<DblpPublications>();
	}
	
//	@Transactional
//	public List<DblpPublications> findNoProceedingReferences(List<DblpPublications> dblp){
//		List<DblpPublications> res = new ArrayList<DblpPublications>();
//		
//		SearchSession searchSession = Search.session(this.entityManager);
//		try{
//			
//			for (DblpPublications publication : dblp) {
//				
//				SearchResult<DblpPublications> results = searchSession
//						.search(DblpPublications.class)
//						.where( data -> data.match()
//								.field("crossref")
//								.matching(publication.getKeyDblp())
//								)
//						.fetch(1);
//				
//				if(results.total().hitCount() < 1)
//					res.add(publication);
//			}
//			
//		} catch (Exception e) {
//			System.err.println("function findIndexedDblpPublicationByCrossref(): "+e.getMessage());
//		}
//		
//		
//		return res;
//	}
	
	/*
	 * Author_publications queries
	 *
	 */
	
	public List<AuthorPublications> findPublicationsByTitleAbstract(String text, Integer ... limit){
		SearchSession searchSession = Search.session(this.entityManager);
		
		try {
			List<AuthorPublications> results = searchSession.search(AuthorPublications.class)
					.where(x -> x.nested().objectField("publications")
							.nest( x.bool()
									.should(	x.phrase()
												.field("publications.title")
												.matching(text)
											)
									.should(	x.phrase()
												.field("publications.abstract_")
												.matching(text)
											)
								)
							)
					.fetchHits( limit.length < 1 ? this.limit : limit[0].intValue() );
			
			if(results.isEmpty()) {
				results = searchSession.search(AuthorPublications.class)
						.where(x -> x.bool()
								.should(x.simpleQueryString().fields("publications.title","publications.abstract_").matching("*"+text+"*"))
								)
						.fetchHits( limit.length < 1 ? this.limit : limit[0].intValue() );
			}
			
		
			return results;
		} catch (Exception e) {
			System.out.println("function findPublicationsByTitleAbstract(), "+e.getMessage());
		}
		return new ArrayList<AuthorPublications>();
	}
	
	//fulltextfield in publications [ ee, rgInfo, abstract, title ]
	//keyword field in publications [ dblpKey, crossref] 
	@Transactional
	public List<AuthorPublications> findIndexedPublicationsByFieldName(String text, String field, Integer ... cant)
	{
		SearchSession searchSession = Search.session(this.entityManager);
		this.limit = cant.length > 0 ? cant[0].intValue(): this.limit;
		
		try {
			List<AuthorPublications> response = searchSession.search(AuthorPublications.class)
					.where( x-> x.nested().objectField("publications")
							.nest( x.bool(  y->{
									if(field.equalsIgnoreCase("ee") || field.equalsIgnoreCase("doi")) {
										y.should( x.match().field("publications.ee").matching(text));
										//.should(x.wildcard().field("publications.ee").matching("http*/"+text));
									}
									else if(field.equalsIgnoreCase("rginfo")) 
										y.should( x.wildcard().field("publications.rgInfo").matching("*"+text+"*"));
									
									else if(field.equalsIgnoreCase("dblpKey"))
										y.should(x.match().field("publications.dblpKey").matching(text));
									
									else if(field.equalsIgnoreCase("crossref"))
										y.should(x.match().field("publications.crossref").matching(text));

									else
										System.out.println( "Function findIndexedPublicationsByFullTextFields(), field "+field+" not found as fulltext.");
							}) )
					).fetchHits(this.limit);
			
			return response;
		} catch (Exception e) {
			System.out.println("function findIndexedPublicationsByFieldName(), "+e.getMessage());
		}
		return new ArrayList<AuthorPublications>();
	}
	
	@Transactional
	public List<AuthorPublications> findIndexedPublicationsByNestedTypeDocument( String text, String document, String field_document, Integer ...cant){
		SearchSession searchSession = Search.session(this.entityManager);
		this.limit = cant.length > 0 ? cant[0].intValue(): this.limit;
		//List<String> documents = Arrays.asList("article","inproceedings","proceedings","book","incollection");
		try{
			List<AuthorPublications> response = searchSession.search(AuthorPublications.class)
					.where(x -> x.nested().objectField("publications")
						.nest( x.bool( y->{
							if(document.equalsIgnoreCase("book")) {
								if(field_document.equalsIgnoreCase("bookTitle"))
									y.should(x.match().field("publications.books.bookTitle").matching(text));
								else if(field_document.equalsIgnoreCase("series"))
									y.should(x.match().field("publications.books.series").matching(text));
								else if(field_document.equalsIgnoreCase("isbn"))
									y.should(x.match().field("publications.books.isbn").matching(text));
								else
									System.out.println( "Function findIndexedPublicationsByNestedTypeDocument(), field "+field_document+" not found as fulltext.");
							}
							else if(document.equalsIgnoreCase("incollection")) {
								if(field_document.equalsIgnoreCase("bookTitle"))
									y.should(x.match().field("publications.bookChapters.bookTitle").matching(text));
								else if(field_document.equalsIgnoreCase("isbn"))
									y.should(x.match().field("publications.bookChapters.isbn").matching(text));
								else if(field_document.equalsIgnoreCase("series"))
									y.should(x.match().field("publications.bookChapters.series").matching(text));
								else
									System.out.println( "Function findIndexedPublicationsByNestedTypeDocument(), field "+field_document+" not found as fulltext.");
							}
							else if(document.equalsIgnoreCase("article")) {
								if(field_document.equalsIgnoreCase("bookTitle"))
									y.should(x.match().field("publications.journalPapers.bookTitle").matching(text));
								else
									System.out.println( "Function findIndexedPublicationsByNestedTypeDocument(), field "+field_document+" not found as fulltext.");
							}
							else if(document.equalsIgnoreCase("inproceedings")) {
								if(field_document.equalsIgnoreCase("bookTitle"))
									y.should(x.match().field("publications.conferencePapers.bookTitle").matching(text));
								else
									System.out.println( "Function findIndexedPublicationsByNestedTypeDocument(), field "+field_document+" not found as fulltext.");
							}
							else if(document.equalsIgnoreCase("proceedings")) {
								//	field_document[ 'conferenceEditorials.isbn', 'conferenceEditorials.bookTitle','journalEditorials.isbn','journalEditorials.bookTitle']
								if(field_document.equalsIgnoreCase("conferenceEditorials.isbn"))
									y.should(x.match().field("publications.conferenceEditorials.isbn").matching(text));
								
								else if(field_document.equalsIgnoreCase("conferenceEditorials.bookTitle"))
									y.should(x.match().field("publications.conferenceEditorials.bookTitle").matching(text));
								
								else if(field_document.equalsIgnoreCase("journalEditorials.isbn"))
									y.should(x.match().field("publications.journalEditorials.isbn").matching(text));
								
								else if(field_document.equalsIgnoreCase("journalEditorials.bookTitle"))
									y.should(x.match().field("publications.journalEditorials.bookTitle").matching(text));
								
								else
									System.out.println( "Function findIndexedPublicationsByNestedTypeDocument(), field "+field_document+" not found as fulltext.(doc_type -> proceedings)");
							}
							else
								System.out.println( "Function findIndexedPublicationsByNestedTypeDocument(), document "+document+" not found as fulltext.");
								
						}) )
					).fetchHits(this.limit);
			
			return response;
		}catch (Exception e) {
			System.out.println("function findIndexedPublicationsByNestedTypeDocument(), "+e.getMessage());
		}
		return new ArrayList<AuthorPublications>();
	}
}
