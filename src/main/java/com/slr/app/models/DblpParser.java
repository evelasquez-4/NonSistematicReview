package com.slr.app.models;

//
//Copyright (c)2015, dblp Team (University of Trier and
//Schloss Dagstuhl - Leibniz-Zentrum fuer Informatik GmbH)
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without
//modification, are permitted provided that the following conditions are met:
//
//(1) Redistributions of source code must retain the above copyright
//notice, this list of conditions and the following disclaimer.
//
//(2) Redistributions in binary form must reproduce the above copyright
//notice, this list of conditions and the following disclaimer in the
//documentation and/or other materials provided with the distribution.
//
//(3) Neither the name of the dblp team nor the names of its contributors
//may be used to endorse or promote products derived from this software
//without specific prior written permission.
//
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
//ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//DISCLAIMED. IN NO EVENT SHALL DBLP TEAM BE LIABLE FOR ANY
//DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
//(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
//LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
//ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


//import org.dblp.mmdb.Person;
import org.dblp.mmdb.PersonName;
import org.dblp.mmdb.Publication;
import org.dblp.mmdb.RecordDb;
import org.dblp.mmdb.RecordDbInterface;

import org.springframework.stereotype.Component;
import org.dblp.mmdb.Field;

import org.xml.sax.SAXException;
 
@Component
public class DblpParser {
	
	private String xmlFileName, dtdFileName;
	
	public DblpParser() {
	}
	
	public DblpParser(String xmlFileName, String dtdFileName) {
		
		if(xmlFileName.isEmpty()  || dtdFileName.isEmpty()){
			 System.err.format("Usage: java %s <dblp-xml-file> <dblp-dtd-file>\n", DblpParser.class.getName());
	         //System.exit(0);
		}
		System.out.println("xml->"+xmlFileName+"\n dtd->"+dtdFileName);
		this.xmlFileName = xmlFileName;
		this.dtdFileName = dtdFileName; 
	}
	
	
	public void parseFiles(int group_state,int batchSize) {
		 System.out.println("building the dblp main memory DB ...");
		 RecordDbInterface dblp;
		 try { 
			   dblp = new RecordDb(this.xmlFileName, this.dtdFileName, false); }
		 catch (final IOException ex) {
			 System.err.println("cannot read dblp XML: " + ex.getMessage());
		     return;
		 }
		 catch (final SAXException ex) { 
			 System.err.println("cannot parse XML: " + ex.getMessage());
		     return;
		 }
		   
	     System.out.format("MMDB ready: %d publs, %d pers\n\n", dblp.numberOfPublications(), dblp.numberOfPersons());
		 
	     List<String> authors = new ArrayList<>();
	     List<String> documents = Arrays.asList("article","inproceedings","proceedings","book","incollection");
	     List<DblpPublications> publicationsBatch = new ArrayList<>();
	     
	     for(Publication publication : dblp.getPublications()) 
	     {
	    	 //doc_type
	    	 String docType = publication.getTag();
	    	 
	    	 if( documents.contains(docType)) 
	    	 {
		    	 /*
		    	  * key_dblp -> attributes.get("key")
		    	  * mdate -> attributes.get("mdate")
		    	  */
		    	 Map<String, String> attributes = publication.getAttributes();
		    	 //System.out.format("key->%s, doc_type->%s\n",publication.getKey(), publication.getTag());
		    	 
		    	 authors = getListNames(publication.getNames());
		    	 
		    	 Map<String,String> fields = parseFields(publication.getFields());		    	 
		    	 //using for-each loop for iteration over Map.entrySet() 
		         //for (Map.Entry<String,String> field : fields.entrySet()) {
		        	 //System.out.println("Key = " + field.getKey() + ", Value = " + field.getValue());
		    	 //} 
		        
		    	 publicationsBatch.add( new DblpPublications( Long.valueOf(0),
		    			 attributes.get("key"),
		    			 //authors.toArray(new String[authors.size()]),
		    			 authors,
		    			 fields.get("title"),
		    			 fields.get("book_title"), 
		    			 fields.get("pages"), 
		    			 Integer.parseInt(fields.get("year")), 
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
		    			 group_state, 
		    			 docType ) 
		    		);
		    	 
		    	 
		    	 
		    	 if(  publicationsBatch.size() % batchSize  == 0) 
		    	 {
		    		 batchInsertPublication(publicationsBatch, batchSize);
		    		 publicationsBatch.clear();
		    	 }   
	    	 }
	    	 else {
	    		 //read authors field
	    	 }
	     }  
	     
	}


	public void batchInsertPublication(List<DblpPublications> publications, int batchSize) {
		
		} 
	
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
		Iterator iterator = fields.iterator() ; iterator.hasNext();) {
			Field field = (Field) iterator.next();
			switch (field.tag())
			{
				case "pages": map.put("pages", field.value() ); break;
				case "year": map.put("year", field.value() ); break;
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
				case "book_title": map.put("book_title", field.value() ); break;

			default:
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
}