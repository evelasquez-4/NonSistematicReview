package com.slr.app.models;
// Generated Jun 22, 2020, 3:24:17 AM by Hibernate Tools 5.2.12.Final

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * BookChapters generated by hbm2java
 */
@Entity
@Table(name = "book_chapters", schema = "slr")
public class BookChapters implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private long id;
	
//	@ManyToOne(fetch = FetchType.LAZY)
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "publication_id" ,referencedColumnName = "id")
	private Publications publications;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "publisher_id")
	private Publishers publishers;
	
	
	private String cite;
	private String chapter;
	private String bookTitle;
	private String pages;
	private String isbn;
	private String series;
	private String school;
	private Date createdAt;

	public BookChapters() {
	}

	public BookChapters(long id, Publications publications) {
		this.id = id;
		this.publications = publications;
	}

	public BookChapters(long id, Publications publications, Publishers publishers, String cite, String chapter,
			String bookTitle, String pages, String isbn, String series, String school, Date createdAt) {
		this.id = id;
		this.publications = publications;
		this.publishers = publishers;
		this.cite = cite;
		this.chapter = chapter;
		this.bookTitle = bookTitle;
		this.pages = pages;
		this.isbn = isbn;
		this.series = series;
		this.school = school;
		this.createdAt = createdAt;
	}

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	@JsonBackReference
	public Publications getPublications() {
		return this.publications;
	}

	public void setPublications(Publications publications) {
		this.publications = publications;
	}

	@JsonBackReference
	public Publishers getPublishers() {
		return this.publishers;
	}

	public void setPublishers(Publishers publishers) {
		this.publishers = publishers;
	}

	@Column(name = "cite")
	public String getCite() {
		return this.cite;
	}

	public void setCite(String cite) {
		this.cite = cite;
	}

	@Column(name = "chapter", length = 100)
	public String getChapter() {
		return this.chapter;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
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

	@Column(name = "isbn", length = 200)
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

	@Temporal(TemporalType.DATE)
	@Column(name = "created_at", length = 13)
	public Date getCreatedAt() {
		return this.createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}