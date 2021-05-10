package com.slr.app.services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.dblp.mmdb.Field;
import org.dblp.mmdb.PersonName;
import org.dblp.mmdb.Publication;
import org.dblp.mmdb.RecordDb;
import org.dblp.mmdb.RecordDbInterface;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.slr.app.config.DBConnect;
import com.slr.app.helpers.SlrHibernateLuceneIndex;
import com.slr.app.models.AuthorPublications;
import com.slr.app.models.Authors;
import com.slr.app.models.BookChapters;
import com.slr.app.models.Books;
import com.slr.app.models.ConferenceEditorials;
import com.slr.app.models.ConferencePapers;
import com.slr.app.models.Conferences;
import com.slr.app.models.DblpPublications;
import com.slr.app.models.Editions;
import com.slr.app.models.JournalEditorials;
import com.slr.app.models.JournalPapers;
import com.slr.app.models.Journals;
import com.slr.app.models.Publications;
import com.slr.app.models.Publishers;
import com.slr.app.repositories.DblpPublicationsRepository;

@Service
public class DblpPublicationsService {
	
	@Autowired
	private DblpPublicationsRepository dblp_repo;
	@Autowired
	private SlrConfigurationService configuration;
	
	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
	private int batchSize;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private AuthorPublicationsService authorPublication_service;
	@Autowired
	private PublishersService publisher_service;
	@Autowired
	private AuthorsService author_service;
	@Autowired
	private PublicationsServices publication_service;
	@Autowired
	private SlrHibernateLuceneIndex index_service;

	
	public DblpPublications getPublicationById(Long id){
		Optional<DblpPublications> res = this.dblp_repo.findById(id);
		if(res.isPresent()) 
			return res.get();
		else	
			throw new RuntimeException("DblpPublication id :"+id+" does not exists.");
	}
	
	public DblpPublications save(DblpPublications publication) {
		return this.dblp_repo.saveAndFlush(publication);
	}
	
	
	public void parseDblpFiles(String xmlFileName, String dtdFileName) 
	{
		int grupo = this.configuration.getValidateConfiguration("active").getGroupState();
		int inserts = 0;
		
		if(xmlFileName.isEmpty()  || dtdFileName.isEmpty())
			 throw new NullPointerException("Usage: java  <dblp-xml-file> <dblp-dtd-file>");
	         
		 System.out.println("building the dblp main memory DB ...");
		 RecordDbInterface dblp;
		 try { 
			   dblp = new RecordDb(xmlFileName, dtdFileName, false); }
		 catch (final IOException ex) {
			 System.err.println("cannot read dblp XML: " + ex.getMessage());
		     return;
		 }
		 catch (final SAXException ex) { 
			 System.err.println("cannot parse XML: " + ex.getMessage());
		     return;
		 }
		   
	    // System.out.format("MMDB ready: %d publs, %d pers\n\n", dblp.numberOfPublications(), dblp.numberOfPersons());
	    		 
	     List<String> authors = new ArrayList<>();
	     List<String> documents = Arrays.asList("article","inproceedings","proceedings","book","incollection");
	     List<DblpPublications> publicationsBatch = new ArrayList<>();
	      
	     for(Publication publication : dblp.getPublications()) 
	     {
	    	 //doc_type
	    	 String docType = publication.getTag();
	    	
	    	 if( documents.contains(docType)) 
	    	 {	 
		    	 //key_dblp -> attributes.get("key")
		    	 //mdate -> attributes.get("mdate")
		    	 Map<String, String> attributes = publication.getAttributes();
		    	 
		    	 authors = getListNames(publication.getNames());
		    	 
		    	 Map<String,String> fields = parseFields(publication.getFields());		    	 
		         Integer year = fields.get("year").trim().isEmpty() ? 0 : Integer.parseInt(fields.get("year"));
		        
		    	 //publicationsBatch.add
		    	 publicationsBatch.add( new DblpPublications(null,
		    			 attributes.get("key"),
		    			 authors,
		    			 fields.get("title"),
		    			 fields.get("book_title"), 
		    			 fields.get("pages"), 
		    			 year,
		    			 fields.get("crossref"), 
		    			 fields.get("address"),
		    			 fields.get("journal"),
		    			 fields.get("volume"), 
		    			 fields.get("number"),
		    			 fields.get("month"),
		    			 fields.get("url"), 
		    			 fields.get("ee"), 
		    			 fields.get("cdrom"), 
		    			 fields.get("cite"),
		    			 fields.get("publisher"),
		    			 fields.get("note"), 
		    			 fields.get("isbn"),
		    			 fields.get("series"), 
		    			 fields.get("school"), 
		    			 fields.get("chapter"),
		    			 fields.get("publnr"),
		    			 attributes.get("mdate"),
		    			 new Date(), 
		    			 "1.inserted", 
		    			 grupo, 
		    			 docType ) 
		    		);  
	    	  }
	    	 
	    	 if(  publicationsBatch.size() % this.batchSize  == 0) {
	    		 inserts += batchDblpPublicationInsert(publicationsBatch);
	    		 System.out.println("Rows inserted in slr.dblp_publications: "+inserts);
	    		 publicationsBatch.clear();
	    	 }
	     } 
	     
	     if(!publicationsBatch.isEmpty()) {
	    	 inserts += batchDblpPublicationInsert(publicationsBatch);
	    	 System.out.println("Rows inserted in slr.dblp_publications: "+inserts);
    		 publicationsBatch.clear();
	     }
	}
	
	
	public String getProceedingReference(String dblpKey) {
		String res = "";
		List<DblpPublications> dblp = this.index_service.findIndexedDblpPublicationByCrossref(dblpKey);
		
		if(dblp.isEmpty())
			throw new RuntimeException("function getProceedingReference() :"+dblpKey+" ,not found reference");
		
		switch (dblp.get(0).getDocType()) {
		case "incollection":
			res = "incollection"; break;
		case "article":
			res = "journal_editorial"; break;
		case "inproceedings":
			res = "conference_editorial"; break;
		default:
			System.err.println("Dblp proceeding reference error: "+dblpKey+" -> "+dblp.get(0).getDocType());
			break;
		}
		
		return res;
	}
	
	/*
	 * llamada a funcion para insertar en slr.publications y slr.author_publications
	 */
	@Transactional
	public void insertIntoAuthorPublications(String doc_type, String state, int limit) throws ParseException  {
		int group = this.configuration.getValidateConfiguration("active").getGroupState();
		
		List<DblpPublications> dblpPublications = this.dblp_repo.getDblpPublicationsByTypeStateGroup(doc_type, state, group, limit) ;
		int i = 0;
		
		for (DblpPublications dblp : dblpPublications) 
		{
			List<Authors> authors = new ArrayList<Authors>();
			//search authors in slr.authors
			if(!dblp.getAuthor().isEmpty()) 
				authors  = this.author_service.searchIndexedAuthors(dblp.getAuthor()) ;
			
			//inserting in slr.publications
			Publications publication = this.publication_service.saveFromDblpPublication(dblp);
			this.entityManager.persist(publication);
			
			//inserting in slr.author_publications
			for (int j = 0; j < authors.size(); j++) {
				AuthorPublications ap = new AuthorPublications(0, authors.get(j),publication, j+1, new Date());
				this.entityManager.persist(ap);
			}
			
			
			//inserting in slr.publishers
			Publishers publisher = null;
			if(!dblp.getPublisher().isEmpty()) {
				publisher = new Publishers(0, dblp.getPublisher(), "active", 
						new Date(), null, null,null);
				this.entityManager.persist(publisher);
			}
			
			//inserting in slr.editions
			Journals journal = null;
			if(!dblp.getJournal().isEmpty()) {
				journal = new Journals(0, dblp.getJournal(), "", new Date(), null);
				this.entityManager.persist(journal);
			}
			
			Conferences conference = null;
			if(dblp.getBookTitle().isEmpty()) {
				conference = new Conferences(0, dblp.getBookTitle(), "", new Date(), null);
				this.entityManager.persist(conference);
			}
			
			Editions edition = new Editions(0, conference, journal, publisher, "", dblp.getVolume(), dblp.getNumber(), null, null, null, null);
			this.entityManager.persist(edition);
			
			switch (dblp.getDocType()) 
			{
				case "book":
					Books book = new Books(0, publication, publisher, 
							dblp.getSeries(),//series
							dblp.getBookTitle(),//bookTitle
							dblp.getPages(),//pages
							dblp.getIsbn(),//isbn
							dblp.getSchool(),//school
							dblp.getCite(),//cite 
							dblp.getMonth(),//month
							dblp.getNote(),//note
							new Date());
					this.entityManager.persist(book);
				break;
				case "incollection":
					BookChapters bookChapter = new BookChapters(0, publication, publisher,
							dblp.getCite(),//cite
							dblp.getChapter(),//chapter
							dblp.getBookTitle(),//bookTitle
							dblp.getPages(),//pages
							dblp.getIsbn(),//isbn
							dblp.getSeries(),//series
							dblp.getSchool(),//school
							new Date());
					this.entityManager.persist(bookChapter);
				break;
				case "inproceedings":
					ConferencePapers conferencePaper = new ConferencePapers(0, edition, publication, 
							dblp.getPages(),//pages
							dblp.getMonth(),//month
							dblp.getCite(),//cite
							dblp.getNote(),//note
							dblp.getBookTitle(),//bookTitle
							new Date());
					this.entityManager.persist(conferencePaper);
				break;
				case "article":
					JournalPapers journalPaper = new JournalPapers(0, edition, publication,
							dblp.getPages(),//pages
							dblp.getMonth(),//month
							dblp.getCite(),//cite
							dblp.getNote(),//note
							dblp.getBookTitle(),//bookTitle
							new Date());
					this.entityManager.persist(journalPaper);
				break;
				
				case "proceedings":
					String proceedingReference = getProceedingReference(dblp.getKeyDblp());
					
					if(Objects.equals(proceedingReference, "incollection")) {
						BookChapters bc = new BookChapters(0, publication, publisher,
								dblp.getCite(),//cite
								dblp.getChapter(),//chapter
								dblp.getBookTitle(),//bookTitle
								dblp.getPages(),//pages
								dblp.getIsbn(),//isbn
								dblp.getSeries(),//series
								dblp.getSchool(),//school
								new Date());
						this.entityManager.persist(bc);
				
					}else if(Objects.equals(proceedingReference, "journal_editorial")) {
						JournalEditorials journalEditorial = new JournalEditorials(0, edition, publication,
								dblp.getSeries(),//series
								dblp.getIsbn(),//isbn 
								dblp.getBookTitle(),//bookTitle 
								new Date(),//createdAt 
								dblp.getNote(),//note
								dblp.getPages()//pages
								);
						this.entityManager.persist(journalEditorial);
					}else if(Objects.equals(proceedingReference, "conference_editorial")) {
						ConferenceEditorials conferenceEditorial = new ConferenceEditorials(0, edition, publication,
								dblp.getIsbn(),//isbn
								dblp.getNote(),//note
								dblp.getSeries(),//series
								dblp.getBookTitle(),//bookTitle,
								dblp.getPages(),//pages
								new Date());
						this.entityManager.persist(conferenceEditorial);
					}
				publication.setProceedingInfo(proceedingReference);
				this.entityManager.merge(publication);
				break;
			}
			dblp.setUpdatedState("2.finalize");
			this.entityManager.merge(dblp);
			i++;
			if(i % 20 == 0) {
				this.entityManager.flush();
				this.entityManager.clear();
			}
			
		}
		
	}
	
	
	/*
	 * @Param: 
	 * @Description: Funcion que inicia la separacion de datos de slr.dblp_publications
	 * 				 en las otras tablas (slr.publications, slr.authors, ...)
	 */
	
	public void parse(String doc_type, String state, int limit)  {
		int group = this.configuration.getValidateConfiguration("active").getGroupState();
		
		List<DblpPublications> dblpPublications = this.dblp_repo.getDblpPublicationsByTypeStateGroup(doc_type, state, group, limit) ;
		List<Publications> publications = new ArrayList<>();
		List<Publications> publicationsNoInserted = new ArrayList<>();
		
		Map<String, List<Authors>> authorsMap = new HashMap<String, List<Authors>>();
		Map<String, Publishers> publisherMap = new HashMap<String, Publishers>();
		//Map<String, Editions> editionMap = new HashMap<String, Editions>();
		
	
		
		try 
		{
			for(int i = 0; i < dblpPublications.size(); i++) 
			{
				DblpPublications dblp = dblpPublications.get(i);
				System.out.println("KEY->"+dblp.getKeyDblp());
				
				//search authors in slr.authors
				if(!dblp.getAuthor().isEmpty()) 
					authorsMap.put(dblp.getKeyDblp(), this.author_service.searchIndexedAuthors(dblp.getAuthor()) );
				else
					authorsMap.put(dblp.getKeyDblp(), null);
				
				
				//adding to List<Publications>
			//	publicationsNoInserted.add( this.publication_service.saveFromDblpPublication(dblp) );
				//inserting publisher
				publisherMap.put(dblp.getKeyDblp(), this.publisher_service
														.registerPublisher(dblp.getPublisher()) );	
				
				if(i%20 == 0) {
					System.out.println("BEGIN INSERTING DATA");
					
					//inserting in slr.publication
					publications  = this.publication_service.savePublicationsList(publicationsNoInserted);
					
					//inserting in slr.author_publications
					publications.forEach(pub->{
						this.authorPublication_service
						.saveAuthorPublicationProcedure(pub, authorsMap.get(pub.getDblpKey()));
					});
					
					
					publicationsNoInserted.clear();
					System.out.println("END INSERTING DATA");
				}
				
				//total rows inserted in slr.author_publications
				//int rows = this.authorPublication_service.saveAuthorPublicationProcedure(publication, authors);
				
				
				
				
				//register in slr.publisher
				//@SuppressWarnings("unused")
				//Publishers publisher = dblp.getPublisher().isEmpty() ?  null : this.publisher_service.registerPublisher(dblp.getPublisher());
				
			}
//			if(dblpPublications.size() < batchSize && dblpPublications.size() > 0) {
//				 this.entityManager.flush();
//		         this.entityManager.clear();
//			}
			
			
		}catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
			System.err.println("function: parse() "+e.getMessage());
		}
	}
	
	/*
	 * Begin parse functions to  slr.dblp_publications 
	 */
	public List<String> getListNames(List<PersonName> persons) {
		List<String>  res = new ArrayList<>();
		for (int i = 0; i < persons.size(); i++)
			res.add(persons.get(i).name());
		
		return res;
	}

	public Map<String, String> parseFields(Collection<Field> fields) {
		//default map
		Map<String, String> map = new HashMap<>();
		
		map.put("pages","");
		map.put("year","");
		map.put("title","");
		map.put("address","");
		map.put("journal","");
		map.put("volume","");
		map.put("number","");
		map.put("month","");
		map.put("url","");
		map.put("ee","");
		map.put("cdrom","");
		map.put("cite","");
		map.put("publisher","");
		map.put("note","");
		map.put("crossref","");
		map.put("isbn","");
		map.put("series","");
		map.put("school","");
		map.put("chapter","");
		map.put("publnr","");
		map.put("book_title","");
	
		List<String> doi = new ArrayList<>();
		
		for(@SuppressWarnings("rawtypes")
		Iterator iterator = fields.iterator() ; iterator.hasNext();) 
		{
			Field field = (Field) iterator.next();
			switch (field.tag())
			{ 
				case "pages": map.put("pages", field.value() ); break;
				case "year": 
					//map.put("year", field.value() );
					String year = field.value().isEmpty() ? "0":field.value();
					map.put("year", year);
					break;
				case "title": map.put("title", field.value() ); break;
				case "address": map.put("address", field.value() ); break;
				case "journal": map.put("journal", field.value() ); break;
				case "volume": map.put("volume", field.value() ); break;
				case "number": map.put("number", field.value() ); break;
				case "month": map.put("month", field.value() ); break;
				case "url": map.put("url", field.value() ); break;
				case "ee":
					doi.add(field.value());
					break;
				case "cdrom": map.put("cdrom", field.value() ); break;
				case "cite": map.put("cite", field.value() ); break;
				case "publisher": map.put("publisher", field.value() ); break;
				case "note": map.put("note", field.value() ); break;
				case "crossref": map.put("crossref", field.value() ); break;
				case "isbn": map.put("isbn", field.value() ); break;
				case "series": map.put("series", field.value() ); break;
				case "school": map.put("school", field.value() ); break;
				case "chapter": map.put("chapter", field.value() ); break;
				case "publnr": map.put("publnr", field.value() ); break;
				case "booktitle": map.put("book_title", field.value() ); break;

			default:
				//System.err.println("Not save field: "+field.tag());
				break;
			}
				
		}
		
		if(!doi.isEmpty()) {
			map.put("ee",  verifyDoiEe(doi));
			doi.clear();
		}
		
		return map;
	}
	
	public String verifyDoiEe(List<String> dois) {
		
		List<String> result =  dois.stream()
							  .filter(doi -> doi.contains("doi.org"))
							  .collect(Collectors.toList());
		
		return result.isEmpty() ? dois.get(0) : result.get(result.size()-1);
	}
		
	@SuppressWarnings("static-access")
	public int batchDblpPublicationInsert(List<DblpPublications> publications) {
		int res = 0;
		DBConnect db = null;
		Connection conn;
		try {
			conn = db.getInstance().getConnection();
			conn.setAutoCommit(false);
			
			PreparedStatement ps = conn.prepareStatement(" INSERT INTO slr.dblp_publications( key_dblp, author, title, book_title, pages,"
					+ " year, crossref, address, journal, volume,"
					+ " number, month, url, ee, cdrom,"
					+ " cite, publisher, note, isbn, series,"
					+ " school, chapter, publnr, mdate," 
					+ " updated_state, grupo, doc_type ) " + 
					"	VALUES (?, ?, ?, ?, ?,"
					+ " ?, ?, ?, ?, ?,"
					+ " ?, ?, ?, ?, ?,"
					+ " ?, ?, ?, ?, ?,"
					+ " ?, ?, ?, ?,"
					+ " ?, ?, ?);");
			
			for (int i = 0; i < publications.size(); i++) {
				DblpPublications dblp = publications.get(i);
				
				ps.setString(1, dblp.getKeyDblp());
				ps.setArray(2, ps.getConnection().createArrayOf( "VARCHAR", dblp.getAuthor().toArray() ));
				ps.setString(3, dblp.getTitle());
				ps.setString(4, dblp.getBookTitle());
				ps.setString(5, dblp.getPages());
				
				ps.setInt(6, dblp.getYear());
				ps.setString(7, dblp.getCrossref());
				ps.setString(8, dblp.getAddress());
				ps.setString(9, dblp.getJournal());
				ps.setString(10, dblp.getVolume());
				
				ps.setString(11, dblp.getNumber());
				ps.setString(12, dblp.getMonth());
				ps.setString(13, dblp.getUrl());
				ps.setString(14, dblp.getEe());
				ps.setString(15, dblp.getCdrom());
				
				ps.setString(16, dblp.getCite());
				ps.setString(17, dblp.getPublisher());
				ps.setString(18, dblp.getNote());
				ps.setString(19, dblp.getIsbn());
				ps.setString(20, dblp.getSeries());
				
				ps.setString(21, dblp.getSchool());
				ps.setString(22, dblp.getChapter());
				ps.setString(23, dblp.getPublnr());
				ps.setString(24, dblp.getMdate());
				
				ps.setString(25, dblp.getUpdatedState());
				ps.setInt(26, dblp.getGrupo());
				ps.setString(27, dblp.getDocType());
				

				ps.addBatch();
				res++;
			}
			ps.executeBatch();
			conn.commit();
			conn.close();
			
		}catch (SQLException e) {
			System.out.println("function :batchDblpPublicationInsert(), "+e.getMessage());
		}        
		return res;
	}

	/* 
	 * End parse functions to  slr.dblp_publications
	 */
	
}
