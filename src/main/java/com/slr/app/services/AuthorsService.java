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
	
	public List<Authors> getAuthorsFromPublicationId(Long publication_id){
		return this.author_repository.getAuthorsFromPublicationId(publication_id);
	}
	
	public Authors saveAuthors(Authors author) {
		return this.author_repository.save(author);
	}
	
	public List<Authors> getAuthorsByGroupPublicationUpdated(int grupo, boolean publication_updated, int limit){
		return this.author_repository.getAuthorsByGroupPublicationUpdated(grupo, publication_updated, limit);
	}
	
	public List<Authors> getAuthorsByGroupPublicationUpdated(boolean publication_updated, int limit){
		
		return this.author_repository.getAuthorsByGroupPublicationUpdated(
				this.configuration_service.getValidateConfiguration("active").getGroupState(), 
				publication_updated, limit);
	}
	
	
	public List<Authors> findAuthorsIndexedByListAuthors(List<String> authors){
		List<Authors> response = new ArrayList<Authors>();
		try {	
			for (String author : authors) 
			{
				List<Authors> auth = this.lucene_index.findIndexedAuthorsByNamesHomonyns(author,"authors",1);
				
				if(auth.isEmpty()) {
					//throw new IndexOutOfBoundsException("function findAuthorsIndexedByListAuthors() : name, "+au+" not found in slr.authors");
					System.err.println("function findAuthorsIndexedByListAuthors() : name, "+author+" not found in slr.authors");
					System.err.println("inserting author: "+author);
						
					Authors obj = new Authors();
					obj.setNames(author);
					obj.setCreatedAt(new Date());
					obj.setInsertGroup(this.configuration_service.getValidateConfiguration("active").getGroupState());
						
					response.add(saveAuthors(obj));
				}else{
					for (Authors a : auth)
						response.add(a);
				}	
			}
				
		} catch (Exception e) {
			System.err.println("function findIndexedAuthorsByName(): "+e.getMessage());
		}
		return response;
	}
	
	
	public List<Authors> getAuthorsToUpdateAffiliation(boolean publication_updated, int grupo, int limit){
		return this.author_repository
				.getAuthorsToUpdateAffiliation(publication_updated, grupo, limit);
	}
	
	
	
	
	//begin parse authors to slr.authors
	public void parseAuthors(String xml, String dtd)  {
		
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
        	List<String> affiliations = new ArrayList<String>();
        	
        	
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
							if(att.equals("type")) 
							{ 
								if(atribts.get(att).equals("affiliation"))
									affiliations.add( f.value()  ); 
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
        			group,
        			false,//publicationsUpdated
        			affiliations,//affiliations
        			null
        			)  );
        	
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
		//System.out.println("Rows to insert: "+authors.size());
		int res = 0;
		
		DBConnect db = null;
		Connection conn;
		try 
		{
			conn = db.getInstance().getConnection();
			conn.setAutoCommit(false);
			
			PreparedStatement ps = conn.prepareStatement(" INSERT INTO slr.authors (key, pid, position, skills,"
					+ " disciplines, names, homonyns, urls, cites, "
					+ " awards, affiliations, mdate, insert_group ) "
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
				ps.setArray(11, ps.getConnection().createArrayOf("VARCHAR", author.getAffiliations().toArray()));
				ps.setString(12, author.getMdate());
				ps.setInt(13, Integer.valueOf( author.getInsertGroup() ));
				
				ps.addBatch();
				res++;
			}
			ps.executeBatch();
			conn.commit();
			conn.close();
			
		}catch (SQLException e) {
			System.out.println("function batchAuthorsInsert : "+e.getMessage());
			 return 0;
		}
		return res;
	}
	
	/*
	@SuppressWarnings("static-access")
	public int batchAuthorsInsertToOrganizationsAndAuthors(List<Authors> authors)  {
		int res = 0;
		
		DBConnect db = null;
		Connection conn;
		String org_query = "INSERT INTO slr.organizations(description, country_id) VALUES(?, ?)";
		
		String query = "INSERT INTO slr.authors (key, pid, position, skills,"
				+ " disciplines, names, homonyns, urls, cites, "
				+ " awards, mdate, insert_group, organization_id ) "
				+ "  VALUES( ? ,? ,? , ?, "
				+ " ?, ?, ?, ? , ?, "
				+ " ?, ?, ?, ? );";
		
		try 
		{
			conn = db.getInstance().getConnection();
			conn.setAutoCommit(false);
			
			String[] columns = {"id"};
			PreparedStatement org = conn.prepareStatement(org_query, columns);
			PreparedStatement ps = conn.prepareStatement(query);
			for(Authors author : authors) 
			{
				int id = 0;
				Organizations or = author.getOrganizationses();
				
				if(!Objects.isNull( or ) ){
					
					org.setString(1, or.getDescription());
					org.setLong(2, or.getCountries().getId());
					org.execute();
					 
					ResultSet rs = org.getGeneratedKeys();
					if(rs.next()) id = rs.getInt(1);
					
					ps.setInt(13,id );
					
				}
				else 
					ps.setNull(13, java.sql.Types.NULL);

				
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
				ps.setString(11, author.getMdate());
				ps.setInt(12, Integer.valueOf( author.getInsertGroup() ));
				
				
					
				ps.addBatch();
				res++;
			}
			ps.executeBatch();
			conn.commit();
			conn.close();
			
		} catch (SQLException e) {
			System.err.println("function batchAuthorsInsert : "+e.getMessage());
		}
		return res;
	}
	//end parse authors to slr.authors
	 * 
	 */
}
