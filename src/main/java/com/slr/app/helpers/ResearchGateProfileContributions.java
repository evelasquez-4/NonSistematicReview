package com.slr.app.helpers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
@Component
public class ResearchGateProfileContributions {

	private String pub_title;
	private String pub_url;
	private String pub_type;
	private String pub_date;//format 'month year', example: 'march 2020'
	private String pub_event;
	private List<String> authors;
	private boolean hasAllAuthors;
	
	public ResearchGateProfileContributions() {
		this.pub_title = "";
		this.pub_url = "";
		this.pub_type = "";
		this.pub_date = "";
		this.pub_event = "";
		this.authors = new ArrayList<>();
		this.hasAllAuthors = false;
	}
	
	public String getPub_title() {
		return pub_title;
	}
	public void setPub_title(String pub_title) {
		this.pub_title = pub_title;
	}
	public String getPub_url() {
		return pub_url;
	}
	public void setPub_url(String pub_url) {
		this.pub_url = pub_url;
	}
	public String getPub_type() {
		return pub_type;
	}
	public void setPub_type(String pub_type) {
		this.pub_type = pub_type;
	}
	public String getPub_date() {
		return pub_date;
	}
	public void setPub_date(String pub_date) {
		this.pub_date = pub_date;
	}
	public String getPub_event() {
		return pub_event;
	}
	public void setPub_event(String pub_event) {
		this.pub_event = pub_event;
	}
	public List<String> getAuthors() {
		return authors;
	}
	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}
	public boolean isHasAllAuthors() {
		return hasAllAuthors;
	}
	public void setHasAllAuthors(boolean hasAllAuthors) {
		this.hasAllAuthors = hasAllAuthors;
	}
	
	public void mostrar() {
		System.out.println("Title: "+this.pub_title);
		System.out.println("Type:"+this.pub_type);
		System.out.println("Date: "+this.pub_date);
		System.out.println("Event: "+this.pub_event);
		System.out.println("CompleteAuthors: "+this.hasAllAuthors);
		System.out.println("Authors:");
		this.authors.forEach(aut->{
			System.out.println(aut);
		});
	}
}