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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
/**
 * Conferences generated by hbm2java
 */
@Entity
@Table(name = "conferences", schema = "slr")
@JsonIgnoreProperties({"hibernateLazyinitializer","handler"})
public class Conferences implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private long id;
	
	
	private String description;
	private String abreviation;
	private Date createdAt;
	
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "conferences")
	private Set<Editions> editionses = new HashSet<Editions>(0);

	public Conferences() {
	}

	public Conferences(long id) {
		this.id = id;
	}

	public Conferences(long id, String description, String abreviation, Date createdAt, Set<Editions> editionses) {
		this.id = id;
		this.description = description;
		this.abreviation = abreviation;
		this.createdAt = createdAt;
		this.editionses = editionses;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "abreviation", length = 200)
	public String getAbreviation() {
		return this.abreviation;
	}

	public void setAbreviation(String abreviation) {
		this.abreviation = abreviation;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "created_at", length = 13)
	public Date getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@JsonIgnore
	public Set<Editions> getEditionses() {
		return this.editionses;
	}

	public void setEditionses(Set<Editions> editionses) {
		this.editionses = editionses;
	}

}
