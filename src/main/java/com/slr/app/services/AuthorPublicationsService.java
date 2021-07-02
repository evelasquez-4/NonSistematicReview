package com.slr.app.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.ParameterMode;

import org.hibernate.Session;
import org.hibernate.procedure.ProcedureCall;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.config.HibernateUtil;
import com.slr.app.helpers.SlrHibernateLuceneIndex;
import com.slr.app.models.AuthorPublications;
import com.slr.app.models.Authors;
import com.slr.app.models.Publications;
import com.slr.app.models.DblpPublications;
import com.slr.app.repositories.AuthorPublicationsRepository;

@Service
public class AuthorPublicationsService {
	@Autowired
	private AuthorPublicationsRepository author_publications;
	@Autowired
	private PublicationsServices publication_service;
	@Autowired
	private SlrHibernateLuceneIndex index_service;
	
	
	public AuthorPublications findById(Long id) {
		Optional<AuthorPublications> res = this.author_publications.findById(id);
		
		if(!res.isPresent())
			throw new RuntimeException("AuthorPublications id :"+id+" does not exists."); 
			
		return res.get();
	}
	
	public List<AuthorPublications> findByPublicationId(Long publication_id) {
		return this.author_publications.findByPublicationId(publication_id);
	} 
	
	public List<AuthorPublications> saveAuthorPublications(Publications publications, List<Authors> authors) {
		List<AuthorPublications> res = new ArrayList<AuthorPublications>();
		for (int i = 0; i < authors.size(); i++) {
			res.add(	this.author_publications.save(
					new AuthorPublications(0, authors.get(i), publications,i + 1, new Date() ) )
					);
		}
		return res;
	}
	
	
	public List<AuthorPublications> saveFromDblpPublication(DblpPublications dblp) {
		List<AuthorPublications> res = new ArrayList<AuthorPublications>();
		
		try {
			//save publication
			Publications publications = this.publication_service.savePublications(
					new Publications(Long.valueOf(0), 
							"",//abstract_ 
							dblp.getTitle(),//title,
							dblp.getKeyDblp(),//dblpKey
							dblp.getYear(),//year
							dblp.getUrl(), //url
							dblp.getEe(), //ee
							dblp.getNote(), //note
							dblp.getCrossref(), //crossref, 
							new SimpleDateFormat("YYYY-mm-dd").parse(dblp.getMdate()),//mdate
							"1.inserted", //updatedState
							dblp.getDocType(),// docType
							new Date(), //regDate
							null, //bookChapters
							null, //journalEditorials
							null, //conferenceEditorials
							null, //authorPublicationses
							null, //conferencePapers
							null, //books
							null, //journalPapers
							null // publicationKeywordses
							)
					);
			
			//find and save authors
			List<Authors> findedAuthors = new ArrayList<Authors>();
			
			for(String name : dblp.getAuthor()) 
			{
				List<Authors> auth = this.index_service.findAuthorsIndexedByName(name);
				//add the first element of authors array finded
				findedAuthors.add(auth.get(0));
			}
			
			res = saveAuthorPublications(publications, findedAuthors);
			
			
		}catch (ParseException e) {
			System.err.println("function saveFromDblpPublication(): "+e.getMessage());
		}
		
		
		return res;
	}

	
	public int saveAuthorPublicationProcedure(Publications publication, List<Authors> authors) {
		int res = 0;
		Session session = null;
		List<String> ids = new ArrayList<>();
		authors.forEach(aut->{
			ids.add(String.valueOf(aut.getId()));
		}); 
		
		try 
		{
			session = HibernateUtil.getSession();
			ProcedureCall procedureCall = session
					.createStoredProcedureCall("slr.slr_insert_author_publications");
			procedureCall.registerParameter("action",String.class,ParameterMode.IN).bindValue("INS_AUTH_PUB");
			procedureCall.registerParameter("publication_id",Integer.class,ParameterMode.IN).bindValue(Long.valueOf( publication.getId()).intValue());
			procedureCall.registerParameter("authors_id",String.class,ParameterMode.IN).bindValue(String.join(",", ids));
			procedureCall.registerParameter("response", Integer.class, ParameterMode.OUT);
			
			res = (int) procedureCall.getOutputs().getOutputParameterValue("response"); 
			
			if(session != null) 
				session.close();
					
			return res;
			
		}catch (Exception e) {
			System.err.println("function saveAuthorPublicationProcedure(), "+e.getMessage());
			return 0;
		}
		finally {
			try {	if(session != null) 
				session.close();
			}catch(Exception ex){ System.err.println("function saveAuthorPublicationProcedure(), "+ex.getMessage());}
		}
		
	}
}
