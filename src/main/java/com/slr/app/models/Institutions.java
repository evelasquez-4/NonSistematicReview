package com.slr.app.models;
// Generated Mar 27, 2021 5:30:49 PM by Hibernate Tools 4.3.5.Final

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Institutions generated by hbm2java
 */
@Entity
@Table(name = "institutions", schema = "slr")
public class Institutions implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private long id;
	
	@ManyToOne
	@JoinColumn(name = "country_id")
	private Countries countries;
	
	private String description;
	private Date createdAt;
	private Boolean updated;
	private String link;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "institutions")
	private Set<Departments> departmentses = new HashSet<Departments>(0);

	public Institutions() {
	}

	public Institutions(long id) {
		this.id = id;
	}

	public Institutions(long id, Countries countries, String description, Date createdAt, Boolean updated, String link,
			Set<Departments> departmentses) {
		this.id = id;
		this.countries = countries;
		this.description = description;
		this.createdAt = createdAt;
		this.updated = updated;
		this.link = link;
		this.departmentses = departmentses;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Countries getCountries() {
		return this.countries;
	}

	public void setCountries(Countries countries) {
		this.countries = countries;
	}

	@Column(name = "description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "created_at", length = 13)
	public Date getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Column(name = "updated")
	public Boolean getUpdated() {
		return this.updated;
	}

	public void setUpdated(Boolean updated) {
		this.updated = updated;
	}

	@Column(name = "link")
	public String getLink() {
		return this.link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Set<Departments> getDepartmentses() {
		return this.departmentses;
	}

	public void setDepartmentses(Set<Departments> departmentses) {
		this.departmentses = departmentses;
	}

}
