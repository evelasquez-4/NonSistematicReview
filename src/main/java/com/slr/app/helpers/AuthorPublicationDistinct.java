package com.slr.app.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

import com.slr.app.models.AuthorPublications;
import com.slr.app.models.Authors;
import com.slr.app.models.IndexedAuthorPublications;

@Component
public class AuthorPublicationDistinct {
	
	public List<IndexedAuthorPublications> filterDistinctObjects(List<AuthorPublications> list) {
		List<IndexedAuthorPublications> response = new ArrayList<IndexedAuthorPublications>();
		List<AuthorPublications> search_list = getAuthorPublicationsSearchList(list);
		
		for(AuthorPublications ap : list) 
		{
			IndexedAuthorPublications idx = new IndexedAuthorPublications();
			
			if( Objects.isNull( ap.getHerarchy() ) || ap.getHerarchy() < 1) {
				idx.setPublication(ap.getPublications());

			}else if( ap.getHerarchy() == 1) {
				
				idx.setPublication(ap.getPublications());
				idx.getAuthors().add(ap.getAuthors());
				
				List<Authors> authors = searchAuthorsFromPublication(search_list, ap.getPublications().getId());
				
				authors.forEach( a->{
					idx.getAuthors().add(a);
				});
				
			}
			if(idx.getPublication().getId() > 0)
				response.add(idx);
		}
		
		return response;
	}
	
	public List<Authors> searchAuthorsFromPublication(List<AuthorPublications> ap,long id){
		List<Authors> response = new ArrayList<Authors>();
		
		ap.stream().filter(x->(x.getPublications().getId() == id)).forEach( y->{
			response.add(y.getAuthors());
		} );
		
		return response;
	}
	
	public List<AuthorPublications> getAuthorPublicationsSearchList(List<AuthorPublications> ap){
		List<AuthorPublications> res = new ArrayList<AuthorPublications>();
		 ap.stream().filter(x->( Objects.nonNull(x.getHerarchy()) && x.getHerarchy() > 1 ) ).forEach(y->{
			res.add(y);
		});
		
		return res;
	}
	
	public List<AuthorPublications> getFirstAuthorPublicationsSearchList(List<AuthorPublications> ap){
		List<AuthorPublications> res = new ArrayList<AuthorPublications>();
		ap.stream().filter( x-> ( Objects.nonNull(x.getHerarchy()) && x.getHerarchy() == 1 ))
								.forEach(y->{
									res.add(y);
								});
		return res;
	}
}
