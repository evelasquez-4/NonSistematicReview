package com.slr.app.models;
// Generated Mar 27, 2021 5:30:49 PM by Hibernate Tools 4.3.5.Final

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embedded;
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

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.search.engine.backend.types.ObjectStructure;
import org.hibernate.search.engine.backend.types.Projectable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.array.ListArrayType;

/**
 * Authors generated by hbm2java
 */
@Entity
@Table(name = "authors", schema = "slr")
@TypeDef(
	    name = "list-array",
	    typeClass = ListArrayType.class
	)
public class Authors implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auth_sequence")
	//@SequenceGenerator(name = "auth_sequence", sequenceName = "slr.author_publications_id_seq",allocationSize=1)
	
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_id",referencedColumnName = "id")
	@Embedded
    @IndexedEmbedded(structure = ObjectStructure.NESTED)
	private Departments departments;
	
	private String key;
	private String pid;
	
	private String position;
	
	private String skills;
	
	private String disciplines;
	
	@Column(name = "names", nullable = false)
	//@Field(name = "names",index=Index.YES, analyze=Analyze.YES, store=Store.NO)
	@FullTextField(analyzer = "english_analyzer",projectable = Projectable.YES)
	private String names;
	
	@Type(type = "list-array")
	@Column(	name = "homonyns",	columnDefinition = "text[]" )
	//@Field(name = "homonyns",index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	@GenericField
	private List<String> homonyns;
	
	@Type(type = "list-array")
	@Column(	name = "urls",	columnDefinition = "text[]" )
	private List<String> urls;
	
	@Type(type = "list-array")
	@Column(	name = "cites",	columnDefinition = "text[]" )
	private List<String> cites;
	
	private String mdate;
	
	private Date createdAt;
	
	@Type(type = "list-array")
	@Column(	name = "awards",	columnDefinition = "text[]" )
	private List<String> awards;
	
	
	private String affiliation;

	@Column(name = "insert_group")
	private int insertGroup;
	
	@Column(name = "publications_updated", nullable = true)
	private boolean publicationsUpdated;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "authors")
	private Set<AuthorPublications> authorPublicationses = new HashSet<AuthorPublications>(0);
	
	public Authors() {
	}

	public Authors(long id, String names) {
		this.id = id;
		this.names = names;
	} 

	public Authors(long id, Departments departments, String key, String pid, String position, String skills,
			String disciplines, String names, List<String> homonyns, List<String> urls, List<String> cites, String mdate,
			Date createdAt, List<String> awards ,String affiliation, int group) {
		this.id = id;
		this.departments = departments;
		this.key = key;
		this.pid = pid;
		this.position = position;
		this.skills = skills;
		this.disciplines = disciplines;
		this.names = names;
		this.homonyns = homonyns;
		this.urls = urls;
		this.cites = cites;
		this.mdate = mdate;
		this.createdAt = createdAt;
		this.awards = awards;
		this.affiliation = affiliation;
		this.insertGroup = group;
		this.publicationsUpdated = false;
	}
	
	public Authors(long id, Departments departments, String key, String pid, String position, String skills,
			String disciplines, String names, List<String> homonyns, List<String> urls, List<String> cites,
			Date createdAt, List<String> awards, String affiliation, String mdate, Integer insertGroup,
			Boolean publicationsUpdated, Set<AuthorPublications> authorPublicationses) {
		this.id = id;
		this.departments = departments;
		this.key = key;
		this.pid = pid;
		this.position = position;
		this.skills = skills;
		this.disciplines = disciplines;
		this.names = names;
		this.homonyns = homonyns;
		this.urls = urls;
		this.cites = cites;
		this.createdAt = createdAt;
		this.awards = awards;
		this.affiliation = affiliation;
		this.mdate = mdate;
		this.insertGroup = insertGroup;
		this.publicationsUpdated = publicationsUpdated;
		this.authorPublicationses = authorPublicationses;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Departments getDepartments() {
		return this.departments;
	}

	public void setDepartments(Departments departments) {
		this.departments = departments;
	}

	@Column(name = "key")
	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Column(name = "pid")
	public String getPid() {
		return this.pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	@Column(name = "position")
	public String getPosition() {
		return this.position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	@Column(name = "skills")
	public String getSkills() {
		return this.skills;
	}

	public void setSkills(String skills) {
		this.skills = skills;
	}

	@Column(name = "disciplines")
	public String getDisciplines() {
		return this.disciplines;
	}

	public void setDisciplines(String disciplines) {
		this.disciplines = disciplines;
	}

	@Column(name = "names", nullable = false)
	public String getNames() {
		return this.names;
	}

	public void setNames(String names) {
		this.names = names;
	}

	@Column(name = "homonyns")
	public List<String> getHomonyns() {
		return this.homonyns;
	}

	public void setHomonyns(List<String> homonyns) {
		this.homonyns = homonyns;
	}

	@Column(name = "urls")
	public List<String> getUrls() {
		return this.urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	@Column(name = "cites")
	public List<String> getCites() {
		return this.cites;
	}

	public void setCites(List<String> cites) {
		this.cites = cites;
	}

	@Column(name = "mdate", length = 70)
	public String getMdate() {
		return this.mdate;
	}

	public void setMdate(String mdate) {
		this.mdate = mdate;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "created_at", length = 13)
	public Date getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Column(name = "awards")
	public List<String> getAwards() {
		return this.awards;
	}

	public void setAwards(List<String> awards) {
		this.awards = awards;
	}

	@Column(name = "affiliation")
	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
	
	
	public int getGroup() {
		return insertGroup;
	}

	public void setGroup(int group) {
		this.insertGroup = group;
	}

	public boolean isPublicationsUpdated() {
		return publicationsUpdated;
	}

	public void setPublicationsUpdated(boolean publicationsUpdated) {
		this.publicationsUpdated = publicationsUpdated;
	}
	
	//@JsonManagedReference
	@JsonIgnore
	public Set<AuthorPublications> getAuthorPublicationses() {
		return this.authorPublicationses;
	}

	public void setAuthorPublicationses(Set<AuthorPublications> authorPublicationses) {
		this.authorPublicationses = authorPublicationses;
	}
}
