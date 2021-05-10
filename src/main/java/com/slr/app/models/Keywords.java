package com.slr.app.models;
// Generated Jun 22, 2020, 3:24:17 AM by Hibernate Tools 5.2.12.Final

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonManagedReference;

/**
 * Keywords generated by hbm2java
 */
@Entity
@Table(name = "keywords", schema = "slr")
public class Keywords implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private long id;
	
	private String decription;
	private Date createdAt;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "keywords")
	private Set<PublicationKeywords> publicationKeywordses = new HashSet<PublicationKeywords>(0);

	public Keywords() {
	}

	public Keywords(long id) {
		this.id = id;
	}

	public Keywords(long id, String decription, Date createdAt, Set<PublicationKeywords> publicationKeywordses) {
		this.id = id;
		this.decription = decription;
		this.createdAt = createdAt;
		this.publicationKeywordses = publicationKeywordses;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "decription")
	public String getDecription() {
		return this.decription;
	}

	public void setDecription(String decription) {
		this.decription = decription;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "created_at", length = 13)
	public Date getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@JsonManagedReference
	public Set<PublicationKeywords> getPublicationKeywordses() {
		return this.publicationKeywordses;
	}

	public void setPublicationKeywordses(Set<PublicationKeywords> publicationKeywordses) {
		this.publicationKeywordses = publicationKeywordses;
	}

}
