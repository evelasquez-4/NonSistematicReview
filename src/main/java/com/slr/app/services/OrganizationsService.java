package com.slr.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slr.app.config.DBConnect;
import com.slr.app.models.Authors;
import com.slr.app.models.Countries;
import com.slr.app.models.Organizations;
import com.slr.app.repositories.OrganizationsRepository;

@Service
@SuppressWarnings({ "static-access" })
public class OrganizationsService {

	@Autowired
	private OrganizationsRepository organization_repo;
	@Autowired
	private CountriesService country_service;
	@Autowired
	private EntityManager entityManager;	
	
	public Organizations findById(Long id) {
		Optional<Organizations> res = this.organization_repo.findById(id);
		if(!res.isPresent())
			throw new RuntimeException("Organizations id: "+id+" does not exists.");
		
		return res.get();
	}
	
	public Organizations save(Organizations organizations) {
		return this.organization_repo.saveAndFlush(organizations);
	}

	
	@Transactional
	public String updateAuthorsAffiliation(List<Authors> authors) {
		int res = 0;
		for (Authors author : authors)
		{	
			if(!author.getAffiliations().isEmpty()) {
				
				for (String af : author.getAffiliations())
					saveOrganizationFromAuthorsAffiliation(af, author);
				
				res++;
			}
			
			author.setPublicationsUpdated(true);
			this.entityManager.merge(author);
		}
		return "Number of Author Affiliations Updated: "+res;
		
	}
	/*
	 * @param: affiliation 'Aarhus University, National Environmental Research Institute, Roskilde, Denmark'
	 * @return: Organization created and Country founded
	 */
	public Organizations saveOrganizationFromAuthorsAffiliation(String affiliation, Authors author)
	{
		if(!affiliation.isEmpty()) 
		{
			String[] split = affiliation.split(",");
			Countries country = this.country_service.getCountrieOrDefault(split[split.length - 1]);
			Collection<String> list = new ArrayList<String>();
			
			for(int i = 0;i< split.length - 1; i++)
				list.add(split[i]);
			
			String desc = split[0];
			
			if(country.getId() > 0)
				desc = "";
			
			desc = list.size() > 0 ? String.join(",", list) : desc;
					
			return  save( new Organizations(0,
							author,
							country, 
							desc, 
							new Date(), 
							false,
							null) );
			
		}else
			return null;
	}
	
	
	public Integer preparedStatementInsert(String description, long country_id) {
		DBConnect db = null;
//		Connection conn;
		String[] columns = {"id"};
		try(
			Connection conn = db.getInstance().getConnection();
			PreparedStatement ps = conn.prepareStatement("INSERT INTO slr.organizations(description, country_id, updated, link)"
						+ " VALUES(?, ?,?,?);", columns);
			
			
				)
		{
			//conn = db.getInstance().getConnection();
			conn.setAutoCommit(false);
//			PreparedStatement ps = conn.prepareStatement("INSERT INTO slr.organizations(description, country_id, updated, link)"
//					+ " VALUES(?, ?,?,?);", columns);
			ps.setString(1, description);
			ps.setLong(2, country_id);
			ps.setBoolean(3, false);
			ps.setNull(4, java.sql.Types.NULL);
			ps.execute();
			
			ResultSet rs = ps.getGeneratedKeys(); 
			if(rs.next())
				System.out.println("ID->"+rs.getInt(1));
			
			conn.commit();
			conn.close();
			return rs.getInt(1);
			
		} catch (SQLException e) {
			System.out.println(""+e.getMessage());
		}
		return null;
	}
}
