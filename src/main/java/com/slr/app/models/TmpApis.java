package com.slr.app.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;

@Entity
@Table(name = "tmp_apis", schema = "public")
@TypeDefs({
	@TypeDef(	name = "json", typeClass = JsonStringType.class 	),
	@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
	@TypeDef( name = "jsonb-node", typeClass = JsonNodeBinaryType.class)
})
public class TmpApis implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "key_dblp", nullable = false, length = 100)
	private String keyDblp;
	
	@Column(name = "api", length = 30)
	private String api;
	
	@Column(name = "search_params", length = 500)
	private String searchParams;
	
	@Column(name = "grupo")
	private Integer group;
	
	@Column(name = "updated")
	private boolean updated;
	
	@Type(type = "jsonb")
	@Column(name = "data",columnDefinition = "json")
	private Serializable data;
	
	@Temporal(TemporalType.DATE)
	@Column(name = "reg_date", length = 13)
	private Date regDate;

	public TmpApis(long id,String keyDblp, String api, String searchParams, Integer group, boolean updated,
			Serializable data,Date regDate) {
		this.id = id;
		this.keyDblp = keyDblp;
		this.api = api;
		this.searchParams = searchParams;
		this.group = group;
		this.updated = updated;
		this.data = data;
		this.regDate = regDate;
	}
	
	public TmpApis(long id) {
		this.id = id;
	}
	
	public TmpApis() {
		
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getKeyDblp() {
		return keyDblp;
	}

	public void setKeyDblp(String keyDblp) {
		this.keyDblp = keyDblp;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
	}

	public String getSearchParams() {
		return searchParams;
	}

	public void setSearchParams(String searchParams) {
		this.searchParams = searchParams;
	}

	public Integer getGroup() {
		return group;
	}

	public void setGroup(Integer group) {
		this.group = group;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}

	public Serializable getData() {
		return data;
	}

	public void setData(Serializable data) {
		this.data = data;
	}

	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}
	
}
