package com.slr.app.services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.dblp.mmdb.Field;
import org.dblp.mmdb.Person;
import org.dblp.mmdb.RecordDb;
import org.dblp.mmdb.RecordDbInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.slr.app.config.DBConnect;
import com.slr.app.helpers.SlrHibernateLuceneIndex;
import com.slr.app.models.Authors;
import com.slr.app.repositories.AuthorsRepository;

@Service
public class AuthorsService {

	@Autowired
	private AuthorsRepository author_repository;
	
	@Autowired
	private SlrConfigurationService configuration_service;
	
	@Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
	private int batchSize;
	
	@Autowired
	private SlrHibernateLuceneIndex lucene_index;
	
	
	public Authors findById(long id){
		Optional<Authors> res =  this.author_repository.findById(id);
		if(!res.isPresent())
			throw new RuntimeException("Authors id "+ id +" does not exists");
		return res.get();
	}
	
	public Authors saveAuthors(Authors author) {
		return this.author_repository.saveAndFlush(author);
	}
	
	public List<Authors> searchIndexedAuthors(List<String> names){
//		List<Authors> res = new ArrayList<>();
//		for(String name : names) {
//			List<Authors> result = this.lucene_index.findAuthorsIndexedByName(name);
//			if(result.isEmpty())
//				throw new IndexOutOfBoundsException("function searchIndexedAuthors() : name, "+name+" not found in slr.authors");
//			
//			res.add( this.lucene_index
//					.findAuthorsIndexedByName(name)
//					.get(0)  );
//			
//		}
//		return res;
		return this.lucene_index.findAuthorsIndexedByListAuthors(names);
	}
	
	
	
	
	
	//begin parse authors to slr.authors
	public void parseAuthors(String xml, String dtd) {
		
		if(xml.isEmpty()  || dtd.isEmpty())
			 throw new NullPointerException("Usage: java  <dblp-xml-file> <dblp-dtd-file>");
	         
		 System.out.println("building the dblp main memory DB ...");
		 RecordDbInterface dblp;
		 try { 
			   dblp = new RecordDb(xml, dtd, false); }
		 catch (final IOException ex) {
			 System.err.println("cannot read dblp XML: " + ex.getMessage());
		     return;
		 }
		 catch (final SAXException ex) { 
			 System.err.println("cannot parse XML: " + ex.getMessage());
		     return;
		 }
		   
	     //System.out.format("MMDB ready: %d publs, %d pers\n\n", dblp.numberOfPublications(), dblp.numberOfPersons());
	     
	   System.out.println(  readDblpPersons(dblp.persons() ) ); 
	}
	
	public int readDblpPersons(Stream<Person> persons) 
	{	
		int group = this.configuration_service.getValidateConfiguration("active").getGroupState();
		
		List<Authors> authors = new ArrayList<>();
		int inserts = 0;
		
		for ( Iterator<Person> per = persons.iterator(); per.hasNext(); ) 
		{
			Person person = per.next();
        
        	//person attributes: key, mdate, ...
        	Map<String,String> atts = person.getAttributes();
        	
        	List<String> names = new ArrayList<>();
        	List<String> urls = new ArrayList<>();
        	List<String> cites = new ArrayList<>();
        	List<String> awards = new ArrayList<>();
        	String affiliation = "";
        	
        	//homonyns
        	person.getNames().forEach(name->{
        		names.add( name.name() );
        	});
        	
        	Stream<Field> fields =  person.fields();
        	for ( Iterator<Field> field = fields.iterator(); field.hasNext(); ) 
        	{	
        		Field f = field.next();
        	
        		switch (f.tag()) {
				case "url": urls.add(f.value());	break;
				case "cite" : cites.add( f.value() ); break;
				case "note":
					if(f.hasAttributes()) 
					{
						Map<String,String> atribts = f.getAttributes();
						
						for( String att	: atribts.keySet() ) 
						{
							if(att.equals("type")) { 
								if(atribts.get(att).equals("affiliation")) 
									affiliation  = f.value();
								else if(atribts.get(att).equals("award"))
									awards.add( f.value()+" ,"+atribts.getOrDefault("label", "") );
							}
						}
					}
					break;
				}
        	}

        	authors.add(          			
        			new Authors(0,//id 
        			null,//departments
        			atts.get("key"),//key
        			person.getPid(),
        			"",//position
        			"",//skills
        			"",//disciplines
        			person.getPrimaryName().coreName(),//names
        			names,//homonyns
        			urls,
        			cites,
        			atts.get("mdate"),//mdate
        			new Date(),
        			awards,
        			affiliation,
        			group
        			));	
        	
        	if(authors.size() % this.batchSize == 0) {
        		inserts += batchAuthorsInsert(authors);
        		System.out.println("rows inserted in slr.authors :"+inserts);
        		authors.clear();
        	}
        }
		
		if(!authors.isEmpty()) {
    		inserts += batchAuthorsInsert(authors);
    		System.out.println("rows inserted in slr.authors :"+inserts);
    		authors.clear();
    	}
        
		return inserts;
	}
	
	@SuppressWarnings("static-access")
	public int batchAuthorsInsert(List<Authors> authors) {
		
		System.out.println("Rows to insert: "+authors.size());
		int res = 0;
		
		DBConnect db = null;
		Connection conn;
		try 
		{
			conn = db.getInstance().getConnection();
			conn.setAutoCommit(false);
			
			PreparedStatement ps = conn.prepareStatement(" INSERT INTO slr.authors (key, pid, position, skills,"
					+ " disciplines, names, homonyns, urls, cites, "
					+ " awards, affiliation, mdate, insert_group ) "
					+ "  VALUES( ? ,? ,? , ?, "
					+ " ?, ?, ?, ? , ?, "
					+ " ?, ?, ?, ? );");
			
			for(Authors author : authors) 
			{
				ps.setString(1, author.getKey());
				ps.setString(2, author.getPid());
				//ps.setNull(3, java.sql.Types.NULL);
				ps.setString(3, author.getPosition());
				ps.setString(4, author.getSkills());
				
				ps.setString(5, author.getDisciplines());
				ps.setString(6, author.getNames());
				ps.setArray(7, ps.getConnection().createArrayOf("VARCHAR", author.getHomonyns().toArray() ));
				ps.setArray(8, ps.getConnection().createArrayOf("VARCHAR", author.getUrls().toArray() ));
				ps.setArray(9, ps.getConnection().createArrayOf("VARCHAR", author.getCites().toArray() ));
				
				//ps.setDate(11, (java.sql.Date) new Date());
				ps.setArray(10, ps.getConnection().createArrayOf("VARCHAR", author.getAwards().toArray()));
				ps.setString(11, author.getAffiliation());
				ps.setString(12, author.getMdate());
				ps.setInt(13, Integer.valueOf( author.getGroup() ));
				
				ps.addBatch();
				res++;
			}
			ps.executeBatch();
			conn.commit();
			conn.close();
			
		} catch (SQLException e) {
			System.out.println("function batchAuthorsInsert : "+e.getMessage());
		}
		
		/*
		for(Authors author : authors) 
		{
			try {
				PreparedStatement ps = db.getInstance()
						.getConnection()
						.prepareStatement(" INSERT INTO slr.authors (key, pid, position, skills,"
								+ " disciplines, names, homonyns, urls, cites, "
								+ " awards, affiliation, mdate, insert_group ) "
								+ "  VALUES( ? ,? ,? , ?, "
								+ " ?, ?, ?, ? , ?, "
								+ " ?, ?, ?, ? );");
				
				ps.setString(1, author.getKey());
				ps.setString(2, author.getPid());
				//ps.setNull(3, java.sql.Types.NULL);
				ps.setString(3, author.getPosition());
				ps.setString(4, author.getSkills());
				
				ps.setString(5, author.getDisciplines());
				ps.setString(6, author.getNames());
				ps.setArray(7, ps.getConnection().createArrayOf("VARCHAR", author.getHomonyns().toArray() ));
				ps.setArray(8, ps.getConnection().createArrayOf("VARCHAR", author.getUrls().toArray() ));
				ps.setArray(9, ps.getConnection().createArrayOf("VARCHAR", author.getCites().toArray() ));
				
				//ps.setDate(11, (java.sql.Date) new Date());
				ps.setArray(10, ps.getConnection().createArrayOf("VARCHAR", author.getAwards().toArray()));
				ps.setString(11, author.getAffiliation());
				ps.setString(12, author.getMdate());
				ps.setInt(13, Integer.valueOf( author.getGroup() ));
				
				ps.executeUpdate();
				
				res++;
				
			} catch (SQLException e) {
				System.out.println("function batchAuthorsInsert : "+e.getMessage());
			}
		}
		
		*/
//		try {
//			for (int i = 0; i < authors.size(); i++) {
//				if (i > 0 && i % authors.size() == 0) {
//		            this.entityManager.flush();
//		            this.entityManager.clear();
//		        }
//				
//				this.entityManager.persist( authors.get(i) );
//				res++;
//			}
//		}catch (Exception e) {
//			System.out.println("function :batchDblpPublicationInsert(), "+e.getMessage());
//		}
		return res;
	}

	//end parse authors to slr.authors
}
