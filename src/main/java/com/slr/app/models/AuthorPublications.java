package com.slr.app.models;
// Generated Jun 22, 2020, 2:29:20 AM by Hibernate Tools 5.4.3.Final

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.search.engine.backend.types.ObjectStructure;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * AuthorPublications generated by hbm2java
 */
@Entity
@Table(name = "author_publications", schema = "slr")
@JsonIgnoreProperties(value={"hibernateLazyInitializer","handler","fieldHandler"})
@Indexed
public class AuthorPublications implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	//@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "authpub_sequence")
	//@SequenceGenerator(name = "authpub_sequence", sequenceName = "slr.authors_id_seq",allocationSize=1)
	
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id", referencedColumnName = "id")
	@IndexedEmbedded(structure = ObjectStructure.NESTED)
	private Authors authors;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publication_id",referencedColumnName = "id")
	@IndexedEmbedded(structure = ObjectStructure.NESTED)
	private Publications publications;
	
	@Column(name = "herarchy")
	private Integer herarchy;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "created_at", length = 13)
	private Date createdAt;

	public AuthorPublications() {
	}

	public AuthorPublications(long id) {
		this.id = id;
	}

	public AuthorPublications(long id, Authors authors, Publications publications, Integer herarchy, Date createdAt) {
		this.id = id;
		this.authors = authors;
		this.publications = publications;
		this.herarchy = herarchy;
		this.createdAt = createdAt;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	//@JsonBackReference
	public Authors getAuthors() {
		return this.authors;
	}

	public void setAuthors(Authors authors) {
		this.authors = authors;
	}
	
	//@JsonBackReference
	public Publications getPublications() {
		return this.publications;
	}

	public void setPublications(Publications publications) {
		this.publications = publications;
	}

	public Integer getHerarchy() {
		return this.herarchy;
	}

	public void setHerarchy(Integer herarchy) {
		this.herarchy = herarchy;
	}

	public Date getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
