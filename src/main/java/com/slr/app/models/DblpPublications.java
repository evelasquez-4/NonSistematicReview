//Generated Mar 14, 2021, 7:01:49 PM by Hibernate Tools 4.3.5.Final
package com.slr.app.models;


import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.vladmihalcea.hibernate.type.array.ListArrayType;;

/**
* DblpPublications generated by hbm2java
*/
@Entity
@Table(name = "dblp_publications", schema = "slr")
@TypeDef(
	    name = "list-array",
	    typeClass = ListArrayType.class
	)
@Indexed
public class DblpPublications{

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "c_generator")
    @SequenceGenerator(name = "c_generator", sequenceName = "slr.dblp_publications_id_seq",allocationSize=1)
	private Long id;
	//@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	@Column(name = "key_dblp", length = 250)
	@Field(name = "keydblp",index=Index.YES, analyze=Analyze.NO, store=Store.YES)
	private String keyDblp;
	
	@Type(type = "list-array")
	@Column(	name = "author",	columnDefinition = "text[]" )
	private List<String> author;
	 
//	@Type(type = "string-array")
//	@Column(	name = "editor",	columnDefinition = "text[]" ) 
//	private String[] editor;

	private String title;
	private String bookTitle;
	private String pages;
	
	@Column(name = "year", columnDefinition = "integer default 0")
	private Integer year;
	
	@Field(name = "crossref",index=Index.YES, analyze=Analyze.NO, store=Store.YES)
	private String crossref;
	
	private String address;
	private String journal;
	private String volume;
	private String number;
	private String month;
	
	private String url;
	private String ee;
	private String cdrom;
	private String cite;
	private String publisher;
	private String note;
	private String isbn;
	private String series;
	private String school;
	private String chapter;
	private String publnr;
	private String mdate;
	private Date regDate;
	private String updatedState;
	private Integer grupo;
	private String docType;


	public DblpPublications() {
	}

	public DblpPublications(Long id) {
		this.id = id;
	}
	
	public DblpPublications(Long id, String keyDblp, List<String> author, String title,
			String bookTitle, String pages, Integer year, String crossref, String address, String journal,
			String volume, String number, String month,
			String url, String ee, String cdrom, String cite,
			String publisher, String note, String isbn, String series, String school, String chapter, String publnr,
			String mdate, Date regDate, String updatedState, Integer grupo, String docType) {
		this.id = id;
		this.keyDblp = keyDblp;
		this.author = author;
		this.title = title;
		this.bookTitle = bookTitle;
		this.pages = pages;
		this.year = year;
		this.crossref = crossref;
		this.address = address;
		this.journal = journal;
		this.volume = volume;
		this.number = number;
		this.month = month;
		this.url = url;
		this.ee = ee;
		this.cdrom = cdrom;
		this.cite = cite;
		this.publisher = publisher;
		this.note = note;
		this.isbn = isbn;
		this.series = series;
		this.school = school;
		this.chapter = chapter;
		this.publnr = publnr;
		this.mdate = mdate;
		this.regDate = regDate;
		this.updatedState = updatedState;
		this.grupo = grupo;
		this.docType = docType;
	}

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	public String getKeyDblp() {
		return this.keyDblp;
	}

	public void setKeyDblp(String keyDblp) {
		this.keyDblp = keyDblp;
	}

	@Column(name = "author")
	public List<String> getAuthor() {
		return this.author;
	}

	public void setAuthor(List<String> author) {
		this.author = author;
	}

	@Column(name = "title")
	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(name = "book_title")
	public String getBookTitle() {
		return this.bookTitle;
	}

	public void setBookTitle(String bookTitle) {
		this.bookTitle = bookTitle;
	}

	@Column(name = "pages", length = 100)
	public String getPages() {
		return this.pages;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public Integer getYear() {
		return this.year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	@Column(name = "crossref")
	public String getCrossref() {
		return this.crossref;
	}

	public void setCrossref(String crossref) {
		this.crossref = crossref;
	}

	@Column(name = "address")
	public String getAddress() {
		return this.address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "journal")
	public String getJournal() {
		return this.journal;
	}

	public void setJournal(String journal) {
		this.journal = journal;
	}

	@Column(name = "volume", length = 100)
	public String getVolume() {
		return this.volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	@Column(name = "number", length = 100)
	public String getNumber() {
		return this.number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	@Column(name = "month", length = 100)
	public String getMonth() {
		return this.month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	@Column(name = "url")
	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "ee")
	public String getEe() {
		return this.ee;
	}

	public void setEe(String ee) {
		this.ee = ee;
	}

	@Column(name = "cdrom")
	public String getCdrom() {
		return this.cdrom;
	}

	public void setCdrom(String cdrom) {
		this.cdrom = cdrom;
	}

	@Column(name = "cite")
	public String getCite() {
		return this.cite;
	}

	public void setCite(String cite) {
		this.cite = cite;
	}

	@Column(name = "publisher")
	public String getPublisher() {
		return this.publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	@Column(name = "note")
	public String getNote() {
		return this.note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Column(name = "isbn")
	public String getIsbn() {
		return this.isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	@Column(name = "series")
	public String getSeries() {
		return this.series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	@Column(name = "school")
	public String getSchool() {
		return this.school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	@Column(name = "chapter")
	public String getChapter() {
		return this.chapter;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	@Column(name = "publnr")
	public String getPublnr() {
		return this.publnr;
	}

	public void setPublnr(String publnr) {
		this.publnr = publnr;
	}

	@Column(name = "mdate", length = 100)
	public String getMdate() {
		return this.mdate;
	}

	public void setMdate(String mdate) {
		this.mdate = mdate;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "reg_date", length = 13)
	public Date getRegDate() {
		return this.regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	@Column(name = "updated_state", length = 100)
	public String getUpdatedState() {
		return this.updatedState;
	}

	public void setUpdatedState(String updatedState) {
		this.updatedState = updatedState;
	}

	@Column(name = "grupo")
	public Integer getGrupo() {
		return this.grupo;
	}

	public void setGrupo(Integer grupo) {
		this.grupo = grupo;
	}

	@Column(name = "doc_type", length = 100)
	public String getDocType() {
		return this.docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

}
